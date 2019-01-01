package com.myapps.ron.family_recipes.dal;

import com.myapps.ron.family_recipes.model.RecipeEntity;

import io.reactivex.Flowable;

/**
 * Created by ronginat on 01/01/2019.
 */
public interface RecipeDataSource {

    /**
     * Get the recipe from the data source.
     *
     * @return the recipe from the data source
     */
    Flowable<RecipeEntity> getRecipe(String id);

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
