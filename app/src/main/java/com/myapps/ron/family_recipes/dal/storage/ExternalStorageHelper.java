package com.myapps.ron.family_recipes.dal.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.myapps.ron.family_recipes.R;

import java.io.File;

import androidx.core.content.FileProvider;

public class ExternalStorageHelper {

    /**
     * @param context - context of application
     * @param fileName - path of file to check
     * @param dir - dir of file to check
     * @return path of requested file from cache if available or from app root storage. null if file not exists
     */
    static Uri getFileAbsolutePath(Context context, String dir, String fileName){
        File filesDir = context.getExternalFilesDir(dir);
        File file = new File(filesDir, fileName);
        Uri uri = Uri.fromFile(file);
        if (file.exists()) {
            //Log.e("ExternalStorageHelper", file.getPath() + " exists");
            return uri;
        }
        return null;
    }

    /**
     * @param context - context of application
     * @param fileName - path of file to check
     * @param dir - dir of file to check
     * @return path of requested file from app root storage. null if file not exists
     */
    public static Uri getFileUri(Context context, String dir, String fileName){
        File filesDir = context.getExternalFilesDir(dir);
        File file = new File(filesDir, fileName);
        Uri uri = FileProvider.getUriForFile(context, context.getString(R.string.appPackage), file);
        if (file.exists()) {
            //Log.e("ExternalStorageHelper", uri.getPath() + " exists");
            return uri;
        }
        return null;
    }

    /**
     * @param context - context of application
     * @param filePath - path of file to check
     * @param dir - dir of file to check
     * @return path of requested file from cache if available or from app root storage. null if file not exists
     */
    static String getFileAbsolutePath1(Context context, String filePath, String dir){
        String path = dir + "/" + filePath;
        //check whether file is in cache directory

        //Log.e("ExternalStorage", "before path - " + context.getExternalFilesDir(null).getAbsolutePath().concat("/" + path));
        File cacheRoot = context.getExternalCacheDir();
        if(cacheRoot != null) {
            String cachePath = cacheRoot.getAbsolutePath() + "/" + path;
            File cacheFile = new File(cachePath);
            if(cacheFile.exists())
                return cacheFile.getAbsolutePath();
        }

        //check whether file is in app storage directory
        File root = context.getExternalFilesDir(null);
        if(root == null)
            return null;

        String pathStr = root.getAbsolutePath().concat("/" + path); //context.getExternalFilesDir(null).getAbsolutePath();
        //Log.e("ExternalStorage", "after path - " + pathStr);
        File file = new File(pathStr);
        if(file.exists())
            return pathStr;
        else
            return null;
    }

    /**
     * @param context - context of application
     * @return path for app storage root
     */
    public static Uri getFilesRootPath(Context context, String dir) {
        File root = context.getExternalFilesDir(dir);
        if (root != null) {
            return FileProvider.getUriForFile(context, context.getString(R.string.appPackage), root);
        }
        return null;
    }

    public static String getFileForOnlineDownload(Context context, String dir, String fileName) {
        try {
            File filesDir = context.getExternalFilesDir(dir);
            File file = new File(filesDir, fileName);
            return file.getAbsolutePath();
            /*Uri uri = FileProvider.getUriForFile(context, context.getString(R.string.appPackage), file);
            if (file.exists())
                Log.e("ExternalStorageHelper", file.getPath() + " exists");
            return uri;*/

        } catch (IllegalArgumentException e) {
            Log.e(ExternalStorageHelper.class.getSimpleName(), e.getMessage());
        }
        return null;
    }
}
