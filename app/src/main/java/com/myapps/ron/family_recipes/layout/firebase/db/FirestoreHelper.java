package com.myapps.ron.family_recipes.layout.firebase.db;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.firebase.AppHelper;
import com.myapps.ron.family_recipes.utils.logic.CrashLogger;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ronginat on 05/07/2019
 */
public class FirestoreHelper {

    private static FirestoreHelper INSTANCE;

    // key = username, value = name attribute from cognito
    // acting as cache layer for firestore db
    // possible because there are relatively few users
    private Map<String, String> users;

    private FirebaseFirestore db;

    private final static String ERROR_MESSAGE = "error with firestore";

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
        if (users.containsKey(username))
            return Single.just(users.get(username));

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
                            Log.e(getClass().getSimpleName(), ERROR_MESSAGE + ", ", t);
                            CrashLogger.logException(t);
                            emitter.onError(new Throwable(ERROR_MESSAGE));
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
                            } else if (task.getException() != null) {
                                CrashLogger.logException(task.getException());
                                emitter.onError(task.getException());
                            } else
                                emitter.onError(new Throwable(ERROR_MESSAGE));
                        })
        );
    }

    public Single<Boolean> setDisplayedName(String name) {
        return Single.create(emitter ->
           INSTANCE.db
                   .collection(Constants.FIRESTORE_USERS + "/" + com.myapps.ron.family_recipes.layout.cognito.AppHelper.getCurrUser())
                   .document(Constants.FIRESTORE_DISPLAYED_NAME)
                   .set(name)
                   .addOnCompleteListener(task -> {
                       if (task.isSuccessful()) {
                           users.put(com.myapps.ron.family_recipes.layout.cognito.AppHelper.getCurrUser(), name);
                           emitter.onSuccess(true);
                       }
                       else if (task.getException() != null){
                           CrashLogger.logException(task.getException());
                           Log.e(FirestoreHelper.class.getSimpleName(), task.getException().getMessage(), task.getException());
                           emitter.onError(task.getException());
                       } else
                           emitter.onError(new Throwable(ERROR_MESSAGE));
                   })
        );
    }
}
