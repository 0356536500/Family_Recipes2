package com.myapps.ron.family_recipes.viewmodels;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.db.CategoriesDBHelper;
import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.dal.repository.CategoryRepository;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.dal.repository.RepoSearchResults;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.network.modelTO.CategoryTO;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.services.GetAllRecipesService;
import com.myapps.ron.family_recipes.utils.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * class for use by MainActivity.
 * loads all recipes, updates local db and server time.
 */
public class DataViewModel extends ViewModel {
    private RecipeRepository recipeRepository;
    private CategoryRepository categoryRepository;

    private MutableLiveData<QueryModel> queryLiveData = new MutableLiveData<>();
    //Applying transformation to get RepoSearchResults for the given Search Query
    private LiveData<RepoSearchResults> repoResults = Transformations.map(queryLiveData,
            input -> recipeRepository.query(input));

    //Applying transformation to get Live PagedList<Repo> from the RepoSearchResult
    private LiveData<PagedList<RecipeMinimal>> pagedRecipes = Transformations.switchMap(repoResults,
            RepoSearchResults::getData
    );

    private MutableLiveData<List<CategoryEntity>> categoryList = new MutableLiveData<>(); // list of newCategories from api

    private MutableLiveData<String> infoFromLastFetch = new MutableLiveData<>(); // info about new or modified pagedRecipes from last fetch from api
    private CompositeDisposable compositeDisposable;

    private Observer<List<CategoryEntity>> categoryObserver =
            listLiveData -> categoryList.setValue(listLiveData);

    public DataViewModel(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;

        this.compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(this.recipeRepository.dispatchInfo.subscribe(infoFromLastFetch::postValue));
        compositeDisposable.add(this.categoryRepository.dispatchInfo.subscribe(infoFromLastFetch::postValue));

        categoryRepository.getAllCategories().observeForever(categoryObserver);
    }


    public LiveData<PagedList<RecipeMinimal>> getPagedRecipes() {
        return pagedRecipes;
    }


    public void applyQuery(@NonNull QueryModel queryModel) {
        queryLiveData.postValue(queryModel);
    }


    public void fetchFromServerJustedLoggedIn(Context context) {
        GetAllRecipesService.startActionGetAllRecipes(context);
        categoryRepository.fetchCategoriesReactive(context);
    }

    public void fetchFromServer(Context context) {
        recipeRepository.fetchRecipesReactive(context);
        categoryRepository.fetchCategoriesReactive(context);
    }

