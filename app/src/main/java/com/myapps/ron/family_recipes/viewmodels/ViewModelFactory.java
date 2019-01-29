package com.myapps.ron.family_recipes.viewmodels;

import com.myapps.ron.family_recipes.dal.repository.CategoryRepository;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;

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

    /*public ViewModelFactory(RecipeRepository recipeRepository) {
        this.mDataSource1 = recipeRepository;
    }*/

    public ViewModelFactory(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.mDataSource1 = recipeRepository;
        this.mDataSource2 = categoryRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RecipeViewModel.class)) {
            try {
                return modelClass.getConstructor(RecipeRepository.class).newInstance(mDataSource1);
                //return (T) new NewViewModel(mDataSource1);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else if (modelClass.isAssignableFrom(CategoriesViewModel.class)) {
            try {
                return modelClass.getConstructor(CategoryRepository.class).newInstance(mDataSource2);
                //return (T) new NewViewModel(mDataSource1);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else if (modelClass.isAssignableFrom(DataViewModel.class)) {
            try {
                return modelClass.getConstructor(RecipeRepository.class, CategoryRepository.class).newInstance(mDataSource1, mDataSource2);
                //return (T) new NewViewModel(mDataSource1);
            } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }

        throw new IllegalArgumentException("UnKnown ViewModel class");
    }
}
