package com.ronginat.family_recipes.logic.storage;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.layout.MiddleWareForNetwork;
import com.ronginat.family_recipes.layout.S3.OnlineStorageWrapper;
import com.ronginat.family_recipes.utils.MyCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;

import static com.ronginat.family_recipes.logic.Constants.COMPRESSION_MAX;
import static com.ronginat.family_recipes.logic.Constants.COMPRESSION_MIN;
import static com.ronginat.family_recipes.logic.Constants.COMPRESSION_REQUIRED;

public class StorageWrapper {
    //private static final String TAG = StorageWrapper.class.getSimpleName();

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
            return Single.error(new Throwable(context.getString(R.string.no_internet_message)));
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

    /**
     * Supply a file in local dedicated folder of the new update to be downloaded
     * @param context application
     * @param fileName the name of the new update file
     * @return file from {@link Context#getExternalFilesDir(String)} to store the app download
     */
    @SuppressWarnings("JavadocReference")
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
        File storageDir = context.getExternalFilesDir(Constants.TEMP_IMAGES_DIR);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @NonNull
    private static File createCompressedFile(Context context, String fileName) throws IOException {
        // Create an image file name
        String imageFileName = fileName + "_" + "compressed";
        File storageDir = context.getExternalFilesDir(Constants.TEMP_IMAGES_DIR); // file_path.xml -> my_images
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * Compress a file with {@link Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)} method.
     * Goal is to reach as close as possible to target size {@link com.ronginat.family_recipes.logic.Constants#COMPRESSION_REQUIRED}
     * @param context application context
     * @param path of original file
     * @return path of compressed file saved in app directory
     */
    public static String compressFile(Context context, String path) {
        if (path == null)
            return null;

        try {
            //String fileName = Uri.parse(fileName).getLastPathSegment();
            //Log.e(TAG, "input path: " + path);
            File originalFile = new File(path);
            if (!originalFile.exists())
                return null;
            File compressedFile = StorageWrapper.createCompressedFile(context, originalFile.getName());
            Uri originUri = Uri.fromFile(originalFile);//ExternalStorageHelper.getFileUri(context, Constants.TEMP_IMAGES_DIR, path);
            if (originUri == null || originUri.getPath() == null)
                return null;

            //Log.e(TAG, "original file path: " + originUri.getPath());
            //Log.e(TAG, path + ", size = " + originalFile.length());

            if (originalFile.length() > COMPRESSION_REQUIRED) {
                // need to compress
                Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getPath());
                String newPath = compressFile(bitmap, originalFile, compressedFile);
                copyExif(path, newPath);
                return newPath;
            } else {
                // file is small enough
                String newPath = copyFile(originalFile, compressedFile);
                copyExif(path, newPath);
                return newPath;
            }
        } catch (IOException e) {
            //Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static String compressFile(Bitmap bitmap, File src, File dest) throws IOException {
        final int qualityDiff = 10;

        int quality = (int)(((float)COMPRESSION_REQUIRED / (float)src.length()) * 200);
        quality = quality > 100 ? 100 : quality;

        FileOutputStream out = new FileOutputStream(dest);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        out.close();
        if (dest.length() < COMPRESSION_MIN && quality + qualityDiff <= 100) {
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality + qualityDiff, out);
        } else if (dest.length() > COMPRESSION_MAX && quality - (qualityDiff /2 ) >= 0) {
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality - (qualityDiff / 2), out);
        }
        out.close();
        //Log.e(TAG, dest.getAbsolutePath() + ", size = " + dest.length());
        return dest.getAbsolutePath();
    }

    private static String copyFile(File src, File dest) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dest);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        //FileUtils.copy(in, out);

        in.close();
        out.close();
        //Log.e(TAG, "dest size = " + dest.length());
        return dest.getAbsolutePath();
    }

    /**
     * Copy image orientation when compressing an existing file
     */
    private static void copyExif(String oldPath, String newPath) {
        try {
            ExifInterface oldExif = new ExifInterface(oldPath);
            String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);
            String date = oldExif.getAttribute(ExifInterface.TAG_DATETIME);

            ExifInterface newExif = new ExifInterface(newPath);
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
            newExif.setAttribute(ExifInterface.TAG_DATETIME, date);
            newExif.saveAttributes();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        //TODO: overcome MediaStore.Images.Media.DATA deprecation issue. https://developer.android.com/reference/android/provider/MediaStore.MediaColumns#DATA
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        //Cursor cursor = context.getContentResolver().openFileDescriptor(contentUri, "r");
        if (cursor != null) {
            try {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                //String result = cursor.getString(column_index);
                //Log.e(TAG, "getRealPathFromURI, " + result);
                return cursor.getString(column_index);
            } finally {
                cursor.close();
            }
        }
        return contentUri.getPath();
    }

    /**
     * Rotate an image if required.
     * Save the resulted Bitmap to same file after manipulation
     * @param selectedImage Image URI
     */
    public static void rotateImageIfRequired(Context context, @NonNull Uri selectedImage) {
        int rotation = getImageRotation(context, selectedImage);
        if (rotation > 0) {
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImage.getPath());
            bitmap = rotateImage(bitmap, rotation);
            saveBitmap(bitmap, selectedImage.getPath());
        }
    }

    private static int getImageRotation(Context context, @NonNull Uri selectedImage) {
        int rotation = 0;
        try {
            ExifInterface ei;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                InputStream input = context.getContentResolver().openInputStream(selectedImage);
                if (input == null)
                    return -1;
                ei = new ExifInterface(input);
            }
            else if (selectedImage.getPath() != null)
                ei = new ExifInterface(selectedImage.getPath());
            else
                return -1;

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return rotation;
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private static void saveBitmap(Bitmap bitmap, String dest) {
        try (FileOutputStream out = new FileOutputStream(dest)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFilesFromCamera(Context context, @NonNull List<String> filesNames) {
        for (String name: filesNames) {
            Uri uri = ExternalStorageHelper.getFileAbsolutePath(context, Constants.TEMP_IMAGES_DIR, name);
            if (uri != null && uri.getPath() != null) {
                new File(uri.getPath()).delete();
                //Log.e(TAG, "deleting " + name + ", " + new File(uri.getPath()).delete());
            }
        }
    }
}
