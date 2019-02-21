package com.myapps.ron.family_recipes.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;

import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.dal.persistence.PendingRecipeDao;
import com.myapps.ron.family_recipes.model.PendingRecipe;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.myapps.ron.family_recipes.network.Constants.RESPONSE_KEY_RECIPE_ID;
import static com.myapps.ron.family_recipes.network.Constants.RESPONSE_KEY_URL;

public class PostEnqueuedRecipesService extends Service {
    private final String TAG = getClass().getSimpleName();

    static final String ACTION_POST_ENQUEUED_RECIPES = "com.myapps.ron.family_recipes.services.action.POST_ENQUEUED_RECIPES";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private int startId;
    private CompositeDisposable compositeDisposable;

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
                switch (action) {
                    case ACTION_POST_ENQUEUED_RECIPES:
                        handleActionPostRecipeFromQueue();
                        break;
                }
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        compositeDisposable = new CompositeDisposable();
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

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        serviceHandler.sendMessage(msg);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        /*Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart*/
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
        compositeDisposable.clear();
        serviceLooper.quit();
    }

    private void handleActionPostRecipeFromQueue() {
        //assuming network connection. Called from Worker that initiates only when connected
        if (AppHelper.getAccessToken() == null) {
            // subscribe for token
            Scheduler intentScheduler = AndroidSchedulers.from(serviceHandler.getLooper());
            compositeDisposable.add(AppHelper.currSessionObservable
                    .subscribeOn(intentScheduler)
                    .observeOn(intentScheduler)
                    .subscribe(next -> {
                        Log.e(TAG, "got info, " + next);
                        startPostPendingRecipesProcess();
                    }, error ->
                            Log.e(TAG, "got error, " + error.getMessage()), () ->
                            Log.e(TAG, "onComplete")));
        }
        else {
            // there is a valid token
            startPostPendingRecipesProcess();
        }
    }

    private void startPostPendingRecipesProcess() {
        // get records from db
        PendingRecipeDao pendingRecipeDao = Injection.providePendingRecipeDao(getApplicationContext());
        List<PendingRecipe> pendingRecipes = pendingRecipeDao.getAll();

        for (PendingRecipe recipe: pendingRecipes) {
            uploadRecipeSync(recipe);

            // delete from db when finish upload
            pendingRecipeDao.delete(recipe);
        }
        stopSelf(startId);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void uploadRecipeSync(final PendingRecipe recipe) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.e(TAG, "handle action post recipe");
        //maybe open a new thread
        APICallsHandler.postRecipe(recipe, AppHelper.getAccessToken(), results -> {
            if (results != null) {
                Log.e(TAG, "finished post pend, " + results.toString());
                boolean fileUploaded = OnlineStorageWrapper.uploadRecipeFileSync(results.get(RESPONSE_KEY_URL), recipe.getRecipeFile());
                if (fileUploaded) {
                    //sendIntentToUser(false, "recipe uploaded");
                    if (recipe.getFoodFiles() != null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            uploadFoodFilesSync(results.get(RESPONSE_KEY_RECIPE_ID)/*recipe.getLastModifiedDate()*/, recipe.getFoodFiles());
                        }
                    } else {
                        //no images to upload
                        new Handler().postDelayed(() -> Log.e(TAG, "recipe uploaded"), 2500);
                    }
                }
                else
                    Log.e(TAG, "recipe wasn't uploaded");
            }
            else
                Log.e(TAG, "recipe wasn't uploaded");
        });

        deleteLocalFiles(recipe.getFoodFiles());
        List<String> recipeFile = new ArrayList<>();
        recipeFile.add(recipe.getRecipeFile());
        deleteLocalFiles(recipeFile);
    }

    private void uploadFoodFilesSync(String id, List<String> foodFiles) {
        Log.e(TAG, "uploading images");
        Log.e(TAG, "id = " + id + "\n files: " + foodFiles);
        //Synchronous request with retrofit 2.0
        List<String> urlsForFood = APICallsHandler.requestUrlsForFoodPicturesSync(id, null, foodFiles.size(), AppHelper.getAccessToken());
        if (urlsForFood != null) {
            //upload the images to s3
            Log.e(TAG, "urls: " + urlsForFood);
            for (int i = 0; i < urlsForFood.size() && i < foodFiles.size(); i++) {
                Log.e(TAG, "uploading file #" + i);
                OnlineStorageWrapper.uploadFoodFileSync(urlsForFood.get(i), foodFiles.get(i));
            }
            new Handler().postDelayed(() -> Log.e(TAG, "images uploaded"), 2500);
        }
        Log.e(TAG, "urls are null");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteLocalFiles(List<String> files) {
        if (files != null && !files.isEmpty()) {
            for (int i = 0; i < files.size(); i++) {
                new File(files.get(i)).delete();
            }
        }
        //new File(recipe.getRecipeFile()).delete();
    }
}
