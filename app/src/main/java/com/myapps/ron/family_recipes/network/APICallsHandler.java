package com.myapps.ron.family_recipes.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myapps.ron.family_recipes.model.Recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APICallsHandler {

    private static final String TAG = APICallsHandler.class.getSimpleName();
    private static Gson gson = new Gson();
    private static Retrofit retrofit;
    //private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void getOneRecipe(String token) {
        RecipeService service = getRetrofitInstance().create(RecipeService.class);
        Call<Recipe> call = service.getOneRecipe(token);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(@NonNull Call<Recipe> call, @NonNull Response<Recipe> response) {
                Log.i(TAG, response.body().toString());
                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Recipe> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting one recipe");
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void getAllRecipes(String date, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);

        RecipeService service = getRetrofitInstance().create(RecipeService.class);
        Call<List<Recipe>> call = service.getAllRecipes(headers, date);

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                Log.i(TAG, response.body().toString());
                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting all recipes");
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void postRecipe(Recipe recipe, int numOfFiles, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);
        Map<String, String> body = new HashMap<>();
        body.put(Constants.RECIPE_ITEM, gson.toJson(recipe));
        body.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));

        RecipeService service = getRetrofitInstance().create(RecipeService.class);
        Call<JsonObject> call = service.postPendRecipe(headers, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {

                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void patchRecipe(Map<String, String> attributes, int numOfFiles, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);
        Map<String, String> body = new HashMap<>();
        body.put(Constants.ATTRIBUTES, gson.toJson(attributes));
        body.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));

        RecipeService service = getRetrofitInstance().create(RecipeService.class);
        Call<Recipe> call = service.patchRecipe(headers, body);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(@NonNull Call<Recipe> call, @NonNull Response<Recipe> response) {
                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Recipe> call, @NonNull Throwable t) {
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
