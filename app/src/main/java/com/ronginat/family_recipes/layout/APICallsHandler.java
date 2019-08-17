package com.ronginat.family_recipes.layout;

import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableMap;
import com.ronginat.family_recipes.BuildConfig;
import com.ronginat.family_recipes.layout.modelTO.CategoryTO;
import com.ronginat.family_recipes.layout.modelTO.CommentTO;
import com.ronginat.family_recipes.layout.modelTO.ContentTO;
import com.ronginat.family_recipes.layout.modelTO.RecipeTO;
import com.ronginat.family_recipes.utils.MyCallback;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

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

    public static Response<Map<String, String>> postRecipeSync(final PostRecipe recipe, final String token) {
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

    public static Observable<Response<RecipeTO>> getRecipeObservable(String id, String lastModified, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);

        return service.getRecipeObservable(token, id, lastModified);
    }

    public static Observable<Response<ContentTO>> getRecipeContentObservable(String id, String lastModified, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);

        return service.getRecipeContentObservable(token, id, lastModified);
    }

    public static Observable<Response<List<CategoryTO>>> getAllCategoriesObservable(String date, String token) {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getAllCategoriesObservable(token, date);
    }

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
                CrashLogger.e(TAG, "error registerNewToken. message: " + t.getMessage());
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
        return service.getAppUpdatesObservable(token, deviceId, BuildConfig.VERSION_NAME);
    }

    // endregion

    public static Observable<Response<Object>> getTestObservable() {
        RecipeInterface service = getReactiveRetrofitInstance().create(RecipeInterface.class);
        return service.getTestObservable();
    }
}
