package com.myapps.ron.family_recipes.layout.firebase.storage;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.myapps.ron.family_recipes.layout.firebase.AppHelper;
import com.myapps.ron.family_recipes.logic.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.utils.logic.CrashLogger;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ronginat on 21/04/2019.
 */
public class FirebaseStorageHelper {
    //private static final String TAG = FirebaseStorageHelper.class.getSimpleName();

    public static Single<Uri> downloadFile(Context context, String key, String dir) {
        /*if (AppHelper.isFirebaseTokenValidElseRefresh(context) == null) {
            return Single.error(new Throwable(context.getString(R.string.invalid_access_token)));
        }*/
        if (AppHelper.isFirebaseTokenValidElseRefresh(context) == null) {
            return Single.create(emitter -> AppHelper.firebaseTokenObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableObserver<String>() {
                        @Override
                        public void onNext(String token) {
                            if (token != null) {
                                final File file = ExternalStorageHelper.getFileForOnlineDownload(context, dir, key);
                                if (file == null) {
                                    emitter.onError(new Throwable("can\'t create local file"));
                                } else {
                                    String storageKey = dir + "/" + key;
                                    FirebaseStorage
                                            .getInstance()
                                            .getReference(storageKey)
                                            .getFile(file)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    emitter.onSuccess(ExternalStorageHelper.getFileAbsolutePath(context, dir, key));
                                                } else {
                                                    if (task.getException() != null)
                                                        task.getException().printStackTrace();
                                                    CrashLogger.logException(task.getException());
                                                    emitter.onError(task.getException());
                                                }
                                            });
                                }
                                dispose();
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            //Log.e(TAG, "error from firebase helper ,", t);
                            CrashLogger.logException(t);
                            emitter.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            if (!isDisposed())
                                dispose();
                        }
                    }));
        }
        return downloadFileWithValidToken(context, key, dir);
    }

    private static Single<Uri> downloadFileWithValidToken(Context context, String key, String dir) {
        final File file = ExternalStorageHelper.getFileForOnlineDownload(context, dir, key);
        if (file == null) {
            return Single.error(new Throwable("can\'t create local file"));
        }

        String storageKey = dir + "/" + key;
        //Log.e(TAG, "downloadFile, " + storageKey);

        return Single.create(emitter ->
                FirebaseStorage
                        .getInstance()
                        .getReference(storageKey)
                        .getFile(file)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                emitter.onSuccess(ExternalStorageHelper.getFileAbsolutePath(context, dir, key));
                            } else {
                                if (task.getException() != null)
                                    task.getException().printStackTrace();
                                CrashLogger.logException(task.getException());
                                emitter.onError(task.getException());
                            }
                        })
        );
    }
}
