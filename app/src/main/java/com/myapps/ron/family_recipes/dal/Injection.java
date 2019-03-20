package com.myapps.ron.family_recipes.dal;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.persistence.AppDatabases;
import com.myapps.ron.family_recipes.dal.repository.CategoryRepository;
import com.myapps.ron.family_recipes.dal.repository.PendingRecipeRepository;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.viewmodels.ViewModelFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ronginat on 01/01/2019.
 */
public class Injection {

    public static PendingRecipeRepository providePendingRecipeRepository(Context context) {
        AppDatabases database = AppDatabases.getInstance(context);
        return PendingRecipeRepository.getInstance(database.pendingRecipeDao(), getExecutor(1));
    }

    public static RecipeRepository provideRecipeRepository(Context context) {
        AppDatabases database = AppDatabases.getInstance(context);
        return RecipeRepository.getInstance(database.recipeDao(), getExecutor(4));
    }

    private static CategoryRepository provideCategoryRepository(Context context) {
        AppDatabases database = AppDatabases.getInstance(context);
        return CategoryRepository.getInstance(database.categoriesDao(), getExecutor(2));
    }

    private static Executor getExecutor(int poolSize) {
        return Executors.newFixedThreadPool(poolSize);
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        RecipeRepository dataSource1 = provideRecipeRepository(context);
        CategoryRepository dataSource2 = provideCategoryRepository(context);
        PendingRecipeRepository dataSource3 = providePendingRecipeRepository(context);
        return new ViewModelFactory(dataSource1, dataSource2, dataSource3);
    }
}
