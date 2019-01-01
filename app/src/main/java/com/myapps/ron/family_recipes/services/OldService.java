package com.myapps.ron.family_recipes.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.utils.MyCallback;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.Constants;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class OldService extends IntentService {
    private static final String TAG = "service";
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POST_RECIPE = "com.myapps.ron.family_recipes.services.action.POST_RECIPE";
    private static final String ACTION_POST_IMAGES = "com.myapps.ron.family_recipes.services.action.POST_IMAGES";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.services.extra.PARAM2";


    public OldService() {
        super("OldService");
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
                handleActionPostRecipe(recipe);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPostRecipe(final RecipeEntity recipe) {
        this.recipe = recipe;
        Log.e(TAG, "handle action post recipe");
        //maybe open a new thread
        APICallsHandler.postRecipe(new RecipeTO(recipe), AppHelper.getAccessToken(), new MyCallback<String>() {
            @Override
            public void onFinished(String result) {
                Log.e(TAG, "finished post pend, got a url, " + result);
                if (!"null".equals(result)) {
                    /*boolean fileUploaded = OnlineStorageWrapper.uploadRecipeFileSync(result, recipe.getRecipeFile());
                    if (fileUploaded) {
                        sendIntentToUser(true, "recipe uploaded");
                        if (recipe.getFoodFiles() != null) {
                            uploadFoodFiles(recipe);
                        }
                    }
                    else
                        sendIntentToUser(false, "recipe wasn't uploaded");*/
                    OnlineStorageWrapper.uploadRecipeFile(result, recipe.getRecipeFile(), new MyCallback<Boolean>() {
                        @Override
                        public void onFinished(Boolean result) {
                            sendIntentToUser(result, "recipe uploaded");
                            if (recipe.getFoodFiles() != null) {
                                uploadFoodFiles(recipe);
                            }
                        }
                    });
                }
                else
                    sendIntentToUser(false, "recipe wasn't uploaded");
            }
        });
    }


    private int picturesUploaded;
    private RecipeEntity recipe = null;
    private List<String> urls;
    private MyCallback<Boolean> myCallback = new MyCallback<Boolean>() {
        @Override
        public void onFinished(Boolean result) {
            Log.e(TAG, "uploading file #" + picturesUploaded);
            if (picturesUploaded < urls.size() - 1) {
                picturesUploaded++;
                uploadImage(urls.get(picturesUploaded - 1), recipe.getFoodFiles().get(picturesUploaded - 1));
            }
        }
    };

    private void uploadFoodFiles(final RecipeEntity recipe) {
        picturesUploaded = 0;
        Log.e(TAG, "uploading images");
        //Asynchronous request with retrofit 2.0
        APICallsHandler.requestUrlsForFoodPictures(recipe.getId(), recipe.getFoodFiles(), AppHelper.getAccessToken(), new MyCallback<List<String>>() {
            @Override
            public void onFinished(final List<String> urlsForFood) {
                if (urlsForFood != null) {
                    urls = new ArrayList<>(urlsForFood);
                    //upload the images to s3
                    OnlineStorageWrapper.uploadFoodFile(urls.get(0), recipe.getFoodFiles().get(0), myCallback);
                }
                else
                    Log.e(TAG, "urls are null");
            }
        });

    }

    private void uploadImage(String url, String localPath) {
        OnlineStorageWrapper.uploadFoodFile(url, localPath, myCallback);
    }

    private void sendIntentToUser(boolean update, String message) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_UPDATE_FROM_SERVICE);
        intent.putExtra("refresh", update);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    private void deleteAllLocalFiles(RecipeEntity recipe) {

    }

    private MyCallback<Boolean> uploadRecipeCallback = new MyCallback<Boolean>() {
        @Override
        public void onFinished(Boolean result) {

        }
    };

    private MyCallback<Boolean> uploadImagesCalback = new MyCallback<Boolean>() {
        @Override
        public void onFinished(Boolean result) {
            Log.e(TAG, "did file uploaded ? " + result);
            sendIntentToUser(result, "images uploaded");
        }
    };

}
