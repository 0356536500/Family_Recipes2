package com.myapps.ron.family_recipes.dal;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.dal.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;

public class StorageWrapper {

    private RecipesDBHelper dbHelper;
    private static StorageWrapper storage;
    //private Context context;

    public static StorageWrapper getInstance(Context context) {
        if(storage == null) {
            storage = new StorageWrapper(context);
            //storage.context = context;
        }

        return storage;
    }

    private StorageWrapper(Context context) {
        dbHelper = new RecipesDBHelper(context);
    }

    public void getFoodFile(Context context, Recipe recipe, MyCallback<String> callback) {
        String path = ExternalStorageHelper.getFileAbsolutePath(context, recipe.getFoodFiles().get(0));
        if(path != null)
            callback.onFinished(path);
        else {
            OnlineStorageWrapper.downloadFile(context, recipe.getFoodFiles().get(0), callback);
        }
    }

    /*public void getAllRecipes(final Context context) {
        final String time = DateUtil.getUTCTime();
        APICallsHandler.getAllRecipes(DateUtil.getLastUpdateTime(context), CognitoHelper.getToken(), new MyCallback<List<Recipe>>() {
            @Override
            public void onFinished(List<Recipe> result) {
                HandleServerDataService.startActionUpdateRecipes(context, new ArrayList<>(result), time);
            }
        });
        dbHelper.getAllRecipes();
    }*/

}
