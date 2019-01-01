package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.dal.RecipeDataSource;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import io.reactivex.Flowable;

/**
 * Created by ronginat on 01/01/2019.
 *
 * Using the Room database as a data source
 */
public class LocalRecipeDataSource implements RecipeDataSource {

    private final RecipeDao mRecipeDao;

    public LocalRecipeDataSource(RecipeDao recipeDao) {
        this.mRecipeDao = recipeDao;
    }

    @Override
    public Flowable<RecipeEntity> getRecipe(String id) {
        return mRecipeDao.getRecipe(id);
    }

    @Override
    public void insetOrUpdateRecipe(RecipeEntity recipeEntity) {
        mRecipeDao.insertRecipe(recipeEntity);
    }

    @Override
    public void deleteAllRecipes() {
        mRecipeDao.deleteAllRecipes();
    }
}
