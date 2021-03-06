package com.ronginat.family_recipes.viewmodels;

import com.ronginat.family_recipes.logic.repository.AppRepository;
import com.ronginat.family_recipes.logic.repository.CategoryRepository;
import com.ronginat.family_recipes.logic.repository.PendingRecipeRepository;
import com.ronginat.family_recipes.logic.repository.RecipeRepository;

import java.lang.reflect.InvocationTargetException;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Created by ronginat on 01/01/2019.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final RecipeRepository mDataSource1;
    private final CategoryRepository mDataSource2;
    private final PendingRecipeRepository mDataSource3;

    public ViewModelFactory(RecipeRepository recipeRepository, CategoryRepository categoryRepository, PendingRecipeRepository pendingRecipeRepository) {
        this.mDataSource1 = recipeRepository;
        this.mDataSource2 = categoryRepository;
        this.mDataSource3 = pendingRecipeRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RecipeViewModel.class)) {
            try {
                return modelClass.getConstructor(RecipeRepository.class).newInstance(mDataSource1);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else if (modelClass.isAssignableFrom(PostRecipeViewModel.class)) {
            try {
                return modelClass.getConstructor(CategoryRepository.class, PendingRecipeRepository.class).newInstance(mDataSource2, mDataSource3);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else if (modelClass.isAssignableFrom(DataViewModel.class)) {
            try {
                return modelClass.getConstructor(RecipeRepository.class, CategoryRepository.class).newInstance(mDataSource1, mDataSource2);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            try {
                return modelClass.getConstructor(AppRepository.class).newInstance(AppRepository.getInstance());
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }

        throw new IllegalArgumentException("UnKnown ViewModel class");
    }
}
