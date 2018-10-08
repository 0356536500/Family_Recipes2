package com.myapps.ron.family_recipes.dal;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * class for use by RecipeActivity.
 * PatchRecipe methods:
 * - like/unlike
 * - add comment
 */
public class RecipeViewModel extends ViewModel {
    private final MutableLiveData<Recipe> recipe = new MutableLiveData<>(); // current recipe on the screen

    public void setRecipe(Recipe item) {
        recipe.setValue(item);
    }

    public LiveData<Recipe> getRecipe() {
        return recipe;
    }

    public void changeLike(final Context context, final Recipe recipe) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            Map<String, String> attrs = new HashMap<>();
            String likeStr = recipe.getMeLike() ? "unlike" : "like";
            attrs.put(Constants.LIKES, likeStr);
            APICallsHandler.patchRecipe(attrs, recipe.getId(), AppHelper.getAccessToken(), new MyCallback<Recipe>() {
                @Override
                public void onFinished(Recipe result) {
                    result.setMeLike(!recipe.getMeLike());
                    RecipesDBHelper dbHelper = new RecipesDBHelper(context);
                    dbHelper.updateRecipe(result);
                    Log.e("viewModel", dbHelper.getRecipe(recipe.getId()).toString());
                    setRecipe(dbHelper.getRecipe(recipe.getId()));
                }
            });
        }
        else {
            Toast.makeText(context, "no Internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}
