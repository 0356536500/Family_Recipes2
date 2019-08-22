package com.ronginat.family_recipes.background.workers;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.logic.Injection;
import com.ronginat.family_recipes.logic.repository.RecipeRepository;
import com.ronginat.family_recipes.model.AccessEntity;
import com.ronginat.family_recipes.model.AccessEntity.RecipeAccess;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.ronginat.family_recipes.logic.Constants.MIN_APK_FOLDER_SIZE_TO_START_DELETING_CONTENT;
import static com.ronginat.family_recipes.logic.Constants.MIN_FOOD_FOLDER_SIZE_TO_START_DELETING_CONTENT;
import static com.ronginat.family_recipes.logic.Constants.MIN_RECIPE_RECORDS_COUNT_TO_START_DELETING_CONTENT;
import static com.ronginat.family_recipes.logic.Constants.MIN_THUMB_FOLDER_SIZE_TO_START_DELETING_CONTENT;
import static com.ronginat.family_recipes.logic.Constants.TARGET_FOOD_FOLDER_SIZE_AFTER_DELETING_CONTENT;
import static com.ronginat.family_recipes.logic.Constants.TARGET_RECIPE_RECORDS_COUNT_AFTER_DELETING_CONTENT;
import static com.ronginat.family_recipes.logic.Constants.TARGET_THUMB_FOLDER_SIZE_AFTER_DELETING_CONTENT;

/**
 * Created by ronginat on 31/03/2019.
 */
public class DeleteOldFilesWorker extends Worker {

    private final String TAG = getClass().getSimpleName();
    private final RecipeRepository repository;

