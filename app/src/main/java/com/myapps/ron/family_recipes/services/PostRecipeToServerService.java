package com.myapps.ron.family_recipes.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.dal.persistence.PendingRecipeDao;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.PendingRecipe;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

import static com.myapps.ron.family_recipes.network.Constants.RESPONSE_KEY_RECIPE_ID;
import static com.myapps.ron.family_recipes.network.Constants.RESPONSE_KEY_URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PostRecipeToServerService extends IntentService {
    private static final String TAG = PostRecipeToServerService.class.getSimpleName();
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POST_RECIPE = "com.myapps.ron.family_recipes.services.action.POST_RECIPE";
    private static final String ACTION_POST_IMAGES = "com.myapps.ron.family_recipes.services.action.POST_IMAGES";
    private static final String ACTION_POST_RECIPE_FROM_QUEUE = "com.myapps.ron.family_recipes.services.action.POST_RECIPE_FROM_QUEUE";

    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.services.extra.PARAM2";
    private static final String EXTRA_PARAM3 = "com.myapps.ron.family_recipes.services.extra.PARAM3";


    public PostRecipeToServerService() {
        super("PostRecipeToServerService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
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
    public static void startActionPostImages(Context context, String id, String lastModifiedDate, List<String> files) {
        Intent intent = new Intent(context, PostRecipeToServerService.class);
        intent.setAction(ACTION_POST_IMAGES);
        intent.putStringArrayListExtra(EXTRA_PARAM1, new ArrayList<>(files));
        intent.putExtra(EXTRA_PARAM2, id);
        intent.putExtra(EXTRA_PARAM3, lastModifiedDate);
        context.startService(intent);
    }

    public static void startActionPostRecipeFromQueue(Context context) {
        Log.e(TAG, "handle action post recipe from queue");
        Intent intent = new Intent(context, PostRecipeToServerService.class);
        intent.setAction(ACTION_POST_RECIPE_FROM_QUEUE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_POST_RECIPE:
                        final RecipeEntity recipe = intent.getParcelableExtra(EXTRA_PARAM1);
                        //final String time = intent.getStringExtra(EXTRA_PARAM2);
                        handleActionPostRecipeSync(recipe);
                        break;
                    case ACTION_POST_IMAGES:
                        final List<String> files = intent.getStringArrayListExtra(EXTRA_PARAM1);
                        String id = intent.getStringExtra(EXTRA_PARAM2);
                        String lastModifiedDate = intent.getStringExtra(EXTRA_PARAM3);
                        handleActionPostImagesSync(id, lastModifiedDate, compressFiles(files));
                        //deleteLocalFiles(files);
                        break;
                    case ACTION_POST_RECIPE_FROM_QUEUE:
                        handleActionPostRecipeFromQueue();
                        break;
                }
            }
        }
    }

    /**
     * Handle action {@link PostRecipeToServerService#ACTION_POST_RECIPE} in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPostRecipeSync(final RecipeEntity recipe) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.e(TAG, "handle action post recipe");
        //maybe open a new thread
        APICallsHandler.postRecipe(new RecipeTO(recipe), AppHelper.getAccessToken(), results -> {
            if (results != null) {
                Log.e(TAG, "finished post pend, " + results.toString());
                boolean fileUploaded = OnlineStorageWrapper.uploadRecipeFileSync(results.get("url"), recipe.getRecipeFile());
                if (fileUploaded) {
                    //sendIntentToUser(false, "recipe uploaded");
                    if (recipe.getFoodFiles() != null) {
                        uploadFoodFilesSync(results.get("id")/*recipe.getLastModifiedDate()*/, recipe.getFoodFiles());
                    } else {
                        //no images to upload
                        new Handler().postDelayed(() -> sendIntentToUser(false, "recipe uploaded"), 2500);
                    }
                }
                else
                    sendIntentToUser(false, "recipe wasn't uploaded");
            }
            else
                sendIntentToUser(false, "recipe wasn't uploaded");
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
            new Handler().postDelayed(() -> sendIntentToUser(true, "images uploaded"), 2500);
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

    private void sendIntentUploadImagesFinishedToUser(boolean finish) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_UPLOAD_IMAGES_SERVICE);
        intent.putExtra("flag", finish);
        sendBroadcast(intent);
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

    private List<String> compressFiles(List<String> paths) {
        List<String> compressedFiles = null;
        if (paths != null) {
            compressedFiles = new ArrayList<>();
            for (String path: paths) {
                String compressedPath = StorageWrapper.compressFile(this, path);
                if (compressedPath != null) {
                    compressedFiles.add(compressedPath);
                    File compressedFile = new File(compressedPath);
                    Log.e(TAG, "compressed file bytes = " + compressedFile.length());
                    compressedFile.deleteOnExit();
                }
            }
        }
        return compressedFiles;
    }

    /**
     * Handle action {@link PostRecipeToServerService#ACTION_POST_IMAGES} in the provided background thread
     * with the provided parameters.
     */
    private void handleActionPostImagesSync(String id, String lastModifiedDate, List<String> foodFiles) {
        //uploadFoodFilesSync(id, localPaths);
        Log.e(TAG, "handle post images");
        Log.e(TAG, "id = " + id + "\n files: " + foodFiles);
        //Synchronous request with retrofit 2.0
        List<String> urlsForFood = APICallsHandler.requestUrlsForFoodPicturesSync(id, lastModifiedDate, foodFiles.size(), AppHelper.getAccessToken());
        if (urlsForFood != null) {
            //upload the images to s3
            Log.e(TAG, "urls: " + urlsForFood);
            for (int i = 0; i < urlsForFood.size() && i < foodFiles.size(); i++) {
                Log.e(TAG, "uploading file #" + i);
                OnlineStorageWrapper.uploadFoodFileSync(urlsForFood.get(i), foodFiles.get(i));
            }
            sendIntentUploadImagesFinishedToUser(true);
        } else {
            Log.e(TAG, "urls are nulls");
            sendIntentUploadImagesFinishedToUser(false);
        }
        deleteLocalFiles(foodFiles);
    }


    private void handleActionPostRecipeFromQueue() {
        //assuming network connection. Called from Worker that initiates only when connected
        if (AppHelper.getAccessToken() == null) {
            // subscribe for token
            AppHelper.currSessionObservable
                    .subscribe(new DisposableObserver<CognitoUserSession>() {
                        @Override
                        public void onNext(CognitoUserSession cognitoUserSession) {
                            Log.e(TAG, "onNext");
                            startPostPendingRecipesProcess();
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.e(TAG, "onError, " + t.getMessage());
                            dispose();
                        }

                        @Override
                        public void onComplete() {
                            Log.e(TAG, "onComplete");
                            dispose();
                        }
                    });
        } else {
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
                        new Handler().postDelayed(() -> sendIntentToUser(false, "recipe uploaded"), 2500);
                    }
                }
                else
                    sendIntentToUser(false, "recipe wasn't uploaded");
            }
            else
                sendIntentToUser(false, "recipe wasn't uploaded");
        });

        deleteLocalFiles(recipe.getFoodFiles());
        List<String> recipeFile = new ArrayList<>();
        recipeFile.add(recipe.getRecipeFile());
        deleteLocalFiles(recipeFile);
    }
}
