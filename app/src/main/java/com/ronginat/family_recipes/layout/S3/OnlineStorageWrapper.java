package com.ronginat.family_recipes.layout.S3;

import android.content.Context;
import android.net.Uri;

import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.layout.firebase.storage.FirebaseStorageHelper;
import com.ronginat.family_recipes.utils.MyCallback;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class OnlineStorageWrapper {

    //private static final String TAG = OnlineStorageWrapper.class.getSimpleName();

    //region uploads

    public static boolean uploadFoodFileSync(String url, String localPath) {
        return uploadFileSync(url, localPath, S3Helper.CONTENT_IMAGE);
    }

    /*public static boolean uploadRecipeFileSync(String url, String localPath) {
        return uploadFileSync(url, localPath, S3Helper.CONTENT_TEXT);
    }*/

    @SuppressWarnings("SameParameterValue")
    private static boolean uploadFileSync(String url, String localPath, String mimeType) {
        return S3Helper.uploadFileSync(url, localPath, mimeType);
    }

    /*public static void uploadFoodFile(String url, String localPath, MyCallback<Boolean> callback) {
        uploadFile(url, localPath, S3Helper.CONTENT_IMAGE, callback);
    }

    public static void uploadRecipeFile(String url, String localPath, MyCallback<Boolean> callback) {
        uploadFile(url, localPath, S3Helper.CONTENT_TEXT, callback);
    }

    private static void uploadFile(String url, String localPath, String mimeType, MyCallback<Boolean> callback) {
        S3Helper.uploadFile(url, localPath, mimeType, callback);
    }*/

    //endregion

    //region downloads

    @SuppressWarnings("SameParameterValue")
    private static Single<Uri> downloadFile(Context context, String key, String dir) {
        S3Helper s3 = S3Helper.getInstance(context);
        return s3.downloadFile(context, key, dir);
    }

    @SuppressWarnings("SameParameterValue")
    private static Single<Uri> downloadFileFirebase(Context context, String key, String dir) {
        return FirebaseStorageHelper.downloadFile(context, key, dir);
    }

    public static Single<Uri> downloadThumbFile(Context context, String key) {
        //return downloadFile(context, key, Constants.THUMB_DIR);
        return downloadFileFirebase(context, key, Constants.THUMB_DIR);
    }

    public static void downloadFoodFile(Context context, String key, final MyCallback<Uri> callback) {
        downloadFile(context, key, Constants.FOOD_DIR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onFinished(uri);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onFinished(null);
                    }
                });
    }

    //endregion
}
