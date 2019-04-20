package com.myapps.ron.family_recipes.layout.firebase.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.myapps.ron.family_recipes.logic.storage.ExternalStorageHelper;

import java.io.File;

import io.reactivex.Single;

/**
 * Created by ronginat on 21/04/2019.
 */
public class FirebaseStorageHelper {
    private static final String TAG = FirebaseStorageHelper.class.getSimpleName();

    public static Single<Uri> downloadFile(Context context, String key, String dir) {
        final File file = ExternalStorageHelper.getFileForOnlineDownload(context, dir, key);
        if (file == null) {
            return Single.error(new Throwable("can\'t create local file"));
        }

        String storageKey = dir + "/" + key;
        Log.e(TAG, "downloadFile, " + storageKey);

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
                                emitter.onError(task.getException());
                            }
                        })
        );
    }
}
