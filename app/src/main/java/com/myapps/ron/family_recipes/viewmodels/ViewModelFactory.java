package com.myapps.ron.family_recipes.viewmodels;

import com.myapps.ron.family_recipes.dal.RecipeDataSource;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Created by ronginat on 01/01/2019.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final RecipeDataSource mDataSource;

    public ViewModelFactory(RecipeDataSource dataSource) {
        this.mDataSource = dataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainRecipesViewModel.class)) {
            return (T) new MainRecipesViewModel(mDataSource);
        }
        throw new IllegalArgumentException("UnKnown ViewModel class");
    }
}
