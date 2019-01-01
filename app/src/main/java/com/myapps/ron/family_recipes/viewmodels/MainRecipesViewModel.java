package com.myapps.ron.family_recipes.viewmodels;

import android.util.Log;

import com.myapps.ron.family_recipes.dal.RecipeDataSource;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import androidx.lifecycle.ViewModel;
import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Created by ronginat on 01/01/2019.
 */
public class MainRecipesViewModel extends ViewModel {

    private int id = 0;
    private final RecipeDataSource mDataSource;

    private RecipeEntity mRecipe;

    public MainRecipesViewModel(RecipeDataSource dataSource) {
        this.mDataSource = dataSource;
    }

    /**
     * Get the recipe name of the recipe.
     *
     * @return a {@link Flowable} that will emit every time the recipe name has been updated.
     */

    public Flowable<String> getRecipeName(String id) {
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

            mDataSource.insetOrUpdateRecipe(mRecipe);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
