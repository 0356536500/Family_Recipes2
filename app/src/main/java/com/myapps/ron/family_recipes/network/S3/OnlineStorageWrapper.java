package com.myapps.ron.family_recipes.network.S3;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MyCallback;

public class OnlineStorageWrapper {

    //private static final String TAG = OnlineStorageWrapper.class.getSimpleName();

    //region uploads

    public static boolean uploadFoodFileSync(String url, String localPath) {
        return uploadFileSync(url, localPath, S3Helper.CONTENT_IMAGE);
    }

    public static boolean uploadRecipeFileSync(String url, String localPath) {
        return uploadFileSync(url, localPath, S3Helper.CONTENT_TEXT);
    }

    private static boolean uploadFileSync(String url, String localPath, String mimeType) {
        return S3Helper.uploadFileSync(url, localPath, mimeType);
    }

    public static void uploadFoodFile(String url, String localPath, MyCallback<Boolean> callback) {
        uploadFile(url, localPath, S3Helper.CONTENT_IMAGE, callback);
    }

    public static void uploadRecipeFile(String url, String localPath, MyCallback<Boolean> callback) {
        uploadFile(url, localPath, S3Helper.CONTENT_TEXT, callback);
    }

    private static void uploadFile(String url, String localPath, String mimeType, MyCallback<Boolean> callback) {
        S3Helper.uploadFile(url, localPath, mimeType, callback);
    }

    //endregion

    //region downloads

    private static void downloadFile(Context context, String key, String rootPath, String dir, final MyCallback<String> callback) {
        S3Helper s3 = S3Helper.getInstance(context);
        s3.downloadFile(key, rootPath, dir, callback);
    }

    public static void downloadThumbFile(Context context, String key, final MyCallback<String> callback) {
        downloadFile(context, key, ExternalStorageHelper.getFilesRootPath(context), Constants.THUMB_DIR, callback);
    }

    public static void downloadFoodFile(Context context, String key, final MyCallback<String> callback) {
        downloadFile(context, key, ExternalStorageHelper.getFilesRootPath(context), Constants.FOOD_DIR, callback);
    }

    public static void downloadRecipeFile(Context context, String key, final MyCallback<String> callback) {
        downloadFile(context, key, ExternalStorageHelper.getFilesRootPath(context), Constants.RECIPES_DIR, callback);
    }

    //endregion

    /*static class MyAsyncUploader extends AsyncTask<Void, Integer, String> {
        private MyCallback<Boolean> callback;
        private String[] localPaths, keys;
        private int i = 0, successes = 0;
        private boolean[] fails;


        MyAsyncUploader(String[] keys, String[] localPaths, MyCallback<Boolean> callback) {
            this.callback = callback;
            this.keys = keys;
            this.localPaths = localPaths;
            fails = new boolean[localPaths.length];
        }

        @Override
        protected String doInBackground(Void... voids) {
            for(i = 0; i < localPaths.length; i++) {
                S3Helper.uploadFile(keys[i], localPaths[i], S3Helper.CONTENT_IMAGE, new MyCallback<Boolean>() {
                    @Override
                    public void onFinished(Boolean result) {
                        if(result)
                            successes++;
                        else
                            fails[i] = true;
                    }
                });
            }

            while(successes < localPaths.length) {
                try {
                    Thread.sleep(100);
                    if(checkForFailures(fails) >= 0)
                        break;
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            int failPosition = checkForFailures(fails);

            return failPosition >= 0 ? keys[failPosition] : null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //callback.onFinished(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("asyncUpload", "problem uploading file, " + s);
        }
    }*/

}
