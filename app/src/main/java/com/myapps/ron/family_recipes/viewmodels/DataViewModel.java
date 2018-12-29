package com.myapps.ron.family_recipes.viewmodels;

import android.annotation.SuppressLint;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.db.CategoriesDBHelper;
import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.List;

/**
 * class for use by MainActivity.
 * loads all recipes, updates local db and server time.
 */
public class DataViewModel extends ViewModel {
    private MutableLiveData<List<Recipe>> recipeList = new MutableLiveData<>(); // list of recipes from api
    private MutableLiveData<List<Recipe>> favoriteList = new MutableLiveData<>(); // list of recipes local db
    private MutableLiveData<List<Category>> categoryList = new MutableLiveData<>(); // list of newCategories from api
    private MutableLiveData<Boolean> canInitBothRecyclerAndFilters = new MutableLiveData<>();

    private MutableLiveData<String> infoFromLastFetch = new MutableLiveData<>(); // info about new or modified data from last fetch from api

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
    private void setRecipes(List<Recipe> items) {
        recipeList.setValue(items);
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipeList;
    }

    public void loadRecipes(final Context context, final String orderBy) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            final String time = DateUtil.getUTCTime();
            APICallsHandler.getAllRecipes(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken(), new MyCallback<List<Recipe>>() {
                @Override
                public void onFinished(List<Recipe> result) {
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

    public List<Recipe> loadLocalRecipesOrdered(final Context context, String orderBy) {
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
        private List<Recipe> recipes;
        private RecipesDBHelper dbHelper;

        MyAsyncRecipeUpdate(Context context, List<Recipe> recipes, String orderBy) {
            this.context = context;
            this.dbHelper = new RecipesDBHelper(context);
            this.recipes = recipes;
            this.orderBy = orderBy;
            this.newRecipes = 0;
            this.modifiedRecipes = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (Recipe item : recipes) {
                //Log.e(getClass().getSimpleName(), "item: " + item.getId());
                if(dbHelper.recipeExists(item.getId())) {
                    //Log.e(getClass().getSimpleName(), "\t exists");
                    dbHelper.updateRecipeServerChanges(item);
                    modifiedRecipes++;
                } else {
                    //Log.e(getClass().getSimpleName(), "\t not exists");
                    dbHelper.insertRecipe(item);
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
    public LiveData<List<Category>> getCategories() {
        return categoryList;
    }

    private void setCategories(List<Category> items) {
        categoryList.setValue(items);
    }

    public void loadCategories(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            if (DateUtil.shouldUpdateCategories(context)) {
                final String time = DateUtil.getUTCTime();
                APICallsHandler.getAllCategories(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken(), new MyCallback<List<Category>>() {
                    @Override
                    public void onFinished(List<Category> result) {
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
        private List<Category> newCategoriesList;
        private CategoriesDBHelper dbHelper;
        private int newCategories, modifiedCategories;

        MyAsyncCategoriesUpdate(Context context, List<Category> categories) {
            this.context = context;
            this.dbHelper = new CategoriesDBHelper(context);
            this.newCategoriesList = categories;
            this.newCategories = 0;
            this.modifiedCategories = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<Category> oldCategories = dbHelper.getAllCategories();

            for (Category item : newCategoriesList) {
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

    private void setFavorites(List<Recipe> items) {
        favoriteList.setValue(items);
    }

    public LiveData<List<Recipe>> getFavorites() {
        return favoriteList;
    }

    public void loadLocalFavoritesOrdered(final Context context, String orderBy) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(context);
        setFavorites(dbHelper.getFavoriteRecipes(orderBy));
    }

    public List<Category> loadFavoritesCategories(final Context context) {
        CategoriesDBHelper dbHelper = new CategoriesDBHelper(context);
        setCategories(dbHelper.getAllCategories());
        return dbHelper.getAllCategories();
    }


    //endregion
}
