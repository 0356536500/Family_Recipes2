package com.myapps.ron.family_recipes.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.background.workers.PostRecipeScheduledWorker;
import com.myapps.ron.family_recipes.logic.repository.CategoryRepository;
import com.myapps.ron.family_recipes.logic.repository.PendingRecipeRepository;
import com.myapps.ron.family_recipes.logic.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.PendingRecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeViewModel extends ViewModel {
    private MutableLiveData<String> recipePath = new MutableLiveData<>();
    private LiveData<List<CategoryEntity>> categoryList; // list of categories from local db

    private CompositeDisposable compositeDisposable;
    private final PendingRecipeRepository pendingRecipeRepository;

    public PostRecipeViewModel(CategoryRepository categoryRepository, PendingRecipeRepository pendingRecipeRepository) {
        this.categoryList = categoryRepository.getAllCategoriesLiveData();
        this.pendingRecipeRepository = pendingRecipeRepository;
        this.compositeDisposable = new CompositeDisposable();
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

    public void postRecipe() {
        this.compositeDisposable.add(
                this.pendingRecipeRepository
                        .insertOrUpdatePendingRecipe(new PendingRecipeEntity(this.recipe))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::initWorker, error -> Log.e(getClass().getSimpleName(), error.getMessage()))
        );
    }

    private void initWorker() {
        this.compositeDisposable.clear();
        WorkManager.getInstance().enqueue(PostRecipeScheduledWorker.createPostRecipesWorker());
    }

    @Override
    protected void onCleared() {
        this.compositeDisposable.clear();
        super.onCleared();
    }
}
