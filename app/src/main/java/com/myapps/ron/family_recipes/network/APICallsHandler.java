package com.myapps.ron.family_recipes.network;

import android.os.StrictMode;
import android.util.Log;

import com.google.gson.JsonObject;
import com.myapps.ron.family_recipes.network.modelTO.CategoryTO;
import com.myapps.ron.family_recipes.network.modelTO.CommentTO;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.utils.MyCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class APICallsHandler {

    private static final String TAG = APICallsHandler.class.getSimpleName();
    private static Retrofit retrofit, rxRetrofit;

    public static final int STATUS_OK = 200;
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

    private static Retrofit getReactiveRetrofitInstance() {
        if (rxRetrofit == null) {
            rxRetrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return rxRetrofit;
    }

    public static void getRecipeComments(String id, String token, MyCallback<List<CommentTO>> callback) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<CommentTO>> call = service.getRecipeComments(token, id);

        call.enqueue(new Callback<List<CommentTO>>() {
            @Override
            public void onResponse(@NotNull Call<List<CommentTO>> call, @NotNull Response<List<CommentTO>> response) {
                String body = response.body() != null ? response.body().toString() : "null";
                Log.e(TAG, body);
                if (response.code() == STATUS_OK) {
                    //List<CommentTO> comments = response.body();
                    callback.onFinished(response.body());
                } else {
                    callback.onFinished(null);
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<CommentTO>> call, @NotNull Throwable t) {
                Log.e(TAG, "error getting recipe comments", t);
            }
        });
    }

    public static void postRecipe(final RecipeTO recipe, final String token, final MyCallback<String> callback) {
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
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
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
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                Log.e(TAG, "error posting recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static Map<String,Object> generatePostRecipeFields(RecipeTO recipe) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(Constants.POSTED_NAME, recipe.getName());
        fields.put(Constants.POSTED_DESCRIPTION, recipe.getDescription());
        fields.put(Constants.POSTED_CATEGORIES, recipe.getCategories());

        Log.e(TAG, "post body:\n" + fields.toString());

        return fields;
    }

    public static void patchRecipe(Map<String, Object> attributes, String id, String lastModifiedDate, String token, final MyCallback<RecipeTO> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);
        //Map<String, String> body = new HashMap<>();
        //body.put(Constants.ATTRIBUTES, gson.toJson(attributes));
        //body.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<RecipeTO> call = service.patchRecipe(headers, id, lastModifiedDate, attributes);

        call.enqueue(new Callback<RecipeTO>() {
            @Override
            public void onResponse(@NotNull Call<RecipeTO> call, @NotNull Response<RecipeTO> response) {
                //String body = response.body() != null ? response.body().toString() : "null";
                Log.i(TAG, "patchRecipe code, " + response.code());

                if (response.code() == STATUS_OK) {
                    //RecipeTO recipe = response.body();
                    callback.onFinished(response.body());
                }
                else {
                    try {
                        if (response.errorBody() != null)
                            Log.e(TAG, "error patchRecipe, code = " + response.code() + "\n errorBody: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callback.onFinished(null);
                }
            }

            @Override
            public void onFailure(@NotNull Call<RecipeTO> call, @NotNull Throwable t) {
                Log.e(TAG, "error patching recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void postComment(Map<String, Object> attributes, String id, String lastModifiedDate, String token, final MyCallback<Boolean> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Void> call = service.postComment(headers, id, lastModifiedDate, attributes);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                //String body = response.body() != null ? response.body().toString() : "null";
                Log.i(TAG, "postComment code, " + response.code());

                if (response.code() == STATUS_OK) {
                    //RecipeTO recipe = response.body();
                    callback.onFinished(true);
                }
                else {
                    try {
                        if (response.errorBody() != null)
                            Log.e(TAG, "error postComment, code = " + response.code() + "\n errorBody: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callback.onFinished(false);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                Log.e(TAG, "error patching recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void getAllCategories(String date, String token, final MyCallback<List<CategoryTO>> callback) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<CategoryTO>> call = service.getAllCategories(token, date);

        call.enqueue(new Callback<List<CategoryTO>>() {
            @Override
            public void onResponse(@NotNull Call<List<CategoryTO>> call, @NotNull Response<List<CategoryTO>> response) {
                String body = response.body() != null ? response.body().toString() : "null";

                if (response.code() == STATUS_OK) {
                    Log.i(TAG, body);
                    List<CategoryTO> list = response.body();
                    callback.onFinished(list);
                }
                else {
                    if (response.code() == STATUS_NOT_MODIFIED) {
                        callback.onFinished(new ArrayList<>());
                    }
                    Log.e(TAG, "getAllCategoriesLiveData, code = " + response.code() + "\n body: " + body);
                }

                //generateDataList(response.body());
            }

            @Override
            public void onFailure(@NotNull Call<List<CategoryTO>> call, @NotNull Throwable t) {
                Log.e(TAG, "error getting all recipes. message: " + t.getMessage());
                callback.onFinished(null);
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*public static void requestUrlsForFoodPictures(String id, List<String> files, String token, final MyCallback<List<String>> callback) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(files.size()));
        requestBody.put(Constants.PUT_FOOD_EXTENSION, "jpg");

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<String>> call = service.requestFoodUrls(token, id, null, requestBody);

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NotNull Call<List<String>> call, @NotNull Response<List<String>> response) {
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
            public void onFailure(@NotNull Call<List<String>> call, @NotNull Throwable t) {
                Log.e(TAG, "error getting all recipes. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });

    }*/

    public static List<String> requestUrlsForFoodPicturesSync(String id, String lastModifiedDate, int numOfFiles, String token) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));
        requestBody.put(Constants.PUT_FOOD_EXTENSION, "jpg");

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<String>> call = service.requestFoodUrls(token, id, lastModifiedDate, requestBody);
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

    public static APIResponse<List<RecipeTO>> getAllRecipesSync(String date, String startKey, int limit, String token) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<RecipeTO>> call = service.getAllRecipesPagination(token, date, startKey, limit);

        try {
            Response<List<RecipeTO>> response = call.execute();
            Log.e(TAG, "response code for getAllRecipesSync, " + response.code());
            Log.e(TAG, "response getAllRecipesSync:\n" + response.body());
            APIResponse<List<RecipeTO>> rv = null;
            if (response.code() == STATUS_OK) {
                rv = new APIResponse<>();
                rv.setData(response.body());
                rv.setLastKey(response.headers().get(Constants.HEADER_LAST_EVAL_KEY));
            }
            return rv;

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

    }

    // region Observable

    public static Observable<Response<RecipeTO>> getRecipeObservable(String id, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);

        return service.getRecipeObservable(token, id);
    }

    public static Observable<Response<List<CategoryTO>>> getAllCategoriesObservable(String date, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);

        return service.getAllCategoriesObservable(token, date);
    }

    /*public static Observable<Response<List<RecipeTO>>> getAllRecipesObservable(String date, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getAllRecipesObservable(token, date, null, null);
    }*/

    public static Observable<Response<List<RecipeTO>>> getAllRecipesObservable(String date, int limit, String startKey, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getAllRecipesObservable(token, date, startKey, limit);
    }

    // endregion

    // region USERS

    public static void registerNewToken(String authToken, String deviceId, String firebaseToken, final MyCallback<String> callback) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Void> call = service.registerNewToken(authToken, deviceId, firebaseToken, "android");

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                //String body = response.body() != null ? response.body().toString() : "null";
                Log.i(TAG, "postComment code, " + response.code());

                if (response.code() == STATUS_OK) {
                    callback.onFinished(null);
                }
                else {
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "error postComment, code = " + response.code() + "\n errorBody: " + response.errorBody().string());
                            callback.onFinished(response.errorBody().string());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                Log.e(TAG, "error patching recipe. message: " + t.getMessage());
                callback.onFinished(t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Observable<Response<Void>> manageSubscriptionsObservable(
            String token, String deviceId, String newRecipeSubscription, String commentSubscription, Map<String, String> attributes) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.manageSubscriptions(token, deviceId, newRecipeSubscription, commentSubscription, attributes);
    }

    // endregion
}
