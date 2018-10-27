package com.myapps.ron.family_recipes.dal.storage;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class ExternalStorageHelper {

    /**
     * @param context - context of application
     * @param filePath - path of file to check
     * @param dir - dir of file to check
     * @return path of requested file from cache if available or from app root storage. null if file not exists
     */
    static String getFileAbsolutePath(Context context, String filePath, String dir){
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
    public static String getFilesRootPath(Context context) {
        File root = context.getExternalFilesDir(null);
        if(root == null)
            return null;

        return root.getAbsolutePath();
    }
}
