package com.ronginat.family_recipes.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.background.services.PostFoodImagesService;
import com.ronginat.family_recipes.background.workers.GetOneRecipeWorker;
import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.layout.MiddleWareForNetwork;
import com.ronginat.family_recipes.layout.cognito.AppHelper;
import com.ronginat.family_recipes.logic.repository.RecipeRepository;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.model.AccessEntity;
import com.ronginat.family_recipes.model.CommentEntity;
import com.ronginat.family_recipes.model.RecipeEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * class for use by RecipeActivity.
 * PatchRecipe methods:
 * - like/unlike
 * - add comment
 */
public class RecipeViewModel extends ViewModel {
    //private final MutableLiveData<RecipeEntity> recipe = new MutableLiveData<>(); // current recipe on the screen
    private final MutableLiveData<List<CommentEntity>> comments = new MutableLiveData<>(); // current recipe on the screen
    private final MutableLiveData<Boolean> isUserLiked = new MutableLiveData<>();
    private MutableLiveData<String> recipeContent = new MutableLiveData<>();
    private MutableLiveData<Uri> imagePath = new MutableLiveData<>();
    private MutableLiveData<String> infoForUser = new MutableLiveData<>();

    private MutableLiveData<RecipeEntity> recipe = new MutableLiveData<>();

    public LiveData<RecipeEntity> getRecipe() {
        return recipe;
    }

    @Nullable
    public ArrayList<String> getFoodFiles() {
        if (recipe.getValue() != null)
            return new ArrayList<>(recipe.getValue().getFoodFiles());
        return null;
    }

    public LiveData<Boolean> isUserLiked() {
        return isUserLiked;
    }

    private void setComments(List<CommentEntity> items) {
        comments.setValue(items);
    }

    public LiveData<List<CommentEntity>> getComments() {
        return comments;
    }

    private void setRecipeContent(String item) {
        recipeContent.setValue(item);
    }

    public LiveData<String> getRecipeContent() {
        return recipeContent;
    }

    private void setImagePath(Uri item) {
        imagePath.setValue(item);
    }

    public LiveData<Uri> getImagePath() {
        return imagePath;
    }

    private void setInfo(String message) {
        infoForUser.setValue(message);
    }

    public LiveData<String> getInfo() {
        return infoForUser;
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RecipeRepository recipeRepository;

    private String recipeId;

    public RecipeViewModel(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
        compositeDisposable.add(this.recipeRepository.dispatchInfoForRecipe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setInfo));
    }

    public void setInitialRecipe(Context context, String recipeId) {
        //this.recipe = initialRecipe;
        //isUserLiked.setValue(initialRecipe.isUserLiked());
        this.recipeId = recipeId;
        compositeDisposable.add(this.recipeRepository.getObservableRecipe(context, recipeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recipeEntity -> {
                    //Log.e(getClass().getSimpleName(), "in recipe observer, " + recipeEntity);
                    //if user like had changed
                    if (this.recipe.getValue() == null || this.recipe.getValue().isUserLiked() != recipeEntity.isUserLiked())
                        this.isUserLiked.setValue(recipeEntity.isUserLiked());

                    this.recipe.setValue(recipeEntity);
                }, error -> {
                    if (error.getMessage() != null)
                        Log.e(getClass().getSimpleName(), error.getMessage());
                    setInfo(context.getString(R.string.recipe_content_not_found));
                }));
    }


    public void loadComments(Context context) {
        compositeDisposable.add(recipeRepository.fetchRecipeComments(context, recipeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setComments, throwable -> setInfo(throwable.getMessage()))
        );
    }

    public void changeLike(final Context context) {
        if (recipe.getValue() != null) {
            Map<String, Object> attrs = new HashMap<>();
            String likeStr = recipe.getValue().isUserLiked() ? "unlike" : "like";
            attrs.put(Constants.LIKES, likeStr);
            compositeDisposable.add(recipeRepository.changeLike(context, recipe.getValue(), attrs)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(message -> {}, throwable -> setInfo(throwable.getMessage()))
            );
        }
    }

    public void postComment(final Context context, String text) {
        if(recipe.getValue() != null) {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(Constants.COMMENT, text);

            compositeDisposable.add(recipeRepository.postComment(context, attrs, recipeId, recipe.getValue().getLastModifiedDate())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> loadComments(context), throwable -> {
                        setComments(null);
                        setInfo(throwable.getMessage());
                    })
            );
        } else {
            setComments(null);
            setInfo(context.getString(R.string.load_error_message));
        }
    }

    public void loadRecipeContent(final Context context) {
        compositeDisposable.add(recipeRepository.getRecipeContentById(context, recipeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(content -> {
                    setRecipeContent(content);
                    updateAccessToRecipeContent(recipeId);
                }));

        /*String recipeFile = null;
        if (recipe.getValue() != null)
            recipeFile = recipe.getValue().getRecipeFile();

        if(recipeFile != null && !recipeFile.equals("\"\"")) {
            StorageWrapper.getRecipeFile(context, recipeFile, path -> {
                //Log.e(getClass().getSimpleName(), "return from getContent");
                if(path != null) {
                    updateAccessToRecipeContent(recipeId);
                    setRecipePath(Constants.FILE_PREFIX + path.getPath());
                }
                else {
                    setInfo(context.getString(R.string.no_internet_message));
                    setRecipePath(null);
                }
            });
        }
        else {
            setInfo(context.getString(R.string.recipe_not_in_server));
            setRecipePath(null);
        }*/
    }

    public void loadRecipeFoodImage(final Context context) {
        List<String> foodFiles = null;
        if (recipe.getValue() != null)
            foodFiles = recipe.getValue().getFoodFiles();
        if (foodFiles != null && foodFiles.size() > 0) {
            StorageWrapper.getFoodFile(context, foodFiles.get(0), this::setImagePath);
        } else {
            setImagePath(null);
        }
    }

    public void postImages(Context context, List<String> imagesPathsToUpload) {
        RecipeEntity recipe = getRecipe().getValue();
        if (recipe != null)
            PostFoodImagesService.startActionPostImages(context, recipe.getId(),
                    recipe.getLastModifiedDate(), imagesPathsToUpload);
    }

    public void refreshRecipeDelayed(Context context) {
        new Handler().postDelayed(() -> {
            if (MiddleWareForNetwork.checkInternetConnection(context) && AppHelper.getAccessToken() != null)
                WorkManager.getInstance(context).enqueue(GetOneRecipeWorker.getOneRecipeWorker(recipeId));
        }, 3000);
    }

    // region Recipe Access

    private void updateAccessToRecipe(String id, String accessKey) {
        recipeRepository.upsertRecipeAccess(id, accessKey, new Date().getTime());
    }

    private void updateAccessToRecipeContent(String id) {
        updateAccessToRecipe(id, AccessEntity.KEY_ACCESSED_CONTENT);
    }

    public void updateAccessToRecipeImages(String id) {
        updateAccessToRecipe(id, AccessEntity.KEY_ACCESSED_IMAGES);
    }

    // endregion

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
