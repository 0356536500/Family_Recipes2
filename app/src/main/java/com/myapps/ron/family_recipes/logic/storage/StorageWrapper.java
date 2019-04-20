package com.myapps.ron.family_recipes.logic.storage;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.utils.logic.DateUtil;
import com.myapps.ron.family_recipes.utils.MyCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import io.reactivex.Single;

public class StorageWrapper {
    private static final String TAG = StorageWrapper.class.getSimpleName();
    /*private static StorageWrapper storage;

    public static StorageWrapper getInstance(Context context) {
        if(storage == null) {
            storage = new StorageWrapper(context);
        }
        return storage;
    }

    private StorageWrapper(Context context) {

    }*/

    public static Single<Uri> getThumbFile(Context context, String fileName) {
        if(fileName == null || fileName.equals(""))
            return Single.error(new Throwable(""));
        Uri path = ExternalStorageHelper.getFileUri(context, Constants.THUMB_DIR, fileName);
        //Log.e("StorageWrapper", "get local path - " + path);
        if(path != null)
            return Single.just(path);
        else if (MiddleWareForNetwork.checkInternetConnection(context)){
            return OnlineStorageWrapper.downloadThumbFile(context, fileName);
        }
        else
            return Single.error(new Throwable(""));
    }

    public static void getFoodFile(Context context, String fileName, MyCallback<Uri> callback) {
        if(fileName == null || fileName.equals(""))
            return;
        Uri path = ExternalStorageHelper.getFileUri(context, Constants.FOOD_DIR, fileName);
        //Log.e("StorageWrapper", "get local path - " + path);
        if(path != null)
            callback.onFinished(path);
        else if (MiddleWareForNetwork.checkInternetConnection(context)){
            OnlineStorageWrapper.downloadFoodFile(context, fileName, callback);
        }
        else
            callback.onFinished(null);
    }

    public static void getRecipeFile(Context context, String fileName, MyCallback<Uri> callback) {
        if(fileName == null || fileName.equals(""))
            return;
        Uri path = ExternalStorageHelper.getFileAbsolutePath(context, Constants.RECIPES_DIR, fileName);
        if(path != null) {
            Log.e("StorageWrapper", "get local path - " + path.getPath());
            callback.onFinished(path);
        }
        else if (MiddleWareForNetwork.checkInternetConnection(context)){
            OnlineStorageWrapper.downloadRecipeFile(context, fileName, callback);
        }
        else
            callback.onFinished(null);
    }

    public static File createHtmlFile(Context context, String fileName, String html) {
        //String path = Environment.getExternalStorageDirectory().getPath();
        //String fileName = DateFormat.format("dd_MM_yyyy_hh_mm_ss", System.currentTimeMillis()).toString();
        //fileName = fileName + ".html";
        //String path = ExternalStorageHelper.getFileAbsolutePath(context, fileName, com.myapps.ron.family_recipes.network.Constants.RECIPES_DIR);
        String path = context.getFilesDir().getPath();
        //Log.e("StorageWrapper", "path is " + path);
        File file = new File(path, fileName);
        try {
            if(file.exists())
                file.delete();
            if(!file.createNewFile())
                return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        //String html = "<html><head><title>Title</title></head><body>This is random text.</body></html>";

        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] data = html.getBytes();
            out.write(data);
            out.close();
            Log.e("StorageWrapper", "createHtml File Saved : " + file.getPath());
        }/* catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/ catch (IOException e) {
            Log.e("StorageWrapper", "createHtml error: " + e.getMessage());
            return null;
        }

        return file;
    }

    public static File createImageFile1(Context context) throws IOException {
        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
        // Create an image file name
        DateFormat dateFormat = DateUtil.DATE_FORMAT;
        dateFormat.setTimeZone(TimeZone.getDefault());
        String timeStamp = dateFormat.format(new Date());
        //String timeStamp = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, Locale.getDefault()).format(new Date());//"yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                timeStamp,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        //return image;
    }

    /**
     * Supply a file in local dedicated folder of the new update to be downloaded
     * @param context application
     * @param fileName the name of the new update file
     * @return file from {@link Context#getExternalFilesDir(String)} to store the app download
     */
    public static File getFileToDownloadUpdateInto(Context context, String fileName) {
        File dir = context.getExternalFilesDir(Constants.APK_DIR);
        return new File(dir, fileName);
    }

    public static File createImageFile(Context context) throws IOException {
        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private static File createCompressedFile(Context context, String fileName) throws IOException {
        // Create an image file name
        String imageFileName = fileName + "_" + "compressed";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static String compressFile(Context context, String path) {
        if (path == null)
            return null;
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        FileOutputStream out;
        String filename = null;
        try {
            File compressedFile = StorageWrapper.createCompressedFile(context, new File(path).getName());
            filename = compressedFile.getAbsolutePath();

            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return filename;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(column_index);
            cursor.close();
            Log.e(TAG, "getRealPathFromURI, " + result);
            return result;
        }
        return contentUri.getPath();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFilesFromCamera(Context context, @NonNull List<String> filesNames) {
        for (String name: filesNames) {
            Uri uri = ExternalStorageHelper.getFileAbsolutePath(context, Environment.DIRECTORY_PICTURES, name);
            if (uri != null) {
                Log.e(TAG, "deleting " + name + ", " + new File(uri.getPath()).delete());
            }
        }
    }
}
