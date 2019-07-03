package com.myapps.ron.family_recipes.background.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import retrofit2.Response;

/**
 * Created by ronginat on 20/06/2019
 *
 * Get User subscriptions and favorites from server
 */
public class GetUserDetailsWorker extends RxWorker {

    private String TAG = getClass().getSimpleName();

    /**
     * @param context   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public GetUserDetailsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Single<Result> createWork() {
        return Single.create(emitter ->
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
                        }));
    }

    private void updateSubscriptions(Map<String, Boolean> subscriptionsMap) {
        if (subscriptionsMap != null) {
            for (Map.Entry<String, Boolean> entry: subscriptionsMap.entrySet()) {
                SharedPreferencesHandler.writeBoolean(getApplicationContext(), entry.getKey(), entry.getValue());
            }
            //Log.e(TAG, SharedPreferencesHandler.getSharedPreferences(getApplicationContext()).getAll().toString());
        }
    }

    static OneTimeWorkRequest getUserDetailsWorker() {
        // Create a Constraints object that defines when the task should run
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // then create a OneTimeWorkRequest that uses those constraints
        return new OneTimeWorkRequest.Builder(GetUserDetailsWorker.class)
                .setConstraints(myConstraints)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
    }
}
