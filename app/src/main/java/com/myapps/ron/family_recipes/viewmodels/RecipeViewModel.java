package com.myapps.ron.family_recipes.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CommentEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.modelTO.CommentTO;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.utils.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private MutableLiveData<String> imagePath = new MutableLiveData<>();
    private MutableLiveData<Integer> infoForUser = new MutableLiveData<>();

    /*private void setRecipe(RecipeEntity item) {
        recipe.setValue(item);
    }*/

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

    private void setImagePath(String item) {
        imagePath.setValue(item);
    }

    public LiveData<String> getImagePath() {
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
    private RecipeEntity recipe;

    public RecipeViewModel(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public void setInitialRecipe(RecipeEntity initialRecipe) {
        this.recipe = initialRecipe;
        this.recipeId = initialRecipe.getId();
        isUserLiked.setValue(initialRecipe.isUserLiked());
        Disposable disposable = this.recipeRepository.getObservableRecipe(recipe.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(recipeEntity -> {
                    Log.e(getClass().getSimpleName(), "in recipe observer, " + recipeEntity);
                    //if user like had changed
                    if (this.recipe.isUserLiked() != recipeEntity.isUserLiked())
                        this.isUserLiked.setValue(recipeEntity.isUserLiked());

                    this.recipe = recipeEntity;
                }, error -> Log.e(getClass().getSimpleName(), error.getMessage()));
        compositeDisposable.add(disposable);
    }


    public void loadComments(Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            APICallsHandler.getRecipeComments(recipe.getId(), AppHelper.getAccessToken(), results -> {
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
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            Map<String, Object> attrs = new HashMap<>();
            String likeStr = recipe.isUserLiked() ? "unlike" : "like";
            attrs.put(Constants.LIKES, likeStr);
            APICallsHandler.patchRecipe(attrs, recipe.getId(), AppHelper.getAccessToken(), result -> {
                if (result) // status 200
                    recipeRepository.changeLike(recipeId, !recipe.isUserLiked());
                else // status <> 200
                    setInfo(R.string.load_error_message);
            });
        }
        else {
            setInfo(R.string.no_internet_message);
        }
    }

    public void postComment(final Context context, String text) {
        if (!"".equals(text)) {
            if(MiddleWareForNetwork.checkInternetConnection(context)) {
                Map<String, Object> attrs = new HashMap<>();
                Map<String, String> commentMap = new HashMap<>();
                commentMap.put(Constants.COMMENT_MESSAGE, text);
                commentMap.put(Constants.COMMENT_USER, AppHelper.getCurrUser());
                attrs.put(Constants.COMMENTS, commentMap);
                Log.e("viewModel", "before posting comment:\n" + attrs);
                APICallsHandler.patchRecipe(attrs, recipe.getId(), AppHelper.getAccessToken(), result -> {
                    if (result) // status 200
                        loadComments(context);
                    else // status <> 200
                        setInfo(R.string.load_error_message);
                });
            } else {
                setInfo(R.string.no_internet_message);
            }
        } else {
            setInfo(R.string.post_comment_error);
        }
    }

    public void loadRecipeContent(final Context context) {
            if(recipe.getRecipeFile() != null && !recipe.getRecipeFile().equals("\"\"")) {
                StorageWrapper.getRecipeFile(context, recipe.getRecipeFile(), new MyCallback<String>() {
                    @Override
                    public void onFinished(String path) {
                        Log.e(getClass().getSimpleName(), "return from getRecipeFile");
                        if(path != null) {
                            Log.e(getClass().getSimpleName(), "path != null");
                            File file = new File(path);
                            if (file.exists()) {
                                Log.e(getClass().getSimpleName(), "file exists");
                                setRecipePath(Constants.FILE_PREFIX + file.getAbsolutePath());
                            }
                        }
                        else {
                            setInfo(R.string.no_internet_message);
                            setRecipePath(null);
                        }
                    }
                });
            }
            else {
                setInfo(R.string.recipe_not_in_server);
                setRecipePath(null);
            }
    }

    public void loadRecipeFoodImage(final Context context) {
        if (recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > 0) {
            StorageWrapper.getFoodFile(context, recipe.getFoodFiles().get(0), this::setImagePath);
        } else {
            setImagePath(null);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
