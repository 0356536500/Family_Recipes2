package com.myapps.ron.family_recipes.dal;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.persistence.AppDatabases;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.viewmodels.ViewModelFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ronginat on 01/01/2019.
 */
public class Injection {

    /*public static RecipeDataSource provideRecipeDataSource(Context context) {
        AppDatabases database = AppDatabases.getInstance(context);
        return new LocalRecipeDataSource(database.recipeDao());
    }*/

    public static RecipeRepository provideRecipeRepository(Context context) {
        AppDatabases database = AppDatabases.getInstance(context);
        return new RecipeRepository(database.recipeDao(), getExecutor(2));
    }

    private static Executor getExecutor(int poolSize) {
        return Executors.newScheduledThreadPool(poolSize);
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        RecipeRepository dataSource = provideRecipeRepository(context);
        return new ViewModelFactory(dataSource);
    }
}
