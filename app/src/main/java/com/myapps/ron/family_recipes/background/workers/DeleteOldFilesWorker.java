package com.myapps.ron.family_recipes.background.workers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.myapps.ron.family_recipes.network.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.myapps.ron.family_recipes.dal.Constants.MIN_FOLDER_SIZE_TO_START_DELETING_CONTENT;

/**
 * Created by ronginat on 31/03/2019.
 */
public class DeleteOldFilesWorker extends Worker {

    private final String TAG = getClass().getSimpleName();
    public DeleteOldFilesWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.e(TAG, "doWork");
        File imagesFolder = getApplicationContext().getExternalFilesDir(Constants.FOOD_DIR);
        if (imagesFolder != null && folderSize(imagesFolder) > MIN_FOLDER_SIZE_TO_START_DELETING_CONTENT) {
            Log.e(TAG, "images name = " + imagesFolder.getName() + ", size = " + imagesFolder.length());
            File[] files = imagesFolder.listFiles();
            Log.e(TAG, Arrays.asList(files).toString());
            Arrays.sort(files, (file, t1) -> {
                long result = getFileTimeWrapBuildVersion(file) - getFileTimeWrapBuildVersion(t1);
                if (result > 0)
                    return 1;
                if (result < 0)
                    return -1;
                return 0;
            });
            Log.e(TAG, "images after sorting");
            Log.e(TAG, Arrays.asList(files).toString());

            List<File> filesToDelete = new ArrayList<>();
            long medianTime = getMedianTime(files);
            Log.e(TAG, "median = " + medianTime);
            for (File file: files) {
                if (getFileTimeWrapBuildVersion(file) < medianTime) {
                    filesToDelete.add(file);
                }
            }

            deleteFiles(filesToDelete);
        }
        return Result.success();
    }

    private void deleteFiles(List<File> filesToDelete) {
        for (File file: filesToDelete) {
                Log.e(getClass().getSimpleName(), "deleting " + file.getName()
                        + ", " + file.delete());
        }
    }

    /**
     * @param files sorted
     * @return median time
     */
    private long getMedianTime(@NonNull File[] files) {
        if (files.length % 2 == 0)
            return (files[files.length / 2].lastModified() + files[files.length / 2 - 1].lastModified()) / 2;
        else
            return files[files.length / 2].lastModified();
    }

    private long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    private long getFileTimeWrapBuildVersion(@NonNull File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getLastAccessedTime(file.toPath());
        } else {
            return getLastModifiedTime(file);
        }
    }

    private long getLastModifiedTime(@NonNull File file) {
        //Log.e(getClass().getSimpleName(), "getLastModifiedTime, name = " + file.getName() + ", date = " + new Date(file.lastModified()).toString());
        return file.lastModified();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private long getLastAccessedTime(@NonNull Path file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            //Log.e(getClass().getSimpleName(), "getLastAccessedTime, name = " + file.toFile().getName() + ", date = " + Date.from(attr.lastAccessTime().toInstant()).toString());
            return attr.lastAccessTime().toMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }

   /* public static OneTimeWorkRequest createPostRecipesWorker() {
        // Create a Constraints object that defines when the task should run
        Constraints.Builder myConstraintsBuilder = new Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            myConstraintsBuilder.setRequiresDeviceIdle(true);
        }

        // then create a OneTimeWorkRequest that uses those constraints

        return new OneTimeWorkRequest.Builder(DeleteOldFilesWorker.class)
                .setConstraints(myConstraintsBuilder.build())
                .build();
    }*/

    public static PeriodicWorkRequest createPostRecipesWorker() {
        // Create a Constraints object that defines when the task should run
        Constraints.Builder myConstraintsBuilder = new Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            myConstraintsBuilder.setRequiresDeviceIdle(true);
        }

        // then create a OneTimeWorkRequest that uses those constraints

        return new PeriodicWorkRequest.Builder(DeleteOldFilesWorker.class, 30, TimeUnit.DAYS)
                .setConstraints(myConstraintsBuilder.build())
                .build();
    }
}