    public DeleteOldFilesWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.repository = Injection.provideRecipeRepository(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        //Log.e(TAG, "doWork");
        File dir = getApplicationContext().getExternalFilesDir(Constants.FOOD_DIR);

        // ### delete images ###
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_FOOD_FOLDER_SIZE_TO_START_DELETING_CONTENT) {
                dirSize = deleteDanglingImages(dir, dirSize, repository::findIfImageIsDangling);
                if (dirSize > MIN_FOOD_FOLDER_SIZE_TO_START_DELETING_CONTENT)
                    deleteFilesByDb(dir, AccessEntity.KEY_ACCESSED_IMAGES, dirSize, TARGET_FOOD_FOLDER_SIZE_AFTER_DELETING_CONTENT);
            }
        }

        // ### delete ContentEntity ###
        int contentCount = repository.getRecipeContentDataCount();
        if (contentCount > MIN_RECIPE_RECORDS_COUNT_TO_START_DELETING_CONTENT) {
            deleteRecords(AccessEntity.KEY_ACCESSED_CONTENT, contentCount, TARGET_RECIPE_RECORDS_COUNT_AFTER_DELETING_CONTENT);
        }

        // ### delete thumbnails ###
        dir = getApplicationContext().getExternalFilesDir(Constants.THUMB_DIR);
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_THUMB_FOLDER_SIZE_TO_START_DELETING_CONTENT) {
                dirSize = deleteDanglingImages(dir, dirSize, repository::findIfThumbnailIsDangling);
                if (dirSize > MIN_THUMB_FOLDER_SIZE_TO_START_DELETING_CONTENT)
                    deleteFilesByDb(dir, AccessEntity.KEY_ACCESSED_THUMBNAIL, dirSize, TARGET_THUMB_FOLDER_SIZE_AFTER_DELETING_CONTENT);
            }
        }
        // ### delete apk ###
        dir = getApplicationContext().getExternalFilesDir(Constants.APK_DIR);
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_APK_FOLDER_SIZE_TO_START_DELETING_CONTENT) { // 2 apk file is larger than 10MB
                deleteFilesKeepLastModified(dir);
            }
        }

        return Result.success();
    }

    private long deleteDanglingImages(@NonNull File dir, long originalSize, Dangling dangling) {
        File[] files = dir.listFiles();
        long modifiedSize = originalSize, deleteCount = 0L;
        if (files != null) {
            for (File file: files) {
                if (dangling.isDangling(file.getName())) {
                    long currentFileSize = file.length();
                    if (file.delete()) {
                        deleteCount++;
                        modifiedSize -= currentFileSize;
                    }
                }
            }
        }
        CrashLogger.e(TAG, String.format(Locale.getDefault(),
                "DIR {%s} - delete count = %d - deleted storage = %d", dir.getName(), deleteCount, originalSize - modifiedSize));
        return modifiedSize;
    }

    @SuppressWarnings("SameParameterValue")
    private void deleteRecords(@NonNull String accessKey, long originalRowCount, long targetRowCount) {
        List<RecipeAccess> recipeAccesses = repository.getRecipesAccessesOrderBy(accessKey);
        if (recipeAccesses != null) {
            int deleteCount = 0;
            for (RecipeAccess access : recipeAccesses) {
                Object object = access.getFileNameByAccessKey(accessKey);
                if (object.getClass().equals(String.class)) {
                    String recipeId = (String) object;
                    repository.deleteRecipeContentById(recipeId);
                    deleteCount++;
                    originalRowCount -= 1;
                    repository.upsertRecipeAccess(access.id, accessKey, null);
                }
                if (originalRowCount <= targetRowCount)
                    break;
            }

            CrashLogger.e(TAG, accessKey + ", deleted " + deleteCount + " files");
        }
    }

    private void deleteFilesByDb(@NonNull File dir, @NonNull String accessKey, long originalSize, long targetSize) {
        long modifiedSize = originalSize;
        List<RecipeAccess> recipeAccesses = repository.getRecipesAccessesOrderBy(accessKey);
        if (recipeAccesses != null) {
            int deleteCount = 0;
            for (RecipeAccess access : recipeAccesses) {
                Object object = access.getFileNameByAccessKey(accessKey);
                if (object.getClass().equals(String.class)) {
                    String fileName = (String) object;
                    File file = new File(dir, fileName);
                    if (file.exists()) {
                        long currentFileSize = file.length();
                        if (file.delete()) {
                            modifiedSize -= currentFileSize;
                            deleteCount++;
                            repository.upsertRecipeAccess(access.id, accessKey, null);
                        }
                    }

                } else if (object instanceof List<?>) {
                    List<String> files = (List<String>) object;
                    for (String fileName : files) {
                        File file = new File(dir, fileName);
                        if (file.exists()) {
                            long currentFileSize = file.length();
                            if (file.delete()) {
                                modifiedSize -= currentFileSize;
                                deleteCount++;
                                repository.upsertRecipeAccess(access.id, accessKey, null);
                            }
                        }
                    }
                }
                if (modifiedSize <= targetSize)
                    break;
            }

            CrashLogger.e(TAG, accessKey + ", deleted " + deleteCount + " files");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteFilesKeepLastModified(@NonNull File dir) {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        Arrays.sort(files, (file, other) -> { // ASC order, last modified is last
            long result = file.lastModified() - other.lastModified();
            if (result > 0)
                return 1;
            if (result < 0)
                return -1;
            return 0;
        });

        for (int i = 0; i < files.length - 1; i++) {
            files[i].delete();
        }
    }

    private long folderSize(@NonNull File directory) {
        long length = 0;
        File[] listFiles = directory.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile())
                    length += file.length();
            }
        }
        return length;
    }


   /*private static final int SCHEDULE_HOUR = 21;
   private static long getMillisForInitialDelay() {
       Calendar now = Calendar.getInstance();
       Calendar later = Calendar.getInstance();
       later.setTime(now.getTime());
       *//*if (now.get(Calendar.HOUR_OF_DAY) < SCHEDULE_HOUR) {
           return later.getTimeInMillis() - now.getTimeInMillis();
       } *//*
       if (now.get(Calendar.HOUR_OF_DAY) >= SCHEDULE_HOUR){
           later.add(Calendar.DAY_OF_MONTH, 1);
       }
       later.set(Calendar.MINUTE, 0);
       later.set(Calendar.HOUR_OF_DAY, SCHEDULE_HOUR);
       return later.getTimeInMillis() - now.getTimeInMillis();
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
                //.setInitialDelay(getMillisForInitialDelay(), TimeUnit.MILLISECONDS)
                .build();
    }


    private interface Dangling {
        boolean isDangling(String name);
    }
}