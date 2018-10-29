package com.myapps.ron.family_recipes.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.myapps.ron.family_recipes.dal.db.CategoriesDBHelper;
import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;

import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class CreateRecipeViewModel extends ViewModel {
    private MutableLiveData<String> recipePath = new MutableLiveData<>();
    private MutableLiveData<List<Category>> categoryList = new MutableLiveData<>(); // list of categories from local db

    public Recipe recipe = new Recipe();

    private void setRecipePath(String item) {
        recipePath.setValue(item);
    }

    public LiveData<String> getRecipePath() {
        return recipePath;
    }

    public LiveData<List<Category>> getCategories() {
        return categoryList;
    }

    private void setCategories(List<Category> items) {
        categoryList.setValue(items);
    }


    public void loadCategories(final Context context) {
        CategoriesDBHelper dbHelper = new CategoriesDBHelper(context);
        setCategories(dbHelper.getAllCategories());
    }


    /*public boolean checkNameValid(final Context context, String name) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(context);
        return true;
    }*/
}
