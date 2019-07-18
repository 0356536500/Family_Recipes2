package com.ronginat.family_recipes.layout;

import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableMap;
import com.ronginat.family_recipes.BuildConfig;
import com.ronginat.family_recipes.layout.modelTO.CategoryTO;
import com.ronginat.family_recipes.layout.modelTO.CommentTO;
import com.ronginat.family_recipes.layout.modelTO.ContentTO;
import com.ronginat.family_recipes.layout.modelTO.RecipeTO;
import com.ronginat.family_recipes.utils.MyCallback;

import java.io.IOException;
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
    public static final int STATUS_OK_NO_CONTENT = 204;
    public static final int STATUS_NOT_MODIFIED = 304;
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

    public static Observable<Response<List<CommentTO>>> getRecipeCommentsObservable(String id, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getRecipeComments(token, id);
    }

    /*public static void getRecipeComments(String id, String token, MyCallback<List<CommentTO>> callback) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<CommentTO>> call = service.getRecipeComments(token, id);

        call.enqueue(new Callback<List<CommentTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<CommentTO>> call, @NonNull Response<List<CommentTO>> response) {
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
            public void onFailure(@NonNull Call<List<CommentTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting recipe comments", t);
            }
        });
    }

    public static void postRecipe(final PostRecipe recipe, final String token, final MyCallback<Map<String, String>> callback) {
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
        Call<Map<String, String>> call = service.postRecipe(headers, body);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                String url = "null";

                Log.i(TAG, "response code = " + response.code() + ",\t" + url);

                if (response.code() == STATUS_OK) {
                    Map<String, String> body = response.body();
                    if (body != null && body.get("url") != null && body.get("id") != null) {
                        callback.onFinished(response.body());
                    } else
                        callback.onFinished(null);
                } else
                    callback.onFinished(null);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                Log.e(TAG, "error posting recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    public static Response<Map<String, String>> postRecipeSync(final PostRecipe recipe, final String token) {
        Log.e(TAG, "start post recipe");

        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);

        //Map<String, Object> body = new HashMap<>();
        //body.put(Constants.RECIPE_ITEM, generatePostRecipeFields(recipe));
        //Log.e(TAG, "post body: \n" + body.toString());

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Map<String, String>> call = service.postRecipe(headers, generatePostRecipeFields(recipe));

        try {
            return call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String,Object> generatePostRecipeFields(PostRecipe recipe) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(Constants.POSTED_NAME, recipe.getName());
        fields.put(Constants.POSTED_DESCRIPTION, recipe.getDescription());
        fields.put(Constants.POSTED_CONTENT, recipe.getRecipeContent());
        fields.put(Constants.POSTED_CATEGORIES, recipe.getCategories());

        return fields;
    }

    /*public static void patchRecipe(Map<String, Object> attributes, String id, String lastModifiedDate, String token, final MyCallback<RecipeTO> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);
        //Map<String, String> body = new HashMap<>();
        //body.put(Constants.ATTRIBUTES, gson.toJson(attributes));
        //body.put(Constants.NUM_FILES_TO_UPLOAD, String.valueOf(numOfFiles));

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<RecipeTO> call = service.patchRecipe1(headers, id, lastModifiedDate, attributes);

        call.enqueue(new Callback<RecipeTO>() {
            @Override
            public void onResponse(@NonNull Call<RecipeTO> call, @NonNull Response<RecipeTO> response) {
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
            public void onFailure(@NonNull Call<RecipeTO> call, @NonNull Throwable t) {
                Log.e(TAG, "error patching recipe. message: " + t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    public static Observable<Response<RecipeTO>> patchRecipeObservable(Map<String, Object> attributes, String id, String lastModifiedDate, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);

        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.patchRecipe(headers, id, lastModifiedDate, attributes);

    }

    public static Observable<Response<Void>> postCommentObservable(Map<String, Object> attributes, String id, String lastModifiedDate, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, "application/json");
        headers.put(Constants.AUTHORIZATION, token);

        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.postComment(headers, id, lastModifiedDate, attributes);
    }

    /*public static void getAllCategories(String date, String token, final MyCallback<List<CategoryTO>> callback) {
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<CategoryTO>> call = service.getAllCategories(token, date);

        call.enqueue(new Callback<List<CategoryTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<CategoryTO>> call, @NonNull Response<List<CategoryTO>> response) {
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
            public void onFailure(@NonNull Call<List<CategoryTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "error getting all recipes. message: " + t.getMessage());
                callback.onFinished(null);
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

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

    public static Response<List<String>> requestUrlsForFoodPicturesSync(String id, String lastModifiedDate, int numOfFiles, String token) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<List<String>> call = service.requestFoodUrls(token, id, lastModifiedDate, numOfFiles, "jpg");
        try {
            return call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // region Observable

    public static Observable<Response<RecipeTO>> getRecipeObservable(String id, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);

        return service.getRecipeObservable(token, id);
    }

    public static Observable<Response<ContentTO>> getRecipeContentObservable(String id, String lastModified, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);

        return service.getRecipeContentObservable(token, id, lastModified);
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
        Map<String, Object> queries = ImmutableMap.of(
                Constants.QUERY_PLATFORM, "android",
                Constants.QUERY_ANDROID_VERSION, Build.VERSION.SDK_INT,
                Constants.QUERY_APP_VERSION, BuildConfig.VERSION_NAME);
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Void> call = service.registerNewToken(authToken, deviceId, firebaseToken, queries);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                //String body = response.body() != null ? response.body().toString() : "null";
                //Log.i(TAG, "registerNewToken code, " + response.code());

                if (response.code() == STATUS_OK) {
                    callback.onFinished(null);
                }
                else {
                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            /*Log.e(TAG, "error registerNewToken, code = " + response.code() + "\n errorBody: " + err
                                    + "\n message: " + response.message());*/
                            callback.onFinished(err);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onFinished("error");
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "error registerNewToken. message: " + t.getMessage());
                callback.onFinished(t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Observable<Response<Void>> manageSubscriptionsObservable(String authToken, String deviceId,
                                           Map<String, String> queries, Map<String, String> policy) {
        /*if (policy == null)
            policy = new HashMap<>();*/
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.manageSubscriptions(authToken, deviceId, queries, policy);
    }

    /*public static void manageSubscriptions(String authToken, String deviceId,
                     Map<String, String> queries, Map<String, String> policy, MyCallback<String> callback) {
        *//*if (policy == null)
            policy = new HashMap<>();*//*
        RecipeInterface service = getRetrofitInstance().create(RecipeInterface.class);
        Call<Void> call = service.manageSubscriptions(authToken, deviceId, queries, policy);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                //String body = response.body() != null ? response.body().toString() : "null";
                Log.i(TAG, "manageSubscriptions code, " + response.code());

                if (response.code() == STATUS_OK) {
                    callback.onFinished(null);
                }
                else {
                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            Log.e(TAG, "error manageSubscriptions, code = " + response.code() + "\n errorBody: " + err
                                    + "\n message: " + response.message());
                            callback.onFinished(err);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onFinished("error");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "error manageSubscriptions. message: " + t.getMessage());
                callback.onFinished(t.getMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    public static Observable<Response<Map<String, Object>>> getUserDetailsObservable(String token, String deviceId) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getUserDetailsObservable(token, deviceId);
    }

    public static Observable<Response<String>> getFirebaseTokenObservable(String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getFirebaseTokenObservable(token);
    }

    // endregion

    // region APP UPDATES

    public static Observable<Response<Map<String, String>>> getAppUpdates(String token, String deviceId) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getAppUpdatesObservable(token, deviceId);
    }

    // endregion

    public static Observable<Response<Object>> getTestObservable() {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getTestObservable();
    }
}
