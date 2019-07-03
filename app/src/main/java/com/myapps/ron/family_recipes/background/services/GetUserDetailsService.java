package com.myapps.ron.family_recipes.background.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.logic.Injection;
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
public class GetUserDetailsService extends IntentService {
    private static final String TAG = GetUserDetailsService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FETCH_USER_DETAILS = "com.myapps.ron.family_recipes.background.services.action.GET_USER_DETAILS";

    public GetUserDetailsService() {
        super("GetUserDetailsService");
    }


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchUserDetails(Context context) {
        Intent intent = new Intent(context, GetUserDetailsService.class);
        intent.setAction(ACTION_FETCH_USER_DETAILS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_USER_DETAILS.equals(action)) {
                handleActionFetchUserDetails();
            }
        }
    }

    /*private void setThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }*/

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
