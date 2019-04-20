package com.myapps.ron.family_recipes.logic.repository;

import android.content.Context;
import android.os.Build;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;

import java.io.IOException;
import java.util.Map;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Created by ronginat on 17/04/2019.
 */
public class AppRepository {

    private static AppRepository INSTANCE;

    public static AppRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppRepository();
        }
        return INSTANCE;
    }

    // region notifications

    public Single<String> manageSubscriptions(Context context, Map<String, String> queries, Map<String, String> policy) {
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.just(context.getString(R.string.no_internet_message));
        if (AppHelper.getAccessToken() == null)
            return Single.just(context.getString(R.string.invalid_access_token));
        return Single.create(emitter ->
                APICallsHandler.manageSubscriptionsObservable(AppHelper.getAccessToken(), MyApplication.getDeviceId(), queries, policy)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(new DisposableObserver<Response<Void>>() {
                            @Override
                            public void onNext(Response<Void> response) {
                                if (response.code() == APICallsHandler.STATUS_OK) {
                                    emitter.onSuccess("");
                                } else {
                                    int code = response.code();
                                    try {
                                        if (response.errorBody() != null)
                                            emitter.onError(new Throwable(String.format("status %d" + response.errorBody().string(), code)));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        emitter.onError(e);
                                    }
                                }
                                dispose();
                            }

                            @Override
                            public void onError(Throwable t) {
                                emitter.onError(t);
                                dispose();
                            }

                            @Override
                            public void onComplete() {
                                if (!isDisposed())
                                    dispose();
                            }
                        })
        );
    }

    // endregion

    /**
     *
     * @param context context of the app/activity
     * @return map containing url to download the new app version and the file name to display to the user
     */
    public Maybe<Map<String, String>> getDataToDownloadUpdate(Context context) {
        if (MiddleWareForNetwork.checkInternetConnection(context)) {
            return Maybe.create(emitter ->
                    APICallsHandler.getDetailsForUpdate(AppHelper.getAccessToken(), Build.VERSION.SDK_INT)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe(new DisposableObserver<Response<Map<String, String>>>() {
                                @Override
                                public void onNext(Response<Map<String, String>> response) {
                                    if (response.isSuccessful()) { // status 2xx
                                        if (response.code() == APICallsHandler.STATUS_OK_NO_CONTENT) // up to date
                                            emitter.onComplete();
                                        else if (response.code() == APICallsHandler.STATUS_OK && response.body() != null) { // update available
                                            Map<String, String> body = response.body();
                                            if (body.containsKey(Constants.RESPONSE_KEY_APP_URL) && body.containsKey(Constants.RESPONSE_KEY_APP_NAME))
                                                emitter.onSuccess(body);
                                            else
                                                emitter.onError(new Throwable("error from server"));
                                        }
                                    } else if (response.errorBody() != null){ // status <> 2xx
                                        try {
                                            //
                                            String err = response.errorBody().string();
                                            emitter.onError(new Throwable(err));

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            emitter.onError(e);
                                        }
                                    } else {
                                        emitter.onError(new Throwable("error updating the app"));
                                    }

                                    dispose();
                                }

                                @Override
                                public void onError(Throwable t) {
                                    emitter.onError(t);
                                    dispose();
                                }

                                @Override
                                public void onComplete() {
                                    emitter.onComplete();
                                    dispose();
                                }
                    }));
        } else
            return Maybe.error(new Throwable(context.getString(R.string.no_internet_message)));
    }


    public Single<String> getFirebaseToken(Context context) {
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.error(new Throwable(context.getString(R.string.no_internet_message)));
        if (AppHelper.getAccessToken() == null)
            return Single.error(new Throwable(context.getString(R.string.invalid_access_token)));
        return Single.create(emitter -> APICallsHandler.getFirebaseTokenObservable(AppHelper.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new DisposableObserver<Response<String>>() {
                    @Override
                    public void onNext(Response<String> response) {
                        if (response.code() == APICallsHandler.STATUS_OK && response.body() != null) { // got a token
                            emitter.onSuccess(response.body());
                        } else if (response.errorBody() != null) {
                            try {
                                String err = response.errorBody().string();
                                emitter.onError(new Throwable(err));

                            } catch (IOException e) {
                                e.printStackTrace();
                                emitter.onError(e);
                            }
                        } else {
                            emitter.onError(new Throwable("error response from server"));
                        }
                        dispose();
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.onError(t);
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (!isDisposed())
                            dispose();
                    }
                }));
    }
}
