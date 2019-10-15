package com.ronginat.family_recipes.layout.firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.ronginat.family_recipes.MyApplication;
import com.ronginat.family_recipes.logic.repository.AppRepository;
import com.ronginat.family_recipes.utils.logic.SharedPreferencesHandler;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ronginat on 20/04/2019.
 */
public class AppHelper {
    //private static final String TAG = AppHelper.class.getSimpleName() + "Firebase";

    private static GetTokenResult authSession;
    //private static FirebaseUser firebaseSession;
    public static PublishSubject<String> firebaseTokenObservable = PublishSubject.create();
    private static Disposable disposable;
    private static boolean waitingForFirebaseToken = false;
    private static final String FIREBASE_TOKEN_EXPIRATION = "firebase_token_expiration";
    private static final long DEFAULT_VALUE = -1L;

    private static GetTokenResult getAuthSession() {
        return authSession;
    }

    private static void setAuthSession(GetTokenResult newAuthSession) {
        authSession = newAuthSession;
    }

    /*public static FirebaseUser getFirebaseUser() {
        return firebaseSession;
    }*/

    public static void initTokenObserver(Context context) {
        SharedPreferencesHandler.removeString(context, FIREBASE_TOKEN_EXPIRATION);
        waitingForFirebaseToken = false;
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
        disposable = com.ronginat.family_recipes.layout.cognito.AppHelper.currSessionObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(currSession -> {
                    //Log.e(TAG, "firebase observer, got token new from aws");
                    if (currSession != null) {
                        isFirebaseTokenValidElseRefresh(MyApplication.getContext());
                    }
                }, throwable -> firebaseTokenObservable.onError(throwable)
                , () -> disposable.dispose());
    }

    public static String isFirebaseTokenValidElseRefresh(Context context) {
        long expiration = SharedPreferencesHandler.getLong(context, FIREBASE_TOKEN_EXPIRATION, DEFAULT_VALUE);
        if (expiration != DEFAULT_VALUE && new Date(expiration).after(new Date()))
            return Long.toString(expiration);
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
                            //Log.e(TAG, "got response from server with firebase token");
                            signInCustomToken(context, token);
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

    private static void signInCustomToken(Context context, @NonNull String token) {
        FirebaseAuth.getInstance()
                .signInWithCustomToken(token)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        updateAuthSession(context, task.getResult().getUser());
                        //Log.e(TAG, "signInWithCustomToken:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        //CrashLogger.logException(task.getException());
                        if (task.getException() != null)
                            firebaseTokenObservable.onError(task.getException());
                        //Log.e(TAG, "signInWithCustomToken:failure", task.getException());
                    }
                });
    }

    private static void updateAuthSession(Context context, @NonNull FirebaseUser firebaseUser) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);

        firebaseUser.getIdToken(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        //Log.e(TAG, "update auth, expiration: " + calendar.getTime().toString());
                        SharedPreferencesHandler.writeLong(context, FIREBASE_TOKEN_EXPIRATION, calendar.getTimeInMillis());
                        setAuthSession(task.getResult());
                        waitingForFirebaseToken = false;
                        if (task.getResult().getToken() != null)
                            firebaseTokenObservable.onNext(task.getResult().getToken());
                        //Log.e(TAG, "getIdToken:success");
                    } else {
                        SharedPreferencesHandler.writeLong(context, FIREBASE_TOKEN_EXPIRATION, DEFAULT_VALUE);
                        if (task.getException() != null)
                            firebaseTokenObservable.onError(task.getException());
                        //Log.e(TAG, "getIdToken:failure", task.getException());
                    }
                });
    }

    public static void signOutUser() {
        FirebaseAuth.getInstance().signOut();
    }
}
