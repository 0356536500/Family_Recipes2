package com.ronginat.family_recipes.logic.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static com.ronginat.family_recipes.logic.Constants.COMPRESSION_MAX;
import static com.ronginat.family_recipes.logic.Constants.COMPRESSION_MIN;
import static com.ronginat.family_recipes.logic.Constants.COMPRESSION_REQUIRED;

/**
 * Created by ronginat on 18/08/2019
 */
public class StorageHelper {
    private static final String TAG = StorageHelper.class.getSimpleName();

    @NonNull
    public static File createImageFile(Context context) throws IOException {
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

    /*private static File createFileForCloning(Context context, @NonNull Uri uri) throws IOException {
        String extension = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme()) && uri.getPath() != null) {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return createImageFile(context, "." + extension);
    }*/

    public static Flowable<Map.Entry<Integer, String>> copyFiles(Context context, List<Uri> list) {
        if (list != null && list.size() > 0) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            return Flowable.create(emitter -> {
                List<Callable<Object>> callableList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    int finalI = i;
                    callableList.add(Executors.callable(() -> emitter.onNext(
                            new AbstractMap.SimpleEntry<>(finalI, copyFile(context, list.get(finalI))))));
                }
                /*List<Future<Object>> results = */executor.invokeAll(callableList);
                emitter.onComplete();

            }, BackpressureStrategy.BUFFER);
        }
        return Flowable.error(new Throwable("no files"));
    }

    private static String copyFile(Context context, @NonNull Uri uri) {
        String path = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            File dest = createImageFile(context);
            in = context.getContentResolver().openInputStream(uri);
            if (in != null) {
                out = new FileOutputStream(dest);
                byte[] buf = new byte[4 * 1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

                out.close();
                in.close();

                // rotate image if required
                in = context.getContentResolver().openInputStream(uri);
                if (in != null) {
                    CrashLogger.e(TAG, "original size = " + dest.length());
                    rotateAndSave(in, dest, getImageRotationDegree(context, uri));
                    in.close();
                }

                path = dest.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CrashLogger.logException(e);
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    private static int getImageRotationDegree(Context context, @NonNull Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                ExifInterface oldExif = new ExifInterface(in);
                return oldExif.getRotationDegrees();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Rotate an image if required.
     * Save the resulted Bitmap to same file after manipulation
     */
    private static void rotateAndSave(@NonNull InputStream src, @NonNull File dest, int degree) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeStream(src);
        if (degree > 0) {
            bitmap = rotateImage(bitmap, degree);
        }
        saveBitmap(bitmap, dest);
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private static void saveBitmap(@NonNull Bitmap bitmap, @NonNull File dest) throws IOException {
        if (dest.length() > COMPRESSION_REQUIRED) {
            // need to compress
            compressFile(bitmap, dest);
        } else {
            try (FileOutputStream out = new FileOutputStream(dest)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 97, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compress a file with {@link Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)} method.
     * Goal is to reach as close as possible to target size {@link com.ronginat.family_recipes.logic.Constants#COMPRESSION_REQUIRED}
     */
    private static void compressFile(Bitmap bitmap, File dest) throws IOException {
        final int qualityDiff = 10;

        int quality = (int)(((float)COMPRESSION_REQUIRED / (float)dest.length()) * 200);
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
        //CrashLogger.e(TAG, "compressed size = " + dest.length());
    }


    /*
     * Copy image orientation when compressing an existing file
     */
    /*private static int copyExif(@NonNull InputStream src, String dest) {
        try {
            ExifInterface oldExif = new ExifInterface(src);
            int exifOrientation = oldExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            String date = oldExif.getAttribute(ExifInterface.TAG_DATETIME);

            ExifInterface newExif = new ExifInterface(dest);
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(exifOrientation));
            newExif.setAttribute(ExifInterface.TAG_DATETIME, date);
            newExif.saveAttributes();
            return oldExif.getRotationDegrees();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }*/
}
