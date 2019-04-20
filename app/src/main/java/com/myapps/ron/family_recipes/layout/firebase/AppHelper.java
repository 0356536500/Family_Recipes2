package com.myapps.ron.family_recipes.layout.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.myapps.ron.family_recipes.logic.repository.AppRepository;

import java.util.Date;

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

    private static GetTokenResult getAuthSession() {
        return authSession;
    }

    public static FirebaseUser getFirebaseUser() {
        return firebaseSession;
    }

    public static String getFirebaseToken(Context context) {
        if (getAuthSession() == null || new Date(getAuthSession().getExpirationTimestamp()).before(new Date())) {
            AppRepository.getInstance().getFirebaseToken(context)
                    .observeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableSingleObserver<String>() {
                        @Override
                        public void onSuccess(String token) {
                            // refresh token
                            signInCustomToken(token);
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            firebaseTokenObservable.onError(t);
                            dispose();
                        }
                    });
            return null;
        }
        return getAuthSession().getToken();
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
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getToken() != null) {
                        authSession = task.getResult();
                        firebaseTokenObservable.onNext(authSession.getToken());
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
