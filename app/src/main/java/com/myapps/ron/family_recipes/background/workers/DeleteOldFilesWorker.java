package com.myapps.ron.family_recipes.background.workers;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.logic.repository.RecipeRepository;
import com.myapps.ron.family_recipes.model.AccessEntity;
import com.myapps.ron.family_recipes.model.AccessEntity.RecipeAccess;
import com.myapps.ron.family_recipes.layout.Constants;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.myapps.ron.family_recipes.logic.Constants.MIN_APK_FOLDER_SIZE_TO_START_DELETING_CONTENT;
import static com.myapps.ron.family_recipes.logic.Constants.MIN_FOOD_FOLDER_SIZE_TO_START_DELETING_CONTENT;
import static com.myapps.ron.family_recipes.logic.Constants.MIN_RECIPE_RECORDS_COUNT_TO_START_DELETING_CONTENT;
import static com.myapps.ron.family_recipes.logic.Constants.MIN_THUMB_FOLDER_SIZE_TO_START_DELETING_CONTENT;
import static com.myapps.ron.family_recipes.logic.Constants.TARGET_FOOD_FOLDER_SIZE_AFTER_DELETING_CONTENT;
import static com.myapps.ron.family_recipes.logic.Constants.TARGET_RECIPE_REDORDS_COUNT_AFTER_DELETING_CONTENT;
import static com.myapps.ron.family_recipes.logic.Constants.TARGET_THUMB_FOLDER_SIZE_AFTER_DELETING_CONTENT;

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
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_FOOD_FOLDER_SIZE_TO_START_DELETING_CONTENT) {
                deleteFilesByDb(dir, AccessEntity.KEY_ACCESSED_IMAGES, dirSize, TARGET_FOOD_FOLDER_SIZE_AFTER_DELETING_CONTENT);
            }
        }
        int contentCount = repository.getRecipeContentDataCount();
        if (contentCount > MIN_RECIPE_RECORDS_COUNT_TO_START_DELETING_CONTENT) {
            deleteRecords(AccessEntity.KEY_ACCESSED_CONTENT, contentCount, TARGET_RECIPE_REDORDS_COUNT_AFTER_DELETING_CONTENT);
        }
        /*dir = getApplicationContext().getExternalFilesDir(Constants.RECIPES_DIR);
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_RECIPE_FOLDER_SIZE_TO_START_DELETING_CONTENT) {
                deleteFilesByDb(dir, AccessEntity.KEY_ACCESSED_CONTENT, dirSize, TARGET_RECIPE_FOLDER_SIZE_AFTER_DELETING_CONTENT);
            }
        }*/
        dir = getApplicationContext().getExternalFilesDir(Constants.THUMB_DIR);
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_THUMB_FOLDER_SIZE_TO_START_DELETING_CONTENT) {
                deleteFilesByDb(dir, AccessEntity.KEY_ACCESSED_THUMBNAIL, dirSize, TARGET_THUMB_FOLDER_SIZE_AFTER_DELETING_CONTENT);
            }
        }
        dir = getApplicationContext().getExternalFilesDir(Constants.APK_DIR);
        if (dir != null) {
            long dirSize = folderSize(dir);
            if (dirSize > MIN_APK_FOLDER_SIZE_TO_START_DELETING_CONTENT) { // 2 apk file is larger than 20MB
                deleteFilesKeepLastModified(dir);
            }
        }

        return Result.success();
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

            Log.e(TAG, accessKey + ", deleted " + deleteCount + " files");
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

            Log.e(TAG, accessKey + ", deleted " + deleteCount + " files");
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

    /*private void deleteFilesByAccessTime(@NonNull File imagesFolder) {
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

    private void deleteFiles(List<File> filesToDelete) {
        for (File file: filesToDelete) {
                Log.e(getClass().getSimpleName(), "deleting " + file.getName()
                        + ", " + file.delete());
        }
    }*/

    /*
     * @param files sorted
     * @return median time
     *
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
            return getLastAccessedTimeLegacy(file.getPath());
        }
    }

    //private long getLastModifiedTime(@NonNull File file) {
        //Log.e(getClass().getSimpleName(), "getLastModifiedTime, name = " + file.getName() + ", date = " + new Date(file.lastModified()).toString());
      //  return file.lastModified();
    //}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private long getLastAccessedTimeLegacy(@NonNull String path) {
        try {
            return Os.stat(path).st_atime;
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        return 0L;
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(t2);

        //SimpleDateFormat formatter =  new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");
        //String formattedDate = formatter.format(calendar.getTime());
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
    }*/

   /* public static OneTimeWorkRequest getOneRecipeWorker() {
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
