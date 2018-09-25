package com.myapps.ron.family_recipes.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.dal.db.RecipesDBMaintainer;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HandleServerDataService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_RECIPES = "com.myapps.ron.family_recipes.services.action.GET_RECIPES";
    private static final String ACTION_GET_IMAGE = "com.myapps.ron.family_recipes.services.action.GET_IMAGE";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.myapps.ron.family_recipes.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.myapps.ron.family_recipes.services.extra.PARAM2";

    public HandleServerDataService() {
        super("HandleServerDataService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionGetRecipes(Context context, ArrayList<Recipe> recipes, String time) {
        Intent intent = new Intent(context, HandleServerDataService.class);
        intent.setAction(ACTION_GET_RECIPES);
        intent.putParcelableArrayListExtra(EXTRA_PARAM1, recipes);
        intent.putExtra(EXTRA_PARAM2, time);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, HandleServerDataService.class);
        intent.setAction(ACTION_GET_IMAGE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_RECIPES.equals(action)) {
                final List<Recipe> recipeList = intent.getParcelableArrayListExtra(EXTRA_PARAM1);
                final String time = intent.getStringExtra(EXTRA_PARAM2);
                handleActionGetRecipes(recipeList, time);
            } else if (ACTION_GET_IMAGE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetRecipes(List<Recipe> recipes, String time) {
        RecipesDBHelper dbHelper = new RecipesDBHelper(getApplicationContext());
        for(Recipe item : recipes) {
            if(dbHelper.recipeExists(item.getId()))
                dbHelper.updateRecipe(item);
            else
                dbHelper.insertRecipe(item);
        }
        DateUtil.updateServerTime(getApplicationContext(), time);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
