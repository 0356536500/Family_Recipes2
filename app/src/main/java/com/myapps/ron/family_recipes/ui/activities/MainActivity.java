package com.myapps.ron.family_recipes.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.ui.fragments.AllRecipesFragment;
import com.myapps.ron.family_recipes.ui.fragments.FavoritesRecipesFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.MyBaseActivity;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;

public class MainActivity extends MyBaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private NavigationView navDrawer;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private int drawerCurrentWidth;
    private Paint backgroundPaint;

    //private SearchView searchView;

    public Menu menu;
    public MenuItem searchMenuItem;
    //public MenuItem sortMenuItem;

    private MyFragment currentFragment, allRecipesFragment, favoritesRecipesFragment;
    private DataViewModel viewModel;

    private CognitoUser user;
    private IntentFilter customFilter;
    private String lastOrderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        bindUI();
        setToolbarBackground();
        configureToolbar();
        configureNavigationDrawer();

        init();
        initFragments();

        viewModel =  ViewModelProviders.of(this).get(DataViewModel.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startWithDefaultFragment();
            }
        }, 100);
        // white background notification bar
        //whiteNotificationBar(recyclerView);

        //initViewModel();
    }

    //region Init Methods
    private void configureToolbar() {
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.toolbar_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_black);
        }
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void configureNavigationDrawer() {
        // Set navigation drawer for this screen
        setNavDrawer();
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                drawerCurrentWidth = (int) (drawerView.getWidth() * slideOffset);
                toolbar.requestLayout();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                //if the user currently sliding the drawer
                if(newState == DrawerLayout.STATE_DRAGGING)
                    changeMenuItemsVisibility(false);

                //if the drawer is not moving
                if(newState == DrawerLayout.STATE_IDLE) {
                    if(mDrawer.isDrawerOpen(navDrawer))
                        changeMenuItemsVisibility(false);
                    else
                        changeMenuItemsVisibility(true);
                }
            }
        };
        mDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        View navigationHeader = navDrawer.getHeaderView(0);
        TextView navHeaderSubTitle = navigationHeader.findViewById(R.id.textViewNavUserSub);
        navHeaderSubTitle.setText(AppHelper.getCurrUser());
    }

    private void changeMenuItemsVisibility(boolean show) {
        getMenu().findItem(R.id.action_search).setVisible(show);
        getMenu().findItem(R.id.action_refresh).setVisible(show);
        getMenu().findItem(R.id.action_sort).setVisible(show);
    }

    private void init() {
        user = AppHelper.getPool().getUser(SharedPreferencesHandler.getString(getApplicationContext(), com.myapps.ron.family_recipes.network.Constants.USERNAME));

        /*regularFilter = new IntentFilter();
        regularFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);*/

        customFilter = new IntentFilter();
        customFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        customFilter.addAction(Constants.ACTION_UPDATE_FROM_SERVICE);
    }

    private void setToolbarBackground() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        toolbar.setLayerType(View.LAYER_TYPE_SOFTWARE, backgroundPaint);

        Drawable toolbarBackground = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                // get drawable dimensions
                Rect bounds = getBounds();

                int width = bounds.right - bounds.left;
                int height = bounds.bottom - bounds.top;

                // draw background gradient
                //int barWidth = width / drawerCurrentWidth;
                int barWidth = drawerCurrentWidth;

                //int barWidthRemainder = width % drawerCurrentWidth;

                backgroundPaint.setColor(getResources().getColor(R.color.logo_background_darker));
                canvas.drawRect(new RectF(0, 0, barWidth, height), backgroundPaint);

                backgroundPaint.setColor(Color.WHITE);
                canvas.drawRect(new RectF(barWidth, 0, width, height), backgroundPaint);

                /*backgroundPaint.setColor(Color.BLACK);
                canvas.drawRoundRect(new RectF(0, height, width, height - 10), 3, 3, backgroundPaint);*/

                // draw remainder, if exists
                /*if (barWidthRemainder > 0) {
                    canvas.drawRect(themeColors.length * barWidth, 0, themeColors.length * barWidth + barWidthRemainder, height, backgroundPaint);
                }*/

            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.OPAQUE;
            }
        };

        toolbar.setBackground(toolbarBackground);
        /*if(getSupportActionBar() != null){

        }*/
    }

    private void bindUI() {
        toolbar = findViewById(R.id.main_toolbar);
        mDrawer = findViewById(R.id.main_drawer_layout);
        navDrawer = findViewById(R.id.nav_view);
    }

    /*private void initViewModel() {
        viewModel =  ViewModelProviders.of(this).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                mAdapter.updateRecipes(recipes);
            }
        });
    }*/

    //endregion

    public void fetchRecipes(String orderBy) {
        lastOrderBy = orderBy;
        viewModel.loadRecipes(this, orderBy);
    }

    public void fetchRecipes() {
        viewModel.loadRecipes(this, lastOrderBy);
    }

    public void fetchCategories() {
        viewModel.loadCategories(this);
    }

    public Menu getMenu() {
        return menu;
    }

    public MenuItem getSearchMenuItem() {
        return searchMenuItem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        searchMenuItem = menu.findItem(R.id.action_search);

        //filterMenuItem = menu.findItem(R.id.action_filter);
        //sortMenuItem = menu.findItem(R.id.action_sort);

        // Associate searchable configuration with the SearchView
        //setSearchView(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_sort:
                currentFragment.onOptionsItemSelected(item);
                return true;
            case R.id.action_refresh:
                currentFragment.onOptionsItemSelected(item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        navDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                // set item as selected to persist highlight
                item.setChecked(true);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(item.getTitle());
                // close drawer when item is tapped
                mDrawer.closeDrawers();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performAction(item);
                    }
                }, 500);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
