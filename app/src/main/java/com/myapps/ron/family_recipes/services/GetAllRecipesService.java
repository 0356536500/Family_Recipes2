package com.myapps.ron.family_recipes.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.ApiResponse;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetAllRecipesService extends IntentService {
    private static final String TAG = GetAllRecipesService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_ALL = "com.myapps.ron.family_recipes.services.action.GET_ALL";
    private static final String ACTION_GET_PAGINATION = "com.myapps.ron.family_recipes.services.action.GET_PAGINATION";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.services.extra.PARAM2";

    public GetAllRecipesService() {
        super("GetAllRecipesService");
    }

    @Override
    public void onDestroy() {
        if (asyncRecipeUpdateList != null)
            for (MyAsyncRecipeUpdate asyncRecipeUpdate: asyncRecipeUpdateList)
                asyncRecipeUpdate.cancel(false);
        super.onDestroy();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetAllRecipes(Context context) {
        Log.e(TAG, "startActionGetAllRecipes");
        Intent intent = new Intent(context, GetAllRecipesService.class);
        intent.setAction(ACTION_GET_ALL);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    /*public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GetAllRecipesService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_ALL.equals(action)) {
                Log.e(TAG, "onHandleIntent");
                setThreadPolicy();
                handleActionGetAllRecipes();
            } /*else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }*/
        }
    }

    private void setThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private List<MyAsyncRecipeUpdate> asyncRecipeUpdateList;
    private AtomicInteger newTotalRecipes = new AtomicInteger(0);
    private AtomicInteger modifiedTotalRecipes =  new AtomicInteger(0);

    /**
     * Handle action GetAllRecipes in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetAllRecipes() {
        Log.e(TAG, "handleActionGetAllRecipes");
        asyncRecipeUpdateList = Collections.synchronizedList(new ArrayList<>());
        String lastKey = null;
        if (!MiddleWareForNetwork.checkInternetConnection(getApplicationContext()))
            return;
        final String time = DateUtil.getUTCTime();
        do {
            ApiResponse<List<RecipeTO>> response = APICallsHandler.getAllRecipesSync(
                    DateUtil.getLastUpdateTime(getApplicationContext()), lastKey, 50, AppHelper.getAccessToken());

            if (response != null) {
                Log.e(TAG, "response: lastKey = " + response.getLastKey());
                Log.e(TAG, "response data length: " + response.getData().size());
                lastKey = response.getLastKey();

                MyAsyncRecipeUpdate asyncRecipeUpdate = new MyAsyncRecipeUpdate(getApplicationContext(), response.getData());
                asyncRecipeUpdate.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                asyncRecipeUpdateList.add(asyncRecipeUpdate);

            }
        }
        while (lastKey != null && !lastKey.isEmpty() && MiddleWareForNetwork.checkInternetConnection(getApplicationContext()));

        DateUtil.updateServerTime(getApplicationContext(), time);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    /*private void handleActionBaz(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

    @SuppressLint("StaticFieldLeak")
    class MyAsyncRecipeUpdate extends AsyncTask<Void, Void, Boolean> {
        private int newRecipes, modifiedRecipes;
        private List<RecipeTO> recipes;
        private RecipesDBHelper dbHelper;

        MyAsyncRecipeUpdate(Context context, List<RecipeTO> recipes) {
            this.dbHelper = new RecipesDBHelper(context);
            this.recipes = recipes;
            this.newRecipes = 0;
            this.modifiedRecipes = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (RecipeTO item : recipes) {
                if (isCancelled())
                    return false;
                //Log.e(getClass().getSimpleName(), "item: " + item.getId());
                if(dbHelper.recipeExists(item.getId())) {
                    //Log.e(getClass().getSimpleName(), "\t exists");
                    dbHelper.updateRecipeServerChanges(item.toEntity());
                    modifiedRecipes++;
                } else {
                    //Log.e(getClass().getSimpleName(), "\t not exists");
                    dbHelper.insertRecipe(item.toEntity());
                    newRecipes++;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean){
                newTotalRecipes.addAndGet(newRecipes);
                modifiedTotalRecipes.addAndGet(modifiedRecipes);
            }
            /*if(aBoolean) {
                setRecipes(dbHelper.getAllRecipes(orderBy));
                setInfoFromLastFetch(context.getString(R.string.message_from_fetch_recipes, newRecipes, modifiedRecipes));
            }*/
        }
    }


}
