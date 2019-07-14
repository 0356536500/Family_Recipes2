package com.myapps.ron.family_recipes.background.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.layout.modelTO.PendingRecipeTO;
import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.logic.repository.PendingRecipeRepository;
import com.myapps.ron.family_recipes.logic.repository.RecipeRepository;
import com.myapps.ron.family_recipes.logic.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.PendingRecipeEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

import static com.myapps.ron.family_recipes.layout.Constants.RESPONSE_KEY_RECIPE_ID;
import static com.myapps.ron.family_recipes.layout.Constants.RESPONSE_KEY_RECIPE_MODIFIED;

public class PostEnqueuedRecipesService extends Service {
    private static final String TAG = PostEnqueuedRecipesService.class.getSimpleName();

    static final String ACTION_POST_RECIPE_FROM_QUEUE = "com.myapps.ron.family_recipes.background.services.action.POST_ENQUEUED_RECIPES";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private int startId;

    public static void startActionPostRecipeFromQueue(Context context) {
        Log.e(TAG, "handle action post recipe from queue");
        Intent intent = new Intent(context, PostEnqueuedRecipesService.class);
        intent.setAction(ACTION_POST_RECIPE_FROM_QUEUE);
        context.startService(intent);
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage");
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            startId = msg.arg1;
            onHandleIntent((Intent)msg.obj);
            //stopSelf(msg.arg1);
        }
    }

    public PostEnqueuedRecipesService() {
        super();
    }

    private void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent");
        //serviceHandler.postDelayed(() -> stopSelf(startId), 2000);
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                if (ACTION_POST_RECIPE_FROM_QUEUE.equals(action)) {
                    startPostPendingRecipesProcess();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("Service[" + getClass().getSimpleName() + "]",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "service starting");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        serviceLooper.quit();
    }

    private void startPostPendingRecipesProcess() {
        // get records from db
        PendingRecipeRepository pendingRepository = Injection.providePendingRecipeRepository(getApplicationContext());
        List<PendingRecipeEntity> pendingRecipes = pendingRepository.getAll();

        if (pendingRecipes != null) {
            for (PendingRecipeEntity recipe : pendingRecipes) {
                uploadRecipeSync(recipe);

                // delete from db when finish upload
                pendingRepository.delete(recipe);
            }
        }
        stopSelf(startId);
    }

    /**
     * Handle action upload recipe in the provided background thread with the provided
     * parameters.
     */
    private void uploadRecipeSync(final PendingRecipeEntity recipe) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RecipeRepository repository = Injection.provideRecipeRepository(getApplicationContext());

        Log.e(TAG, "handle action post recipe");
        String errorMessage = getApplicationContext().getString(R.string.post_recipe_error) + recipe.getName();
        Response<Map<String, String>> response = APICallsHandler.postRecipeSync(new PendingRecipeTO(recipe), AppHelper.getAccessToken());
        if (response != null) {
            if (response.isSuccessful() && response.body() != null) {
                Map<String, String> results = response.body();
                Log.e(TAG, "finished post pend, " + results.toString());
                if (recipe.getFoodFiles() != null) {
                    uploadFoodFilesSync(
                            results.get(RESPONSE_KEY_RECIPE_ID),
                            results.get(RESPONSE_KEY_RECIPE_MODIFIED),
                            compressFiles(recipe.getFoodFiles())
                    );
                } else {
                    //no images to upload
                    new Handler().postDelayed(() -> Log.e(TAG, "recipe uploaded"), 2500);
                }

            } else {
                try {
                    if (response.errorBody() != null) {
                        String message = response.errorBody().string();
                        repository.dispatchInfo.onNext(message);
                    } else
                        repository.dispatchInfo.onNext(errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                    repository.dispatchInfo.onNext(errorMessage);
                }
            }
        } else {
            Log.e(TAG, "recipe wasn't uploaded");
            repository.dispatchInfo.onNext(errorMessage);
        }

        //maybe open a new thread
        /*APICallsHandler.postRecipe(new PendingRecipeTO(recipe), AppHelper.getAccessToken(), results -> {
            if (results != null) {
                Log.e(TAG, "finished post pend, " + results.toString());
                if (recipe.getFoodFiles() != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        uploadFoodFilesSync(results.get(RESPONSE_KEY_RECIPE_ID), results.get(RESPONSE_KEY_RECIPE_MODIFIED), recipe.getFoodFiles());
                    }
                } else {
                    //no images to upload
                    new Handler().postDelayed(() -> Log.e(TAG, "recipe uploaded"), 2500);
                }
            }
            else
                Log.e(TAG, "recipe wasn't uploaded");
        });
        deleteLocalFiles(recipe.getFoodFiles());*/
    }

    @Nullable
    private List<String> compressFiles(List<String> paths) {
        List<String> compressedFiles = null;
        if (paths != null) {
            compressedFiles = new ArrayList<>();
            for (String path: paths) {
                String compressedPath = StorageWrapper.compressFile(this, path);
                if (compressedPath != null) {
                    compressedFiles.add(compressedPath);
                }
            }
        }
        return compressedFiles;
    }

    private void uploadFoodFilesSync(String id, String lastModifiedDate, @Nullable List<String> foodFiles) {
        Log.e(TAG, "uploading images");
        RecipeRepository repository = Injection.provideRecipeRepository(getApplicationContext());

        if (foodFiles == null) {
            repository.dispatchInfo.onNext(getApplicationContext().getString(R.string.post_images_error));
            return;
        }
        Log.e(TAG, "id = " + id + "\n files: " + foodFiles);

        //Synchronous request with retrofit 2.0
        Response<List<String>> response = APICallsHandler.requestUrlsForFoodPicturesSync(id, lastModifiedDate, foodFiles.size(), AppHelper.getAccessToken());
        if (response != null) {
            // check if successful
            if (response.isSuccessful() && response.body() != null) {
                List<String> urlsForFood = response.body();

                //upload the images to s3
                Log.e(TAG, "urls: " + urlsForFood);
                for (int i = 0; i < urlsForFood.size() && i < foodFiles.size(); i++) {
                    Log.e(TAG, "uploading file #" + i);
                    OnlineStorageWrapper.uploadFoodFileSync(urlsForFood.get(i), foodFiles.get(i));
                }
                //deleteLocalFiles(foodFiles);
                new Handler().postDelayed(() -> Log.e(TAG, "images uploaded"), 2500);
            } else {
                try {
                    if (response.errorBody() != null) {
                        String message = response.errorBody().string();
                        repository.dispatchInfo.onNext(message);
                    } else
                        repository.dispatchInfo.onNext(getApplicationContext().getString(R.string.post_images_error));
                } catch (IOException e) {
                    e.printStackTrace();
                    repository.dispatchInfo.onNext(getApplicationContext().getString(R.string.post_images_error));
                }
            }
        } else
            Log.e(TAG, "urls are null");
    }

    /*@SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteLocalFiles(List<String> files) {
        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                new File(files.get(i)).delete();
            }
        }
        //new File(recipe.getContent()).delete();
    }*/
}
