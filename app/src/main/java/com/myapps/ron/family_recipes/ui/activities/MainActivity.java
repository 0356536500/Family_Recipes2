package com.myapps.ron.family_recipes.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.ui.baseclasses.MyFragment;
import com.myapps.ron.family_recipes.ui.fragments.AllRecipesFragment;
import com.myapps.ron.family_recipes.ui.fragments.FavoritesRecipesFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends MyBaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private NavigationView navDrawer;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private int drawerCurrentWidth;
    private Paint backgroundPaint;
    private boolean isRTL;

    private DataViewModel viewModel;

    public Menu menu;
    public MenuItem searchMenuItem;
    //public MenuItem sortMenuItem;

    private List<MyFragment> myFragments;
    private List<MyFragment> backStack;
    private MyFragment currentFragment;//, allRecipesFragment, favoritesRecipesFragment;

    private IntentFilter customFilter;
    private String lastOrderBy;

    private int toolbarColorPrimary, toolbarColorSecond;

    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentResumed(fm, f);
            if (f instanceof MyFragment) {
                currentFragment = (MyFragment) f;

                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(currentFragment.getMyTag());

                int index = myFragments.indexOf(currentFragment);
                if (index >= 0)
                    navDrawer.getMenu().getItem(index).setChecked(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindUI();
        loadColorsFromTheme();
        setToolbarBackground();
        configureToolbar();
        configureNavigationDrawer();

        init();
        initFragments();
        isRTL = isRTL();

        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(DataViewModel.class);
        //viewModel = ViewModelProviders.of(this).get(DataViewModel.class);

        // create fragments only when first created, not after change language
        if (savedInstanceState == null) {
            handleDataFromIntentAndStart();
            getFirebaseToken();
        } else {
            currentFragment = (MyFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_frame);
        }

        // white background notification bar
        //whiteNotificationBar(recyclerView);
    }

    private void handleDataFromIntentAndStart() {
        //handle data from intent
        if (getIntent() != null && getIntent().getSerializableExtra(Constants.MAIN_ACTIVITY_FIRST_FRAGMENT) != null) {
            String firstFragment = getIntent().getStringExtra(Constants.MAIN_ACTIVITY_FIRST_FRAGMENT);
            Log.e(TAG, "firstFragment = " + firstFragment);

            if (firstFragment.equals(Constants.MAIN_ACTIVITY_FRAGMENT_ALL))
                new Handler().postDelayed(() -> startWithDefaultFragment(myFragments.get(0)), 100);
            else if (firstFragment.equals(Constants.MAIN_ACTIVITY_FRAGMENT_FAVORITES))
                new Handler().postDelayed(() -> startWithDefaultFragment(myFragments.get(1)), 100);
        } else
            new Handler().postDelayed(() -> startWithDefaultFragment(myFragments.get(0)), 100);

        //check if got here from login activity
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("from_login", false))
                new Handler().postDelayed(() -> viewModel.fetchFromServerJustLoggedIn(this), 10000);
        }
    }

    private void getFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    if (task.getResult() != null) {
                        String token = task.getResult().getToken();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.e(TAG, "firebase token: " + token);
                        //Toast.makeText(MainActivity.this, "firebase token: " + token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadColorsFromTheme() {
        TypedValue valuePrimary = new TypedValue();
        TypedValue valueSecond = new TypedValue();

        getTheme().resolveAttribute(R.attr.toolbarBackgroundPrimary, valuePrimary, true);
        getTheme().resolveAttribute(R.attr.toolbarBackgroundSecondary, valueSecond, true);

        toolbarColorPrimary = valuePrimary.data;
        toolbarColorSecond = valueSecond.data;
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

                if (!isRTL) {
                    //backgroundPaint.setColor(getResources().getColor(R.color.logo_background_darker));
                    backgroundPaint.setColor(getColorSecondary());
                    canvas.drawRect(new RectF(0, 0, barWidth, height), backgroundPaint);

                    //backgroundPaint.setColor(Color.WHITE);
                    backgroundPaint.setColor(getColorPrimary());
                    canvas.drawRect(new RectF(barWidth, 0, width, height), backgroundPaint);
                } else {
                    //backgroundPaint.setColor(getResources().getColor(R.color.logo_background_darker));
                    backgroundPaint.setColor(getColorSecondary());
                    canvas.drawRect(new RectF(width - barWidth, 0, width, height), backgroundPaint);

                    //backgroundPaint.setColor(Color.WHITE);
                    backgroundPaint.setColor(getColorPrimary());
                    canvas.drawRect(new RectF(0, 0, width - barWidth, height), backgroundPaint);
                }


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

            private int getColorPrimary() {
                return toolbarColorPrimary;
            }

            private int getColorSecondary() {
                return  toolbarColorSecond;
            }
        };

        toolbar.setBackground(toolbarBackground);
        /*if(getSupportActionBar() != null){

        }*/
    }

    private boolean isRTL() {
        return getResources().getConfiguration()
                .getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void bindUI() {
        toolbar = findViewById(R.id.main_toolbar);
        mDrawer = findViewById(R.id.main_drawer_layout);
        navDrawer = findViewById(R.id.nav_view);
    }

    //endregion

    // region Menu Methods

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
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_sort:
                /*currentFragment.onOptionsItemSelected(item);
                return true;*/
            case R.id.action_refresh:
                currentFragment.onOptionsItemSelected(item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        navDrawer.setNavigationItemSelectedListener(item -> {
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(item.getTitle());
            // close drawer when item is tapped
            mDrawer.closeDrawers();

            new Handler().postDelayed(() -> performAction(item), 500);
            return true;
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
                fragment = myFragments.get(0);//allRecipesFragment;
                break;
            case R.id.nav_main_favorites:
                // Add a new attribute
                fragment = myFragments.get(1);//favoritesRecipesFragment;
                break;
            case R.id.nav_main_settings:
                // Show user settings
                showSettings();
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
                Intent intent = new Intent(MainActivity.this, PostRecipeActivity.class);
                startActivityForResult(intent, Constants.POST_RECIPE_ACTIVITY_CODE);
        }

        if(fragment != null && fragment != currentFragment) {
            // set item as selected to persist highlight
            //item.setChecked(true);
            int index = backStack.indexOf(fragment);
            if (index > 0) {
                // remove fragment from old index
                backStack.remove(index);

            }
            // add the fragment to backStack at index 0
            backStack.add(0, fragment);

            //currentFragment = fragment;  // Redundant - lifecycleCallback onFragmentResumed will do it
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_enter_from_left,
                            R.anim.slide_right_to_exit,
                            R.anim.slide_enter_from_right,
                            R.anim.slide_left_to_exit)
                    .replace(R.id.main_frame, fragment)
                    .commit();
        }
    }

    // endregion

    private void initFragments() {
        backStack = new ArrayList<>();
        myFragments = new ArrayList<>();
        myFragments.add(new AllRecipesFragment());
        myFragments.get(0).setMyTag(navDrawer.getMenu().getItem(0).getTitle().toString());
        myFragments.add(new FavoritesRecipesFragment());
        myFragments.get(1).setMyTag(navDrawer.getMenu().getItem(1).getTitle().toString());
        /*allRecipesFragment = new AllRecipesFragment();
        favoritesRecipesFragment = new FavoritesRecipesFragment();*/
    }

    private void startWithDefaultFragment(@NonNull MyFragment startingFragment) {
        currentFragment = startingFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.main_frame, currentFragment)
                //.addToBackStack(null)
                .commit();
        backStack.add(currentFragment); // index 0 is the currently displayed fragment
    }

    private boolean popFragmentFromBackStack() {
        if (backStack.size() > 1) {
            backStack.remove(0); // pop the current fragment out of the stack
            MyFragment nextFragment = backStack.get(0); // new displaying fragment

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_enter_from_right,
                            R.anim.slide_left_to_exit,
                            R.anim.slide_enter_from_left,
                            R.anim.slide_right_to_exit)
                    .replace(R.id.main_frame, nextFragment)
                    .commit();
            return true;
        }
        return false;
    }

    // Sign out user
    private void signOut() {
        AppHelper.signOutUser();
        SharedPreferencesHandler.removeString(this, com.myapps.ron.family_recipes.network.Constants.USERNAME);
        SharedPreferencesHandler.removeString(this, com.myapps.ron.family_recipes.network.Constants.PASSWORD);
        exit();
    }

    private void showSettings() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
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
            if (!popFragmentFromBackStack())
                super.onBackPressed();
        }
    }

    /*private void whiteNotificationBar(View view) {
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
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
        getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
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
                            //fetchRecipes();
                            //Toast.makeText(MainActivity.this, "recipe uploaded successfully!", Toast.LENGTH_SHORT).show();
                        } //else
                            //Toast.makeText(MainActivity.this, "failed to post the recipe", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };
}
