package com.myapps.ron.family_recipes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*final String time = "0";
        APICallsHandler.getAllRecipes(time, AppHelper.getAccessToken(), new MyCallback<List<Recipe>>() {
            @Override
            public void onFinished(List<Recipe> result) {
                //PostRecipeToServerService.startActionPostRecipe(context, new ArrayList<>(result), time);
                if(result != null) {
                    Log.e(TAG, "success fetching all recipes. count = " + result.size());
                }
            }
        });*/

        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            Log.e(TAG, locale.toString());
        }
    }
}