    public Single<RecipeEntity> getRecipe(String id) {
        return recipeRepository.getRecipe(id);
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categoryList;
    }




    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        categoryRepository.getAllCategories().removeObserver(categoryObserver);
    }

















    private MutableLiveData<List<RecipeEntity>> recipeList = new MutableLiveData<>(); // list of recipes from api
    private MutableLiveData<List<RecipeEntity>> favoriteList = new MutableLiveData<>(); // list of recipes local db
    private MutableLiveData<Boolean> canInitBothRecyclerAndFilters = new MutableLiveData<>();



    private boolean recipesReady = false, categoriesReady = false;

    public void setRecipesReady(boolean value) {
        this.recipesReady = value;
        setCanInitBothRecyclerAndFilters(recipesReady && categoriesReady);
    }

    public void setCategoriesReady(boolean value) {
        this.categoriesReady = value;
        setCanInitBothRecyclerAndFilters(recipesReady && categoriesReady);
    }

    private void setInfoFromLastFetch(String item) {
        infoFromLastFetch.setValue(item);
    }

    public LiveData<String> getInfoFromLastFetch() {
        return infoFromLastFetch;
    }

    private void setCanInitBothRecyclerAndFilters(boolean value) {
        canInitBothRecyclerAndFilters.setValue(value);
    }

    public LiveData<Boolean> getCanInitBothRecyclerAndFilters() {
        return canInitBothRecyclerAndFilters;
    }

    //region recipes
    private void setRecipes(List<RecipeEntity> items) {
        recipeList.setValue(items);
    }

    public LiveData<List<RecipeEntity>> getRecipes() {
        return recipeList;
    }

    public void loadRecipes(final Context context, final String orderBy) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            final String time = DateUtil.getUTCTime();
            APICallsHandler.getAllRecipes(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken(), new MyCallback<List<RecipeTO>>() {
                @Override
                public void onFinished(List<RecipeTO> result) {
                    //PostRecipeToServerService.startActionPostRecipe(context, new ArrayList<>(result), time);
                    if(result != null) {
                        DateUtil.updateServerTime(context, time);
                        if (result.isEmpty())
                            loadLocalRecipes(context, orderBy);
                        else
                            new MyAsyncRecipeUpdate(context, result, orderBy).execute();

                    } else {
                        loadLocalRecipes(context, orderBy);
                        setInfoFromLastFetch(context.getString(R.string.load_error_message));
                    }
                }
            });
        }
        else {
            loadLocalRecipes(context, orderBy);
        }
    }

    public List<RecipeEntity> loadLocalRecipesOrdered(final Context context, String orderBy) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(context);
        return dbHelper.getAllRecipes(orderBy);
    }

    private void loadLocalRecipes(final Context context, final String orderBy) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(context);
        setRecipes(dbHelper.getAllRecipes(orderBy));
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncRecipeUpdate extends AsyncTask<Void, Void, Boolean> {
        private int newRecipes, modifiedRecipes;
        private Context context;
        private String orderBy;
        private List<RecipeTO> recipes;
        private RecipesDBHelper dbHelper;

        MyAsyncRecipeUpdate(Context context, List<RecipeTO> recipes, String orderBy) {
            this.context = context;
            this.dbHelper = new RecipesDBHelper(context);
            this.recipes = recipes;
            this.orderBy = orderBy;
            this.newRecipes = 0;
            this.modifiedRecipes = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (RecipeTO item : recipes) {
                //Log.e(getClass().getSimpleName(), "item: " + item.getId());
                if(dbHelper.recipeExists(item.getId())) {
                    //Log.e(getClass().getSimpleName(), "\t exists");
                    dbHelper.updateRecipeServerChanges(item.toEntity());
                    modifiedRecipes++;
                } else {
                    //Log.e(getClass().getSimpleName(), "\t not exists");
                    dbHelper.insertRecipe(item.toEntity());
                    newRecipes++;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                setRecipes(dbHelper.getAllRecipes(orderBy));
                setInfoFromLastFetch(context.getString(R.string.message_from_fetch_recipes, newRecipes, modifiedRecipes));
            }
        }
    }
    //endregion

    //region newCategories
    public LiveData<List<CategoryEntity>> getCategories1() {
        return categoryList;
    }

    private void setCategories(List<CategoryEntity> items) {
        /*categoryList.setValue(items);*/
    }

    public void loadCategories(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            if (DateUtil.shouldUpdateCategories(context)) {
                final String time = DateUtil.getUTCTime();
                APICallsHandler.getAllCategories(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken(), new MyCallback<List<CategoryTO>>() {
                    @Override
                    public void onFinished(List<CategoryTO> result) {
                        //PostRecipeToServerService.startActionPostRecipe(context, new ArrayList<>(result), time);
                        if (result != null) {
                            if (result.isEmpty())
                                loadLocalCategories(context);
                            else {
                                DateUtil.updateCategoriesServerTime(context, time);
                                new MyAsyncCategoriesUpdate(context, result).execute();
                            }
                        } else {
                            setInfoFromLastFetch(context.getString(R.string.error_message_from_fetch_categories));
                        }
                    }
                });
            }
            //don't need to check the newCategories every time
            else
                loadLocalCategories(context);
        }
        //no internet connection
        else {
            setInfoFromLastFetch(context.getString(R.string.no_internet_message));
            loadLocalCategories(context);
        }
    }

    private void loadLocalCategories(final Context context) {
        CategoriesDBHelper dbHelper = new CategoriesDBHelper(context);
        setCategories(dbHelper.getAllCategories());
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncCategoriesUpdate extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private List<CategoryEntity> newCategoriesList;
        private CategoriesDBHelper dbHelper;
        private int newCategories, modifiedCategories;

        MyAsyncCategoriesUpdate(Context context, List<CategoryTO> categories) {
            this.context = context;
            this.dbHelper = new CategoriesDBHelper(context);
            if (categories != null) {
                this.newCategoriesList = new ArrayList<>();
                for (CategoryTO to: categories) {
                    this.newCategoriesList.add(to.toEntity());
                }
            }
            //this.newCategoriesList = categories;
            this.newCategories = 0;
            this.modifiedCategories = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<CategoryEntity> oldCategories = dbHelper.getAllCategories();

            for (CategoryEntity item : newCategoriesList) {
                int oldIndex = oldCategories.indexOf(item);
                if (oldIndex >= 0) {
                    Log.e(getClass().getSimpleName(), "category exists: " + item.toString());
                    if (item.fartherEquals(oldCategories.get(oldIndex))) {
                        Log.e(getClass().getSimpleName(), "exact category exists");
                        continue;
                    } else {
                        Log.e(getClass().getSimpleName(), "need to update, deleting local category.\n" + oldCategories.get(oldIndex).toString());
                        dbHelper.deleteCategory(oldCategories.get(oldIndex));
                        modifiedCategories++;
                    }
                } else
                    newCategories++;

                Log.e(getClass().getSimpleName(), "inserting category: " + item.toString());
                /*if(dbHelper.categoryExists(item.getName()))
                    dbHelper.updateCategoryServerChanges(item);
                else*/
                dbHelper.insertCategory(item);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                setCategories(dbHelper.getAllCategories());
                setInfoFromLastFetch(context.getString(R.string.message_from_fetch_categories, newCategories, modifiedCategories));
            }
        }
    }
    //endregion

    //region favorites

    private void setFavorites(List<RecipeEntity> items) {
        favoriteList.setValue(items);
    }

    public LiveData<List<RecipeEntity>> getFavorites() {
        return favoriteList;
    }

    public void loadLocalFavoritesOrdered(final Context context, String orderBy) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(context);
        setFavorites(dbHelper.getFavoriteRecipes(orderBy));
    }

    public List<CategoryEntity> loadFavoritesCategories(final Context context) {
        CategoriesDBHelper dbHelper = new CategoriesDBHelper(context);
        setCategories(dbHelper.getAllCategories());
        return dbHelper.getAllCategories();
    }


    //endregion
}
