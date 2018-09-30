package com.myapps.ron.family_recipes.dal.storage;

import android.content.Context;

import java.io.File;

public class ExternalStorageHelper {

    public static String getFileAbsolutePath(Context context, String filePath){
        //check whether file is in cache directory
        File cacheRoot = context.getExternalCacheDir();
        if(cacheRoot != null) {
            String cachePath = cacheRoot.getAbsolutePath() + "/" + filePath;
            File cacheFile = new File(cachePath);
            if(cacheFile.exists())
                return cacheFile.getAbsolutePath();
        }

        //check whether file is in app storage directory
        File root = context.getExternalFilesDir(null);
        if(root == null)
            return null;

        String path = root.getAbsolutePath().concat("/" + filePath); //context.getExternalFilesDir(null).getAbsolutePath();
        File file = new File(path);
        if(file.exists())
            return path;
        else
            return null;
    }

    public static String getFilesRootPath(Context context) {
        File root = context.getExternalFilesDir(null);
        if(root == null)
            return null;

        return root.getAbsolutePath();
    }
}
