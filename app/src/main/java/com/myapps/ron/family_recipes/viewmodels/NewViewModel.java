package com.myapps.ron.family_recipes.viewmodels;

import android.content.Context;
import android.os.CountDownTimer;

import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.dal.repository.RepoSearchResults;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.services.GetAllRecipesService;

import androidx.annotation.NonNull;
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
            input -> repository.query(input));

    //Applying transformation to get Live PagedList<Repo> from the RepoSearchResult
    private LiveData<PagedList<RecipeMinimal>> data = Transformations.switchMap(repoResults,
            RepoSearchResults::getData
    );


    private int id = 0;
    private RecipeEntity mRecipe;

    //@Inject
    public NewViewModel(RecipeRepository recipeRepository) {
        this.repository = recipeRepository;
    }

    public LiveData<PagedList<RecipeMinimal>> getData() {
        return data;
    }


    public void applyQuery(@NonNull QueryModel queryModel) {
        queryLiveData.postValue(queryModel);
    }

    public void fetchRecipesFromServerJustedLoggedIn(Context context) {
        GetAllRecipesService.startActionGetAllRecipes(context);
    }

    public void fetchRecipesFromServer(Context context) {
        repository.fetchRecipesReactive(context);
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
