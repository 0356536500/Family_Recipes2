package com.myapps.ron.family_recipes.network.S3;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.network.MyCallback;

public class OnlineStorageWrapper {

    //private static final String TAG = OnlineStorageWrapper.class.getSimpleName();

    public static void uploadFile(String url, String localPath, MyCallback<Boolean> callback) {
        S3Helper.uploadFile(url, localPath, S3Helper.CONTENT_IMAGE, callback);
    }

    public static void downloadFile(Context context, String key, final MyCallback<String> callback) {
        S3Helper s3 = S3Helper.getInstance(context);
        s3.downloadFile(key, ExternalStorageHelper.getFilesRootPath(context) + "/", callback);
    }

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
