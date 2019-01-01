package com.myapps.ron.family_recipes.dal;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.persistence.AppDatabases;
import com.myapps.ron.family_recipes.dal.persistence.LocalRecipeDataSource;
import com.myapps.ron.family_recipes.viewmodels.ViewModelFactory;

/**
 * Created by ronginat on 01/01/2019.
 */
public class Injection {

    public static RecipeDataSource provideRecipeDataSource(Context context) {
        AppDatabases database = AppDatabases.getInstance(context);
        return new LocalRecipeDataSource(database.recipeDao());
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        RecipeDataSource dataSource = provideRecipeDataSource(context);
        return new ViewModelFactory(dataSource);
    }
}
