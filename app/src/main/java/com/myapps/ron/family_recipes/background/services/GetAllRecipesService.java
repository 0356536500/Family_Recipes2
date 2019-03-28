package com.myapps.ron.family_recipes.background.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.APIResponse;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.utils.logic.DateUtil;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.reactivex.observers.DisposableObserver;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetAllRecipesService extends IntentService {
    private static final String TAG = GetAllRecipesService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_ALL = "com.myapps.ron.family_recipes.background.services.action.GET_ALL";
    private static final String ACTION_FETCH_USER_DETAILS = "com.myapps.ron.family_recipes.background.services.action.GET_USER_DETAILS";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.background.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.background.services.extra.PARAM2";

    public GetAllRecipesService() {
        super("GetAllRecipesService");
    }

    @Override
    public void onDestroy() {
        /*if (asyncRecipeUpdateList != null)
            for (MyAsyncRecipeUpdate asyncRecipeUpdate: asyncRecipeUpdateList)
                asyncRecipeUpdate.cancel(false);*/
        super.onDestroy();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetAllRecipes(Context context) {
        Log.e(TAG, "startActionGetAllRecipes");
        Intent intent = new Intent(context, GetAllRecipesService.class);
        intent.setAction(ACTION_GET_ALL);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchUserDetails(Context context) {
        Intent intent = new Intent(context, GetAllRecipesService.class);
        intent.setAction(ACTION_FETCH_USER_DETAILS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_ALL.equals(action)) {
                Log.e(TAG, "onHandleIntent");
                setThreadPolicy();
                handleActionGetAllRecipes();
            } else if (ACTION_FETCH_USER_DETAILS.equals(action)) {
                handleActionFetchUserDetails();
            }
        }
    }

    private void setThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    /**
     * Handle action GetAllRecipes in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetAllRecipes() {
        Log.e(TAG, "handleActionGetAllRecipes");
        //asyncRecipeUpdateList = Collections.synchronizedList(new ArrayList<>());
        final RecipeRepository  repository = Injection.provideRecipeRepository(getApplicationContext());
        String lastKey = null;
        if (!MiddleWareForNetwork.checkInternetConnection(getApplicationContext()))
            return;
        final String time = DateUtil.getUTCTime();
        do {
            APIResponse<List<RecipeTO>> response = APICallsHandler.getAllRecipesSync(
                    DateUtil.getLastUpdateTime(getApplicationContext()), lastKey, 50, AppHelper.getAccessToken());

            if (response != null) {
                Log.e(TAG, "response: lastKey = " + response.getLastKey());
                Log.e(TAG, "response data length: " + response.getData().size());
                lastKey = response.getLastKey();

                repository.updateFromServer(getApplicationContext(), response.getData(), null);
            }
        }
        while (lastKey != null && !lastKey.isEmpty() && MiddleWareForNetwork.checkInternetConnection(getApplicationContext()));

        DateUtil.updateServerTime(getApplicationContext(), time);
    }


    /**
     * Handle action FetchUserDetails in the provided background thread
     */
    private void handleActionFetchUserDetails() {
        if (MiddleWareForNetwork.checkInternetConnection(getApplicationContext())) {
            if (AppHelper.getAccessToken() == null)
                return;
            APICallsHandler
                    .getUserDetailsObservable(AppHelper.getAccessToken(), MyApplication.getDeviceId())
                    .subscribe(new DisposableObserver<Response<Map<String, Object>>>() {
                        @Override
                        public void onNext(Response<Map<String, Object>> response) {
                            Log.e(TAG, "getUserDetails code = " + response.code());
                            if (response.code() == APICallsHandler.STATUS_OK) {
                                Map<String, Object> body = response.body();
                                if (body != null) {
                                    Log.e(TAG, body.toString());
                                    Gson gson = new Gson();
                                    if (body.get(Constants.RESPONSE_KEY_SUBSCRIPTIONS) != null) {
                                        Type mapType = new TypeToken<Map<String,Boolean>>() {}.getType();

                                        Map<String, Boolean> subscriptionsMap = gson.fromJson(gson.toJson(body.get(Constants.RESPONSE_KEY_SUBSCRIPTIONS)), mapType);
                                        if (subscriptionsMap != null)
                                            Log.e(TAG, "subscriptions: " + subscriptionsMap.toString());
                                        updateSubscriptions(subscriptionsMap);
                                    }

                                    if (body.get(Constants.RESPONSE_KEY_FAVORITES) != null) {
                                        Type mapType = new TypeToken<List<String>>() {}.getType();

                                        List<String> favoritesMap = gson.fromJson(gson.toJson(body.get(Constants.RESPONSE_KEY_FAVORITES)), mapType);
                                        if (favoritesMap != null)

                                        Injection.provideRecipeRepository(getApplicationContext())
                                                .updateFavoritesFromUserRecord(favoritesMap);

                                    }
                                }
                            }
                            else {
                                try {
                                    if (response.errorBody() != null) {
                                        Log.e(TAG, "error getUserDetails, errorBody: " + response.errorBody().string()
                                                + "\n message: " + response.message());
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.e(TAG, "onError, " + t.getMessage());
                            dispose();
                        }

                        @Override
                        public void onComplete() {
                            Log.e(TAG, "onComplete");
                            dispose();
                        }
                    });
        }
    }

    private void updateSubscriptions(Map<String, Boolean> subscriptionsMap) {
        if (subscriptionsMap != null) {
            for (Map.Entry<String, Boolean> entry: subscriptionsMap.entrySet()) {
                SharedPreferencesHandler.writeBoolean(getApplicationContext(), entry.getKey(), entry.getValue());
            }
            //Log.e(TAG, SharedPreferencesHandler.getSharedPreferences(getApplicationContext()).getAll().toString());
        }
    }


}
