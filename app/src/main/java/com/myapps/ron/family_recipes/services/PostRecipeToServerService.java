package com.myapps.ron.family_recipes.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.utils.MyCallback;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PostRecipeToServerService extends IntentService {
    private static final String TAG = "service";
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POST_RECIPE = "com.myapps.ron.family_recipes.services.action.POST_RECIPE";
    private static final String ACTION_POST_IMAGES = "com.myapps.ron.family_recipes.services.action.POST_IMAGES";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.services.extra.PARAM2";


    public PostRecipeToServerService() {
        super("PostRecipeToServerService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPostRecipe(Context context, RecipeEntity recipe) {
        Log.e(TAG, "handle action post recipe");
        Intent intent = new Intent(context, PostRecipeToServerService.class);
        intent.setAction(ACTION_POST_RECIPE);
        intent.putExtra(EXTRA_PARAM1, recipe);
        //intent.putExtra(EXTRA_PARAM2, time);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPostImages(Context context, String id, ArrayList<String> files) {
        Intent intent = new Intent(context, PostRecipeToServerService.class);
        intent.setAction(ACTION_POST_IMAGES);
        intent.putStringArrayListExtra(EXTRA_PARAM1, files);
        intent.putExtra(EXTRA_PARAM2, id);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_POST_RECIPE.equals(action)) {
                final RecipeEntity recipe = intent.getParcelableExtra(EXTRA_PARAM1);
                //final String time = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPostRecipeSync(recipe);
                //handleActionPostRecipe(recipe);
            } else if (ACTION_POST_IMAGES.equals(action)) {
                final List<String> files = intent.getStringArrayListExtra(EXTRA_PARAM1);
                String id = intent.getStringExtra(EXTRA_PARAM2);
                handleActionPostImagesSync(id, files);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPostRecipeSync(final RecipeEntity recipe) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.e(TAG, "handle action post recipe");
        //maybe open a new thread
        APICallsHandler.postRecipe(new RecipeTO(recipe), AppHelper.getAccessToken(), new MyCallback<String>() {
            @Override
            public void onFinished(String urlForContent) {
                Log.e(TAG, "finished post pend, got a url, " + urlForContent);
                if (!"null".equals(urlForContent)) {
                    boolean fileUploaded = OnlineStorageWrapper.uploadRecipeFileSync(urlForContent, recipe.getRecipeFile());
                    if (fileUploaded) {
                        //sendIntentToUser(false, "recipe uploaded");
                        if (recipe.getFoodFiles() != null) {
                            uploadFoodFilesSync(recipe.getId(), recipe.getFoodFiles());
                        } else {
                            //no images to upload
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendIntentToUser(false, "recipe uploaded");
                                }
                            }, 2500);
                        }
                    }
                    else
                        sendIntentToUser(false, "recipe wasn't uploaded");
                }
                else
                    sendIntentToUser(false, "recipe wasn't uploaded");
            }
        });

        deleteAllLocalFiles(recipe);
    }


    private void uploadFoodFilesSync(String id, List<String> foodFiles) {
        Log.e(TAG, "uploading images");
        Log.e(TAG, "id = " + id + "\n files: " + foodFiles);
        //Synchronous request with retrofit 2.0
        List<String> urlsForFood = APICallsHandler.requestUrlsForFoodPicturesSync(id, foodFiles, AppHelper.getAccessToken());
        if (urlsForFood != null) {
            //upload the images to s3
            Log.e(TAG, "urls: " + urlsForFood);
            for (int i = 0; i < urlsForFood.size() && i < foodFiles.size(); i++) {
                Log.e(TAG, "uploading file #" + i);
                OnlineStorageWrapper.uploadFoodFileSync(urlsForFood.get(i), foodFiles.get(i));
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendIntentToUser(true, "images uploaded");
                }
            }, 2500);
        }
        Log.e(TAG, "urls are null");
    }

    private void sendIntentToUser(boolean update, String message) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_UPDATE_FROM_SERVICE);
        intent.putExtra("refresh", update);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    private void deleteAllLocalFiles(RecipeEntity recipe) {
        if (recipe.getFoodFiles() != null && !recipe.getFoodFiles().isEmpty()) {
            for (int i = 0; i < recipe.getFoodFiles().size(); i++) {
                new File(recipe.getFoodFiles().get(i)).delete();
            }
        }
        new File(recipe.getRecipeFile()).delete();
    }


    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPostImagesSync(String id, List<String> localPaths) {
        uploadFoodFilesSync(id, localPaths);
    }
}
