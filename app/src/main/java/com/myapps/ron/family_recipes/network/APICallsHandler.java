package com.myapps.ron.family_recipes.network;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;

import java.io.IOException;
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

    private static final int STATUS_OK = 200;
    private static final int STATUS_NOT_MODIFIED = 304;
    //private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void getOneRecipe(String token) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Recipe> call = service.getOneRecipe(token);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(@NonNull Call<Recipe> call, @NonNull Response<Recipe> response) {
                String body = response.body() != null ? response.body().toString() : "null";
                Log.i(TAG, body);
                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Recipe> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting one recipe");
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void getAllRecipes(String date, String token, final MyCallback<List<Recipe>> callback) {
        /*Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);*/

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<Recipe>> call = service.getAllRecipes(token, date);

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                String body = response.body() != null ? response.body().toString() : "null";

                if (response.code() == STATUS_OK) {
                    Log.i(TAG, body);
                    List<Recipe> list = response.body();
                    callback.onFinished(list);
                }
                /*else if (response.code() == STATUS_NOT_MODIFIED) {
                    Log.i(TAG, "not modified");
                    callback.onFinished(null);
                }*/
                else {
                    Log.e(TAG, "error getAllRecipes, code = " + response.code() + "\n body: " + body);
                    callback.onFinished(null);
                }

                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting all recipes. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void postRecipe(final Recipe recipe, final String token, final MyCallback<String> callback) {
        Log.e(TAG, "start post recipe");

        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);

        Map<String, Object> body = new HashMap<>();
        //body.put(Constants.RECIPE_ITEM, gson.toJson(recipe));
        body.put(Constants.RECIPE_ITEM, generatePostRecipeFields(recipe));
        //body.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));

        Log.e(TAG, "post body: \n" + body.toString());
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<JsonObject> call = service.postPendRecipe(headers, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                String url = "null";
                if (response.body() != null && response.body().get("url") != null)
                    url = response.body().get("url").getAsString();

                Log.i(TAG, "response code = " + response.code() + ",\t" + url);

                if (response.code() == STATUS_OK) {
                    //Log.i(TAG, url);
                    callback.onFinished(url);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "error posting recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static Map<String,Object> generatePostRecipeFields(Recipe recipe) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(Constants.POSTED_NAME, recipe.getName());
        fields.put(Constants.POSTED_DESCRIPTION, recipe.getDescription());
        fields.put(Constants.POSTED_CATEGORIES, recipe.getCategories());

        Log.e(TAG, "post body:\n" + fields.toString());

        return fields;
    }

    public static void patchRecipe(Map<String, String> attributes, String id, String token, final MyCallback<Recipe> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);
        //Map<String, String> body = new HashMap<>();
        //body.put(Constants.ATTRIBUTES, gson.toJson(attributes));
        //body.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Recipe> call = service.patchRecipe(headers, id, attributes);

        call.enqueue(new Callback<Recipe>() {
            @Override
            public void onResponse(@NonNull Call<Recipe> call, @NonNull Response<Recipe> response) {
                String body = response.body() != null ? response.body().toString() : "null";

                if (response.code() == STATUS_OK) {
                    Log.i(TAG, body);
                    Recipe recipe = response.body();
                    callback.onFinished(recipe);
                }
                else {
                    Log.e(TAG, "error patchRecipe, code = " + response.code() + "\n body: " + body);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Recipe> call, @NonNull Throwable t) {
                Log.e(TAG, "error patching recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void getAllCategories(String date, String token, final MyCallback<List<Category>> callback) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<Category>> call = service.getAllCategories(token, "0");

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                String body = response.body() != null ? response.body().toString() : "null";

                if (response.code() == STATUS_OK) {
                    Log.i(TAG, body);
                    List<Category> list = response.body();
                    callback.onFinished(list);
                }
                else {
                    Log.e(TAG, "error getAllCategories, code = " + response.code() + "\n body: " + body);
                }

                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting all recipes. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void requestUrlsForFoodPictures(String id, List<String> files, String token, final MyCallback<List<String>> callback) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(files.size()));
        requestBody.put(Constants.PUT_FOOD_EXTENSION, "jpg");

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<String>> call = service.requestFoodUrls(token, id, requestBody);

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                String body = response.body() != null ? response.body().toString() : "null";

                if (response.code() == STATUS_OK) {
                    Log.i(TAG, "result from urls " + body);
                    List<String> list = response.body();
                    callback.onFinished(list);
                } else {
                    Log.e(TAG, "error requesting urls, code = " + response.code() + "\n body: " + body);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting all recipes. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static List<String> requestUrlsForFoodPicturesSync(String id, List<String> files, String token) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(files.size()));
        requestBody.put(Constants.PUT_FOOD_EXTENSION, "jpg");

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<String>> call = service.requestFoodUrls(token, id, requestBody);
        try {
            Response<List<String>> response = call.execute();
            Log.e(TAG, "response code for requesting urls, " + response.code());
            Log.e(TAG, "response urls:\n" + response.body());
            return response.body();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}
