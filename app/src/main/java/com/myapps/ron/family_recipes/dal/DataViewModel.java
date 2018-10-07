package com.myapps.ron.family_recipes.dal;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.List;

/**
 * class for use by MainActivity.
 * loads all recipes, updates local db and server time.
 */
public class DataViewModel extends ViewModel {
    private final MutableLiveData<List<Recipe>> recipeList = new MutableLiveData<>(); // list of recipes from api


    public void setRecipes(List<Recipe> items) {
        recipeList.setValue(items);
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipeList;
    }

    public void loadRecipes(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            final String time = DateUtil.getUTCTime();
            APICallsHandler.getAllRecipes(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken(), new MyCallback<List<Recipe>>() {
                @Override
                public void onFinished(List<Recipe> result) {
                    //HandleServerDataService.startActionUpdateRecipes(context, new ArrayList<>(result), time);
                    if(result != null) {
                        DateUtil.updateServerTime(context, time);
                        new MyAsyncRecipeUpdate(context, result).execute();
                    }
                }
            });
        }
        else {
            RecipesDBHelper dbHelper = new RecipesDBHelper(context);
            setRecipes(dbHelper.getAllRecipes());
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncRecipeUpdate extends AsyncTask<Void, Void, Boolean> {

        private List<Recipe> recipes;
        private RecipesDBHelper dbHelper;

        MyAsyncRecipeUpdate(Context context, List<Recipe> recipes) {
            this.dbHelper = new RecipesDBHelper(context);
            this.recipes = recipes;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (Recipe item : recipes) {
                if(dbHelper.recipeExists(item.getId()))
                    dbHelper.updateRecipe(item);
                else
                    dbHelper.insertRecipe(item);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean)
                setRecipes(dbHelper.getAllRecipes());
        }
    }
}
