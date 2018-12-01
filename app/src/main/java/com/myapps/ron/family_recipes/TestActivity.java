package com.myapps.ron.family_recipes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;

import java.util.List;

public class TestActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String time = "0";
        APICallsHandler.getAllRecipes(time, AppHelper.getAccessToken(), new MyCallback<List<Recipe>>() {
            @Override
            public void onFinished(List<Recipe> result) {
                //PostRecipeToServerService.startActionPostRecipe(context, new ArrayList<>(result), time);
                if(result != null) {
                    Log.e(TAG, "success fetching all recipes. count = " + result.size());
                }
            }
        });
    }
}
