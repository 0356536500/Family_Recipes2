package com.myapps.ron.family_recipes.viewmodels;

import android.os.CountDownTimer;

import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.dal.repository.RepoSearchResults;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import io.reactivex.Flowable;

/**
 * Created by ronginat on 02/01/2019.
 *
 * ViewModel of {@link com.myapps.ron.family_recipes.TestActivity }
 */
public class NewViewModel extends ViewModel {

    private RecipeRepository repository;

    private MutableLiveData<QueryModel> queryLiveData = new MutableLiveData<>();
    //private final LiveData<PagedList<RecipeMinimal>> data;
    //Applying transformation to get RepoSearchResults for the given Search Query
    private LiveData<RepoSearchResults> repoResults = Transformations.map(queryLiveData,
            input -> repository.search(input));

    //Applying transformation to get Live PagedList<Repo> from the RepoSearchResult
    private LiveData<PagedList<RecipeMinimal>> data = Transformations.switchMap(repoResults,
            RepoSearchResults::getData
    );


    private int id = 0;
    private RecipeEntity mRecipe;

    //@Inject
    public NewViewModel(RecipeRepository recipeRepository) {
        this.repository = recipeRepository;

        new CountDownTimer(10000, 1000) {
            String idStr = "recipe";
            int idInt = 0;

            @Override
            public void onTick(long l) {
                QueryModel filter = new QueryModel(RecipeEntity.KEY_CREATED, "" + idInt++);
                queryLiveData.setValue(filter);
                /*Disposable disposable = recipeRepository.getRecipe(idStr + idInt)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((recipe, throwable) -> {
                            if (recipe != null) {
                                recipe.setName("updateFromViewMode" + idInt);
                                repository.insertOrUpdateRecipe(recipe);
                                idInt++;
                            } else
                                Log.e(getClass().getSimpleName(), "getOneRecipe" + throwable.getMessage());
                        });*/
            }

            @Override
            public void onFinish() {

            }
        };//.start();
    }

    public LiveData<PagedList<RecipeMinimal>> getData() {
        return data;
    }

    private String wrapQueryWithPercent(String query) {
        if (query != null && !"".equals(query))
            return "%" + query + "%";
        return "%";
    }

    /**
     * Search a repository based on a query string.
     */
    public void searchQuery(String query, String orderBy, List<String> categories) {
        queryLiveData.postValue(new QueryModel(
                wrapQueryWithPercent(query),
                orderBy));
    }

    public void fetchRecipesFromServer() {

    }


    /*public void insertRecipe(RecipeEntity recipe) {
        mDataSource.insertOrUpdateRecipe(recipe);
    }*/

    /**
     * Get the recipe name of the recipe.
     *
     * @return a {@link Flowable} that will emit every time the recipe name has been updated.
     */

    /*public Flowable<String> getRecipeName(String id) {
        return mDataSource.getRecipe(id)
                // for every emission of the recipe, get the recipe name
                .map(recipe -> {
                    mRecipe = recipe;
                    Log.e(getClass().getSimpleName(), "in flowable, name = " + recipe.getName());
                    return recipe.getName();
                });
    }


    public Completable updateRecipeName(final String recipeName) {
        return Completable.fromAction(() -> {
            // if there's no recipe, create a new recipe.
            // if we already have a recipe, then, since the recipe object is immutable,
            // create a new recipe, with the id of the previous recipe and the updated recipe name.
            mRecipe = mRecipe == null
                    ? new RecipeEntity.RecipeBuilder()
                    .id("" + id++)
                    .name(recipeName)
                    .build()
                    : new RecipeEntity.RecipeBuilder()
                    .id(mRecipe.getId())
                    .name(recipeName)
                    .build();

            mDataSource.insertOrUpdateRecipe(mRecipe);
        });
    }*/

    @Override
    protected void onCleared() {
        super.onCleared();
        //compositeDisposable.clear();
    }

}
