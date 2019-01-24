package com.myapps.ron.family_recipes.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CommentEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.modelTO.CommentTO;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
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
    private final MutableLiveData<RecipeEntity> recipe = new MutableLiveData<>(); // current recipe on the screen
    private final MutableLiveData<List<CommentEntity>> comments = new MutableLiveData<>(); // current recipe on the screen
    private MutableLiveData<String> recipePath = new MutableLiveData<>();
    private MutableLiveData<String> imagePath = new MutableLiveData<>();
    private MutableLiveData<String> infoForUser = new MutableLiveData<>();

    private void setRecipe(RecipeEntity item) {
        recipe.setValue(item);
    }

    public LiveData<RecipeEntity> getRecipe() {
        return recipe;
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

    private void setInfo(String item) {
        infoForUser.setValue(item);
    }

    public LiveData<String> getInfo() {
        return infoForUser;
    }


    public void loadComments(Context context, RecipeEntity recipeEntity) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            APICallsHandler.getOneRecipe(recipeEntity.getId(), AppHelper.getAccessToken(), results -> {
                if(results != null) {
                    List<CommentEntity> rv = new ArrayList<>();
                    for (CommentTO to: results) {
                        rv.add(to.toEntity());
                    }
                    setComments(rv);
                } else {
                    setComments(null);
                    setInfo(context.getString(R.string.load_error_message));
                }
            });
        }
        else {
            setRecipe(recipeEntity);
            setInfo(context.getString(R.string.no_internet_message));
        }
    }

    public void changeLike(final Context context, final RecipeEntity recipe) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            Map<String, Object> attrs = new HashMap<>();
            String likeStr = recipe.isUserLiked() ? "unlike" : "like";
            attrs.put(Constants.LIKES, likeStr);
            APICallsHandler.patchRecipe(attrs, recipe.getId(), AppHelper.getAccessToken(), new MyCallback<RecipeTO>() {
                @Override
                public void onFinished(RecipeTO result) {
                    RecipeEntity rv = result.toEntity();
                    rv.setMeLike(!recipe.isUserLiked() ? TRUE : FALSE);
                    /*RecipesDBHelper dbHelper = new RecipesDBHelper(context);
                    dbHelper.updateRecipeUserChanges(rv);
                    Log.e("viewModel", dbHelper.getRecipe(recipe.getId()).toString());
                    setRecipe(dbHelper.getRecipe(recipe.getId()));*/
                }
            });
        }
        else {
            setInfo(context.getString(R.string.no_internet_message));
        }
    }

    public void postComment(final Context context, final RecipeEntity recipe, String text) {
        if (!"".equals(text)) {
            if(MiddleWareForNetwork.checkInternetConnection(context)) {
                Map<String, Object> attrs = new HashMap<>();
                Map<String, String> commentMap = new HashMap<>();
                commentMap.put(Constants.COMMENT_MESSAGE, text);
                commentMap.put(Constants.COMMENT_USER, AppHelper.getCurrUser());
                attrs.put(Constants.COMMENTS, commentMap);
                Log.e("viewModel", "before posting comment:\n" + attrs);
                APICallsHandler.patchRecipe(attrs, recipe.getId(), AppHelper.getAccessToken(), new MyCallback<RecipeTO>() {
                    @Override
                    public void onFinished(RecipeTO result) {
                        RecipeEntity rv = result.toEntity();
                        rv.setMeLike(recipe.isUserLiked() ? TRUE : FALSE);
                        /*RecipesDBHelper dbHelper = new RecipesDBHelper(context);
                        dbHelper.updateRecipeUserChanges(rv);
                        Log.e("viewModel", dbHelper.getRecipe(recipe.getId()).toString());
                        setRecipe(dbHelper.getRecipe(recipe.getId()));
                        setInfo(context.getString(R.string.post_comment_succeeded));*/
                    }
                });
            } else {
                setInfo(context.getString(R.string.no_internet_message));
            }
        } else {
            setInfo(context.getString(R.string.post_comment_error));
        }
    }

    public void loadRecipeContent(final Context context, final RecipeEntity recipe) {
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
                            setInfo(context.getString(R.string.no_internet_message));
                            setRecipePath(null);
                        }
                    }
                });
            }
            else {
                setInfo(context.getString(R.string.recipe_not_in_server));
                setRecipePath(null);
            }
    }

    public void loadRecipeFoodImage(final Context context, final RecipeEntity recipe) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            if (recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > 0) {
                StorageWrapper.getFoodFile(context, recipe.getFoodFiles().get(0), new MyCallback<String>() {
                    @Override
                    public void onFinished(String path) {
                        setImagePath(path);
                    }
                });
            }
        } else {
            setImagePath(null);
        }
    }
}
