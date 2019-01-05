package com.myapps.ron.family_recipes.viewmodels;

import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;

import java.lang.reflect.InvocationTargetException;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Created by ronginat on 01/01/2019.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final RecipeRepository mDataSource;

    public ViewModelFactory(RecipeRepository recipeRepository) {
        this.mDataSource = recipeRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NewViewModel.class)) {
            try {
                return modelClass.getConstructor(RecipeRepository.class).newInstance(mDataSource);
                //return (T) new NewViewModel(mDataSource);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
        throw new IllegalArgumentException("UnKnown ViewModel class");
    }
}
