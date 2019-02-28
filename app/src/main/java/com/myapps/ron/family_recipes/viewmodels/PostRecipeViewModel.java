package com.myapps.ron.family_recipes.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;

import com.myapps.ron.family_recipes.dal.repository.CategoryRepository;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeViewModel extends ViewModel {
    private MutableLiveData<String> recipePath = new MutableLiveData<>();
    private LiveData<List<CategoryEntity>> categoryList;// = new MutableLiveData<>(); // list of categories from local db

    //private CategoryRepository categoryRepository;

    public PostRecipeViewModel(CategoryRepository categoryRepository) {
        //this.categoryRepository = categoryRepository;
        this.categoryList = categoryRepository.getAllCategoriesLiveData();
    }

    private File recipeFile;
    //private List<Uri> imagesUris = new ArrayList<>();
    public RecipeEntity recipe = new RecipeEntity();

    private void setRecipePath(String item) {
        recipePath.setValue(item);
    }

    public LiveData<String> getRecipePath() {
        return recipePath;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categoryList;
    }

    /*private void setCategories(List<CategoryEntity> items) {
        categoryList.setValue(items);
    }*/


    /*public void loadCategories(final Context context) {
        setCategories(categoryRepository.getAllCategories());
    }*/

    public File getRecipeFile() {
        return recipeFile;
    }

    public void setRecipeFile(Context context, String html) {
        recipeFile = StorageWrapper.createHtmlFile(context,recipe.getName().concat(".html"), html);
        if (recipeFile != null)
            recipe.setRecipeFile(recipeFile.getAbsolutePath());
    }

    public void setImagesUris(List<String> imagesUris) {
        if (imagesUris != null) {
            List<String> paths = new ArrayList<>(imagesUris);
            /*if (!imagesUris.isEmpty()) {
                for (Uri uri : imagesUris)
                    paths.add(uri.getPath());
            }*/
            recipe.setFoodFiles(paths);
        }
    }

    /*public boolean checkNameValid(final Context context, String name) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(context);
        return true;
    }*/
}
