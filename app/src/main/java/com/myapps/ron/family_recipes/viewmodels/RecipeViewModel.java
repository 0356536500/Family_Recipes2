package com.myapps.ron.family_recipes.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.AccessEntity;
import com.myapps.ron.family_recipes.model.CommentEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.network.modelTO.CommentTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.myapps.ron.family_recipes.utils.Constants.FALSE;
import static com.myapps.ron.family_recipes.utils.Constants.TRUE;

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
    private MutableLiveData<String> recipePath = new MutableLiveData<>();
    private MutableLiveData<Uri> imagePath = new MutableLiveData<>();
    private MutableLiveData<Integer> infoForUser = new MutableLiveData<>();

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

    private void setRecipePath(String item) {
        recipePath.setValue(item);
    }

    public LiveData<String> getRecipePath() {
        return recipePath;
    }

    private void setImagePath(Uri item) {
        imagePath.setValue(item);
    }

    public LiveData<Uri> getImagePath() {
        return imagePath;
    }

    private void setInfo(int item) {
        infoForUser.setValue(item);
    }

    public LiveData<Integer> getInfo() {
        return infoForUser;
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RecipeRepository recipeRepository;

    private String recipeId;

    public RecipeViewModel(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public void setInitialRecipe(String recipeId) {
        //this.recipe = initialRecipe;
        //isUserLiked.setValue(initialRecipe.isUserLiked());
        this.recipeId = recipeId;
        compositeDisposable.add(this.recipeRepository.getObservableRecipe(recipeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recipeEntity -> {
                    //Log.e(getClass().getSimpleName(), "in recipe observer, " + recipeEntity);
                    //if user like had changed
                    if (this.recipe.getValue() == null || this.recipe.getValue().isUserLiked() != recipeEntity.isUserLiked())
                        this.isUserLiked.setValue(recipeEntity.isUserLiked());

                    this.recipe.setValue(recipeEntity);
                }, error -> {
                    Log.e(getClass().getSimpleName(), error.getMessage());
                    setInfo(R.string.recipe_content_not_found);
                }));
    }


    public void loadComments(Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            APICallsHandler.getRecipeComments(recipeId, AppHelper.getAccessToken(), results -> {
                if(results != null) {
                    List<CommentEntity> rv = new ArrayList<>();
                    for (CommentTO to: results) {
                        rv.add(to.toEntity());
                    }
                    setComments(rv);
                } else {
                    setComments(null);
                    setInfo(R.string.load_error_message);
                }
            });
        }
        else {
            //setRecipe(recipeEntity);
            setInfo(R.string.no_internet_message);
        }
    }

    public void changeLike(final Context context) {
        if (recipe.getValue() != null) {
            Map<String, Object> attrs = new HashMap<>();
            String likeStr = recipe.getValue().isUserLiked() ? "unlike" : "like";
            attrs.put(Constants.LIKES, likeStr);
            recipeRepository.changeLike(context, recipe.getValue(), attrs)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<Integer>() {
                        @Override
                        public void onSuccess(Integer status) {
                            if (status != -1) {
                                setInfo(status);
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            dispose();
                        }
                    });
        }
        /*if(MiddleWareForNetwork.checkInternetConnection(context) && recipe.getValue() != null) {
            Map<String, Object> attrs = new HashMap<>();
            String likeStr = recipe.getValue().isUserLiked() ? "unlike" : "like";
            attrs.put(Constants.LIKES, likeStr);
            APICallsHandler.patchRecipe(attrs, recipeId, recipe.getValue().getLastModifiedDate(), AppHelper.getAccessToken(), result -> {
                if (result != null && recipeId.equals(result.getId())) { // status 200
                    RecipeEntity update = result.toEntity();
                    update.setMeLike(!recipe.getValue().isUserLiked() ? TRUE : FALSE);
                    recipeRepository.updateRecipe(update);
                }
                else // status <> 200
                    setInfo(R.string.load_error_message);
            });
        }
        else {
            setInfo(R.string.no_internet_message);
        }*/
    }

    public void postComment(final Context context, String text) {
        if(MiddleWareForNetwork.checkInternetConnection(context) && recipe.getValue() != null) {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(Constants.COMMENT, text);
            //Log.e("viewModel", "before posting comment:\n" + attrs);
            APICallsHandler.postComment(attrs, recipeId, recipe.getValue().getLastModifiedDate(), AppHelper.getAccessToken(), result -> {
                if (result) // status 200
                    loadComments(context);
                else { // status <> 200
                    setComments(null);
                    setInfo(R.string.load_error_message);
                }
            });
        } else {
            setComments(null);
            setInfo(R.string.no_internet_message);
        }
    }

    public void loadRecipeContent(final Context context) {
        String recipeFile = null;
        if (recipe.getValue() != null)
            recipeFile = recipe.getValue().getRecipeFile();

        if(recipeFile != null && !recipeFile.equals("\"\"")) {
            StorageWrapper.getRecipeFile(context, recipeFile, path -> {
                //Log.e(getClass().getSimpleName(), "return from getRecipeFile");
                if(path != null) {
                    updateAccessToRecipeContent(recipeId);
                    setRecipePath(Constants.FILE_PREFIX + path.getPath());
                }
                else {
                    setInfo(R.string.no_internet_message);
                    setRecipePath(null);
                }
            });
        }
        else {
            setInfo(R.string.recipe_not_in_server);
            setRecipePath(null);
        }
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

    // region Recipe Access

    private void updateAccessToRecipe(String id, String accessKey) {
        recipeRepository.upsertRecipeAccess(id, accessKey, new Date().getTime());
    }

    private void updateAccessToRecipeContent(String id) {
        updateAccessToRecipe(id, AccessEntity.KEY_ACCESSED_RECIPE);
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
