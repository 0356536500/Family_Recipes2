package com.myapps.ron.family_recipes.background.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.S3.OnlineStorageWrapper;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.logic.repository.RecipeRepository;
import com.myapps.ron.family_recipes.logic.storage.StorageWrapper;
import com.myapps.ron.family_recipes.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PostFoodImagesService extends IntentService {
    private static final String TAG = PostFoodImagesService.class.getSimpleName();
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POST_IMAGES = "com.myapps.ron.family_recipes.background.services.action.POST_IMAGES";

    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.background.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.background.services.extra.PARAM2";
    private static final String EXTRA_PARAM3 = "com.myapps.ron.family_recipes.background.services.extra.PARAM3";


    public PostFoodImagesService() {
        super("PostRecipeToServerService");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPostImages(Context context, String id, String lastModifiedDate, List<String> files) {
        Intent intent = new Intent(context, PostFoodImagesService.class);
        intent.setAction(ACTION_POST_IMAGES);
        intent.putStringArrayListExtra(EXTRA_PARAM1, new ArrayList<>(files));
        intent.putExtra(EXTRA_PARAM2, id);
        intent.putExtra(EXTRA_PARAM3, lastModifiedDate);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                if (ACTION_POST_IMAGES.equals(action)) {
                    final List<String> files = intent.getStringArrayListExtra(EXTRA_PARAM1);
                    String id = intent.getStringExtra(EXTRA_PARAM2);
                    String lastModifiedDate = intent.getStringExtra(EXTRA_PARAM3);
                    handleActionPostImagesSync(id, lastModifiedDate, compressFiles(files));
                    //deleteLocalFiles(files);
                }
            }
        }
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
        //new File(recipe.getContent()).delete();
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
     * Handle action {@link PostFoodImagesService#ACTION_POST_IMAGES} in the provided background thread
     * with the provided parameters.
     */
    private void handleActionPostImagesSync(String id, String lastModifiedDate, List<String> foodFiles) {
        //uploadFoodFilesSync(id, localPaths);
        Log.e(TAG, "handle post images");
        Log.e(TAG, "id = " + id + "\n files: " + foodFiles);
        RecipeRepository repository = Injection.provideRecipeRepository(getApplicationContext());
        //Synchronous request with retrofit 2.0
        Response<List<String>> response = APICallsHandler.requestUrlsForFoodPicturesSync(id, lastModifiedDate, foodFiles.size(), AppHelper.getAccessToken());
        if (response != null) {
            if (response.isSuccessful() && response.body() != null) {
                List<String> urlsForFood = response.body();

                Log.e(TAG, "urls: " + urlsForFood);
                for (int i = 0; i < urlsForFood.size() && i < foodFiles.size(); i++) {
                    Log.e(TAG, "uploading file #" + i);
                    OnlineStorageWrapper.uploadFoodFileSync(urlsForFood.get(i), foodFiles.get(i));
                }
                sendIntentUploadImagesFinishedToUser(true);

            } else {
                try {
                    if (response.errorBody() != null) {
                        String message = response.errorBody().string();
                        repository.dispatchInfoForRecipe.onNext(message);
                    } else
                        repository.dispatchInfoForRecipe.onNext(getApplicationContext().getString(R.string.post_images_error));
                } catch (IOException e) {
                    e.printStackTrace();
                    repository.dispatchInfoForRecipe.onNext(getApplicationContext().getString(R.string.post_images_error));
                } finally {
                    sendIntentUploadImagesFinishedToUser(false);
                }
            }

        } else {
            Log.e(TAG, "urls are nulls");
            sendIntentUploadImagesFinishedToUser(false);
        }

        deleteLocalFiles(foodFiles);
    }
}