/*        // Close the navigation drawer
        mDrawer.closeDrawers();*/
        MyFragment fragment = null;
        // Find which item was selected
        switch(item.getItemId()) {
            case R.id.nav_main_all_recipes:
                // Add a new attribute
                fragment = allRecipesFragment;
                break;
            case R.id.nav_main_favorites:
                // Add a new attribute
                fragment = favoritesRecipesFragment;
                break;
            case R.id.nav_main_settings:
                // Show user settings
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                //showSettings();
                break;
            case R.id.nav_main_sign_out:
                // Sign out from this account
                signOut();
                break;
            case R.id.nav_main_about:
                // For the inquisitive
                /*Intent aboutAppActivity = new Intent(this, AboutApp.class);
                startActivity(aboutAppActivity);*/
                break;
            case R.id.nav_main_post_recipe:
                //unregisterReceiver(mReceiver);
                //registerReceiver(mReceiver, customFilter);
                Intent intent = new Intent(MainActivity.this, PostRecipeActivity.class);
                startActivityForResult(intent, Constants.POST_RECIPE_ACTIVITY_CODE);
        }

        if(fragment != null) {
            getSupportFragmentManager().findFragmentByTag("all");
            currentFragment = fragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    //.addToBackStack(null)
                    .commit();
            /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_frame, fragment);
            transaction.commit();*/
        }
    }

    private void initFragments() {
        allRecipesFragment = new AllRecipesFragment();
        favoritesRecipesFragment = new FavoritesRecipesFragment();
    }

    private void startWithDefaultFragment() {
        currentFragment = allRecipesFragment;
        /*if (getIntent().getBooleanExtra("load_likes", false)) {
            currentFragment.setArguments(getIntent().getExtras());
        }*/
        navDrawer.getMenu().getItem(0).setChecked(true);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_frame, currentFragment)
                //.addToBackStack(null)
                .commit();
    }

    // Sign out user
    private void signOut() {
        user.signOut();
        SharedPreferencesHandler.removeString(this, com.myapps.ron.family_recipes.network.Constants.USERNAME);
        SharedPreferencesHandler.removeString(this, com.myapps.ron.family_recipes.network.Constants.PASSWORD);
        exit();
    }

    private void exit () {
        String username = AppHelper.getCurrSession().getUsername();
        Intent intent = new Intent();
        if(username == null)
            username = "";
        intent.putExtra("name", username);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        /*if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }*/
        if(mDrawer.isDrawerOpen(navDrawer)) {
            mDrawer.closeDrawers();
            return;
        }
        if(!currentFragment.onBackPressed()) {
            /*FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() > 1)
                manager.popBackStack();
            else*/
                super.onBackPressed();
        }
    }

/*    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)  {
            case Constants.RECIPE_ACTIVITY_CODE:
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "results from RecipeActivity...");
                    //mAdapter.updateRecipes(new RecipesDBHelper(this).getAllRecipes());
                    /*Recipe updatedRecipe = data.getParcelableExtra(Constants.RECIPE);
                    mAdapter.updateOneRecipe(updatedRecipe);*/
                }
                break;
            case Constants.POST_RECIPE_ACTIVITY_CODE:
                if (resultCode != RESULT_OK) {
                    //unregisterReceiver(mReceiver);
                    //registerReceiver(mReceiver, regularFilter);
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mReceiver, customFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                // When network state changes
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    Log.d(TAG, "Network connectivity change");
                    if (intent.getExtras() != null) {
                        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                            MiddleWareForNetwork.setConnection(activeNetwork != null && activeNetwork.isConnected());
                            //NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
                            if (activeNetwork != null && activeNetwork.isConnected()) {
                                Log.i(TAG, "Network " + activeNetwork.getTypeName() + " connected");
                            } else {// if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                                Log.d(TAG, "There's no network connectivity");
                            }
                        }
                        else
                            MiddleWareForNetwork.setConnection(false);
                    }
                    break;
                case Constants.ACTION_UPDATE_FROM_SERVICE:
                    Log.d(TAG, "Got an update from service");
                    //unregisterReceiver(mReceiver);
                    //registerReceiver(mReceiver, regularFilter);
                    if (intent.getExtras() != null) {
                        boolean update = intent.getBooleanExtra("refresh", false);
                        Toast.makeText(MainActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
                        if (update) {
                            fetchRecipes();
                            //Toast.makeText(MainActivity.this, "recipe uploaded successfully!", Toast.LENGTH_SHORT).show();
                        } //else
                            //Toast.makeText(MainActivity.this, "failed to post the recipe", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };
}
