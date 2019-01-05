package com.myapps.ron.family_recipes.dal;

import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Created by ronginat on 01/01/2019.
 */
public interface RecipeDataSource<T> {

    /**
     * Get the recipe from the data source.
     *
     * @return the recipe from the data source
     */
    Single<RecipeEntity> getRecipe(String id);


    Flowable<List<T>> getAllRecipes();

    /**
     * Insert the recipe  into the data source, or, if this is an existing recipe, updates it.
     *
     * @param recipeEntity the recipe to be inserted or updated
     */
    void insetOrUpdateRecipe(RecipeEntity recipeEntity);

    /**
     * Deletes all recipes from the data source.
     */
    void deleteAllRecipes();
}
