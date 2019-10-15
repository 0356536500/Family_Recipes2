package com.ronginat.family_recipes.layout.firebase.db;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.layout.MiddleWareForNetwork;
import com.ronginat.family_recipes.layout.firebase.AppHelper;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ronginat on 05/07/2019
 */
public class FirestoreHelper {
    //private static final String TAG = FirestoreHelper.class.getSimpleName();

    private static FirestoreHelper INSTANCE;

    // key = username, value = name attribute from cognito
    // acting as cache layer for firestore db
    // possible because there are relatively few users
    private Map<String, String> users;

    private FirebaseFirestore db;

    public static FirestoreHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FirestoreHelper();
        }
        return INSTANCE;
    }

    private FirestoreHelper() {
        super();
        users = new HashMap<>();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieve user's displayed name from Firebase Firestore
     * Collection: {@link Constants#FIRESTORE_USERS}
     * Document id: {@link Constants#USERNAME}
     * Displayed name attribute: {@link Constants#FIRESTORE_DISPLAYED_NAME}
     *
     * if no internet connection, return the {@param username} as Displayed name
     */
    public Single<String> getUserDisplayedName(Context context, String username) {
        if (users.containsKey(username)) {
            String name = users.get(username);
            if (name != null)
                return Single.just(name);
        }

        // when no network, return the username as displayed name
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.just(username);

        if (AppHelper.isFirebaseTokenValidElseRefresh(context) == null) {
            return Single.create(emitter -> AppHelper.firebaseTokenObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableObserver<String>() {
                        @Override
                        public void onNext(String token) {
                            // token NonNull from io.reactivex.Observer Interface
                            INSTANCE.db
                                    .collection(Constants.FIRESTORE_USERS)
                                    .document(username)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                            String name = task.getResult().getString(Constants.FIRESTORE_DISPLAYED_NAME);
                                            users.put(username, name);
                                            emitter.onSuccess(name);
                                        }
                                    });
                        }

                        @Override
                        public void onError(Throwable t) {
                            //Log.e(TAG, ERROR_MESSAGE + ", ", t);
                            CrashLogger.logException(t);
                            emitter.onSuccess(username);
                        }

                        @Override
                        public void onComplete() {
                            if (!isDisposed())
                                dispose();
                        }
                    }));
        } else {
            // token is valid
            return getUserDisplayedName(username);
        }
    }

    private Single<String> getUserDisplayedName(String username) {
        return Single.create(emitter ->
                INSTANCE.db
                        .collection(Constants.FIRESTORE_USERS)
                        .document(username)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                String name = task.getResult().getString(Constants.FIRESTORE_DISPLAYED_NAME);
                                users.put(username, name);
                                emitter.onSuccess(name);
                            } else {
                                if (task.getException() != null)
                                    CrashLogger.logException(task.getException());

                                emitter.onSuccess(username);
                            }
                        })
        );
    }

    public Single<Boolean> setDisplayedName(Context context, String name) {
        // when no network, return the username as displayed name
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.error(new Throwable(context.getString(R.string.no_internet_message)));

        if (AppHelper.isFirebaseTokenValidElseRefresh(context) == null) {
            return Single.create(emitter -> AppHelper.firebaseTokenObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableObserver<String>() {
                        @Override
                        public void onNext(String token) {
                            // token NonNull from io.reactivex.Observer Interface
                            INSTANCE.db
                                    .collection(Constants.FIRESTORE_USERS)
                                    .document(com.ronginat.family_recipes.layout.cognito.AppHelper.getCurrUser())
                                    .set(Collections.singletonMap(Constants.FIRESTORE_DISPLAYED_NAME, name), SetOptions.merge())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            //Log.e(TAG, "success setting new displayed name");
                                            users.put(com.ronginat.family_recipes.layout.cognito.AppHelper.getCurrUser(), name);
                                            emitter.onSuccess(true);
                                        }
                                        else if (task.getException() != null){
                                            CrashLogger.logException(task.getException());
                                            //Log.e(TAG, task.getException().getMessage(), task.getException());
                                            emitter.onError(new Throwable(context.getString(R.string.change_displayed_name_error)));
                                        } else {
                                            //Log.e(TAG, "error setting new displayed name");
                                            emitter.onError(new Throwable(context.getString(R.string.change_displayed_name_error)));
                                        }
                                    });
                        }

                        @Override
                        public void onError(Throwable t) {
                            //Log.e(TAG, ERROR_MESSAGE + ", ", t);
                            //CrashLogger.logException(t);
                            emitter.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            if (!isDisposed())
                                dispose();
                        }
                    }));
        } else {
            // token is valid
            return setDisplayedNameWithValidToken(context, name);
        }

    }


    private Single<Boolean> setDisplayedNameWithValidToken(Context context, String name) {
        return Single.create(emitter ->
           INSTANCE.db
                   .collection(Constants.FIRESTORE_USERS)
                   .document(com.ronginat.family_recipes.layout.cognito.AppHelper.getCurrUser())
                   .set(Collections.singletonMap(Constants.FIRESTORE_DISPLAYED_NAME, name), SetOptions.merge())
                   .addOnCompleteListener(task -> {
                       if (task.isSuccessful()) {
                           users.put(com.ronginat.family_recipes.layout.cognito.AppHelper.getCurrUser(), name);
                           emitter.onSuccess(true);
                       }
                       else if (task.getException() != null){
                           CrashLogger.logException(task.getException());
                           //Log.e(TAG, task.getException().getMessage(), task.getException());
                           emitter.onError(new Throwable(context.getString(R.string.change_displayed_name_error)));
                       } else
                           emitter.onError(new Throwable(context.getString(R.string.change_displayed_name_error)));
                   })
        );
    }
}
