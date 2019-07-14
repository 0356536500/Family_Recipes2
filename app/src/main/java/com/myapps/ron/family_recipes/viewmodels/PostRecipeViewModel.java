package com.myapps.ron.family_recipes.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.myapps.ron.family_recipes.background.workers.BeginContinuationWorker;
import com.myapps.ron.family_recipes.logic.repository.CategoryRepository;
import com.myapps.ron.family_recipes.logic.repository.PendingRecipeRepository;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.PendingRecipeEntity;
import com.myapps.ron.family_recipes.utils.logic.CrashLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeViewModel extends ViewModel {
    private LiveData<List<CategoryEntity>> categoryList; // list of categories from local db

    private CompositeDisposable compositeDisposable;
    private final PendingRecipeRepository pendingRecipeRepository;

    public PostRecipeViewModel(CategoryRepository categoryRepository, PendingRecipeRepository pendingRecipeRepository) {
        this.categoryList = categoryRepository.getAllCategoriesLiveData();
        this.pendingRecipeRepository = pendingRecipeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    //private List<Uri> imagesUris = new ArrayList<>();
    public PendingRecipeEntity recipe = new PendingRecipeEntity();

    public LiveData<List<CategoryEntity>> getCategories() {
        return categoryList;
    }

    public void setRecipeContent(String html) {
        //recipeFile = StorageWrapper.createHtmlFile(context,recipe.getName().concat(".html"), html);
        //if (recipeFile != null)
        recipe.setContent(html);
    }

    public void setImagesUris(List<String> imagesUris) {
        if (imagesUris != null) {
            List<String> paths = new ArrayList<>(imagesUris);
            recipe.setFoodFiles(paths);
        }
    }

    public void postRecipe(Context context) {
        this.compositeDisposable.add(
                this.pendingRecipeRepository
                        .insertOrUpdatePendingRecipe(this.recipe)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> initWorker(context), CrashLogger::logException)
        );
    }

    private void initWorker(Context context) {
        this.compositeDisposable.clear();
        BeginContinuationWorker.enqueueWorkContinuationWithValidSession(context);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.compositeDisposable.clear();
    }
}
