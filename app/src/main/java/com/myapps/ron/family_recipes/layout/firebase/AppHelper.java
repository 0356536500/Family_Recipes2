package com.myapps.ron.family_recipes.layout.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.logic.repository.AppRepository;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ronginat on 20/04/2019.
 */
public class AppHelper {
    private static final String TAG = AppHelper.class.getSimpleName();

    private static GetTokenResult authSession;
    private static FirebaseUser firebaseSession;
    public static PublishSubject<String> firebaseTokenObservable = PublishSubject.create();
    private static Disposable disposable;
    private static boolean waitingForFirebaseToken = false;

    private static GetTokenResult getAuthSession() {
        return authSession;
    }

    private static void setAuthSession(GetTokenResult newAuthSession) {
        authSession = newAuthSession;
    }

    public static FirebaseUser getFirebaseUser() {
        return firebaseSession;
    }

    public static void initTokenObserver() {
        waitingForFirebaseToken = false;
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
        disposable = com.myapps.ron.family_recipes.layout.cognito.AppHelper.currSessionObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(currSession -> {
                    Log.e(TAG, "firebase observer, got token new from aws");
                    if (currSession != null) {
                        getFirebaseToken(MyApplication.getContext());
                    }
                }, throwable -> firebaseTokenObservable.onError(throwable)
                , () -> disposable.dispose());
    }

    public static String getFirebaseToken(Context context) {
        //Log.e(TAG, "request new token");
        if (getAuthSession() != null && new Date(getAuthSession().getExpirationTimestamp()).after(new Date()))
            return getAuthSession().getToken();
        //Log.e(TAG, "null or expired firebase token");
        if (!waitingForFirebaseToken) {
            waitingForFirebaseToken = true;
            AppRepository.getInstance().getFirebaseToken(context)
                    .observeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableSingleObserver<String>() {
                        @Override
                        public void onSuccess(String token) {
                            // refresh token
                            Log.e(TAG, "got response from server with firebase token");
                            signInCustomToken(token);
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            firebaseTokenObservable.onError(t);
                            dispose();
                        }
                    });
        }
        return null;
    }

    private static void signInCustomToken(@NonNull String token) {
        FirebaseAuth.getInstance()
                .signInWithCustomToken(token)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        updateAuthSession(task.getResult().getUser());
                        Log.e(TAG, "signInWithCustomToken:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithCustomToken:failure", task.getException());
                    }
                });
    }

    private static void updateAuthSession(@NonNull FirebaseUser firebaseUser) {
        firebaseUser.getIdToken(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        setAuthSession(task.getResult());
                        waitingForFirebaseToken = false;
                        if (task.getResult().getToken() != null)
                            firebaseTokenObservable.onNext(task.getResult().getToken());
                        Log.e(TAG, "getIdToken:success");
                    } else {
                        Log.e(TAG, "getIdToken:failure", task.getException());
                    }
                });
    }

    public static void signOutUser() {
        FirebaseAuth.getInstance().signOut();
    }
}
