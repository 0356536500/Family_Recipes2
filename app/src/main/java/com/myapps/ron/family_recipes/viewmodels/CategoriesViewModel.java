package com.myapps.ron.family_recipes.viewmodels;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.repository.CategoryRepository;
import com.myapps.ron.family_recipes.model.CategoryEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by ronginat on 14/01/2019.
 */
public class CategoriesViewModel extends ViewModel {
    public LiveData<List<CategoryEntity>> categories;
    private final CategoryRepository repository;

    public CategoriesViewModel(CategoryRepository categoryRepository) {
        this.repository = categoryRepository;

        categories = this.repository.getAllCategories();
    }

    public void loadCategories(Context context) {
        repository.fetchCategoriesReactive(context);
    }
}
