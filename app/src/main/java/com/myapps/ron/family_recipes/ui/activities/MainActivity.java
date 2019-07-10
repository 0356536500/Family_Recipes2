package com.myapps.ron.family_recipes.ui.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.navigation.NavigationView;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.logic.storage.StorageWrapper;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.ui.baseclasses.MyFragment;
import com.myapps.ron.family_recipes.ui.fragments.AllRecipesFragment;
import com.myapps.ron.family_recipes.ui.fragments.FavoritesRecipesFragment;
import com.myapps.ron.family_recipes.utils.BackStack;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.localehelper.LocaleHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends MyBaseActivity implements BackStack.BackStackHelper {
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

    //private List<MyFragment> myFragments;
    private BackStack backStack;
    private MyFragment currentFragment;//, allRecipesFragment, favoritesRecipesFragment;

    private IntentFilter customFilter;
    private String lastOrderBy;

    private int toolbarColorPrimary, toolbarColorSecond;

    private List<Integer> listOfTags = Arrays.asList(R.string.nav_main_all_recipes, R.string.nav_main_favorites);
    /*private List<Integer> getListOfTags() {
        return  Arrays.asList(R.string.nav_main_all_recipes, R.string.nav_main_favorites);
    }*/

    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentResumed(fm, f);

            if (f instanceof MyFragment) {
                currentFragment = (MyFragment) f;

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(currentFragment.getMyTag());
                }

                int index = listOfTags.indexOf(currentFragment.getMyTag());
                if (index >= 0)
                    navDrawer.getMenu().getItem(index).setChecked(true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindUI();
        loadColorsFromTheme();
        setToolbarBackground();
        configureToolbar();
        configureNavigationDrawer();

        init();
        //initFragments();
        isRTL = isRTL();

        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(DataViewModel.class);

        // white background notification bar
        //whiteNotificationBar(recyclerView);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // create fragments only when first created, not after change language
        if (savedInstanceState == null) {
            // first creation
            initFragments();
            handleDataFromIntentAndStart();
            checkIfUpdateAvailable();
        } else {
            Log.e(TAG, "restore fragments!!!");
            getSupportFragmentManager().getFragments()
                    .stream()
                    .filter(fragment -> fragment instanceof MyFragment)
                    .map(fragment -> (MyFragment) fragment)
                    .collect(Collectors.toList())
                    .forEach(myFragment -> Log.e(TAG, getString(myFragment.getMyTag())));
            // restore after shutdown
            if (backStack == null)
                backStack = BackStack.restoreBackStackFromList(savedInstanceState, this, getSupportFragmentManager().getFragments()); // FragmentManager contains the last fragment
            currentFragment = backStack.peekTopFragment();
            /*currentFragment = (MyFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_frame);*/
        }
    }

    // region BackStack

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        backStack.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public MyFragment generateFragmentFromTag(@StringRes int tag) {
        MyFragment fragment = null;
        switch (tag) {
            case R.string.nav_main_all_recipes:
                fragment = new AllRecipesFragment();
                fragment.setMyTag(tag);
                break;
            case R.string.nav_main_favorites:
                fragment = new FavoritesRecipesFragment();
                fragment.setMyTag(tag);
                break;
        }
        return fragment;
    }

    @Nullable
    private MyFragment getOrCreateFragment(@StringRes int tag) {
        MyFragment fragment = backStack.findFragmentByTag(tag);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.FIRST_LOAD_FRAGMENT, fragment == null);
        /*if (fragment != null)
            Log.e(TAG, "getOrCreateFragment, fragment tag = " + getString(fragment.getMyTag()));*/
        if (fragment == null) {
            //Log.e(TAG, "getOrCreateFragment, fragment == null");
            switch (tag) {
                case R.string.nav_main_all_recipes:
                    fragment = new AllRecipesFragment();
                    fragment.setMyTag(tag);
                    break;
                case R.string.nav_main_favorites:
                    fragment = new FavoritesRecipesFragment();
                    fragment.setMyTag(tag);
                    break;
            }
        }
        if (fragment != null)
            fragment.setArguments(bundle);
        return fragment;
    }

    // endregion

    private void handleDataFromIntentAndStart() {
        //handle data from intent
        Runnable startWithDefaultFragmentRunnable = () -> startWithDefaultFragment(getOrCreateFragment(R.string.nav_main_all_recipes));

        if (getIntent() != null && getIntent().getSerializableExtra(Constants.MAIN_ACTIVITY_FIRST_FRAGMENT) != null) {
            String firstFragment = getIntent().getStringExtra(Constants.MAIN_ACTIVITY_FIRST_FRAGMENT);
            Log.e(TAG, "firstFragment = " + firstFragment);

            if (Constants.MAIN_ACTIVITY_FRAGMENT_ALL.equals(firstFragment))
                new Handler().postDelayed(startWithDefaultFragmentRunnable, 100);
            else if (Constants.MAIN_ACTIVITY_FRAGMENT_FAVORITES.equals(firstFragment))
                new Handler().postDelayed(() -> startWithDefaultFragment(getOrCreateFragment(R.string.nav_main_favorites)), 100);
        } else
            new Handler().postDelayed(startWithDefaultFragmentRunnable, 100);

        //check if got here from login activity
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("from_login", false))
                new Handler().postDelayed(() -> viewModel.fetchFromServerJustLoggedIn(this), 10000);
        }
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
        customFilter = new IntentFilter();
        customFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //customFilter.addAction(Constants.ACTION_UPDATE_FROM_SERVICE);
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
        return LocaleHelper.INSTANCE.isRTL(Locale.getDefault());
        /*return getResources().getConfiguration()
                .getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;*/
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                fragment = getOrCreateFragment(R.string.nav_main_all_recipes);//allRecipesFragment;
                break;
            case R.id.nav_main_favorites:
                // Add a new attribute
                fragment = getOrCreateFragment(R.string.nav_main_favorites);//favoritesRecipesFragment;
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
                showAboutPage();
                break;
            case R.id.nav_main_post_recipe:
                Intent intent = new Intent(MainActivity.this, PostRecipeActivity.class);
                startActivityForResult(intent, Constants.POST_RECIPE_ACTIVITY_CODE);
        }

        if(fragment != null && fragment != currentFragment) {

            // add the fragment to backStack at index 0
            backStack.addToBackStack(fragment);
            //Log.e(TAG, backStack.toString());

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_enter_from_left,
                            R.anim.slide_right_to_exit,
                            R.anim.slide_enter_from_right,
                            R.anim.slide_left_to_exit)
                    .replace(R.id.main_frame, fragment, String.valueOf(fragment.getMyTag()))
                    .commit();
        }
    }

    // endregion

    private void initFragments() {
        backStack = new BackStack(this);
        /*myFragments = new ArrayList<>();
        myFragments.add(new AllRecipesFragment());
        myFragments.get(0).setMyTag(R.string.nav_main_all_recipes);
        myFragments.add(new FavoritesRecipesFragment());
        myFragments.get(1).setMyTag(R.string.nav_main_favorites);*/
        /*allRecipesFragment = new AllRecipesFragment();
        favoritesRecipesFragment = new FavoritesRecipesFragment();*/
    }

    private void startWithDefaultFragment(@Nullable MyFragment startingFragment) {
        if (startingFragment != null) {
            currentFragment = startingFragment;

            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.main_frame, currentFragment, String.valueOf(currentFragment.getMyTag()))
                    //.addToBackStack(null)
                    .commit();
            backStack.addToBackStack(currentFragment); // index 0 is the currently displayed fragment
        }
    }

    private boolean popFragmentFromBackStack() {
        MyFragment nextFragment = backStack.popFromBackStack(); // new displaying fragment
        //Log.e(TAG, backStack.toString());
        //Log.e(TAG, "popFragmentFromBackStack, fragment == null ? " + (nextFragment == null));

        if (nextFragment != null) {
            //Log.e(TAG, "popFragmentFromBackStack, fragment tag = " + getString(nextFragment.getMyTag()));
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_enter_from_right,
                            R.anim.slide_left_to_exit,
                            R.anim.slide_enter_from_left,
                            R.anim.slide_right_to_exit)
                    .replace(R.id.main_frame, nextFragment, String.valueOf(nextFragment.getMyTag()))
                    .commit();
            return true;
        }
        return false;
    }

    // Sign out user
    private void signOut() {
        AppHelper.signOutUser();
        com.myapps.ron.family_recipes.layout.firebase.AppHelper.signOutUser();
        SharedPreferencesHandler.removeString(this, com.myapps.ron.family_recipes.layout.Constants.USERNAME);
        SharedPreferencesHandler.removeString(this, com.myapps.ron.family_recipes.layout.Constants.PASSWORD);
        // start splash activity
        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSettings() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void showAboutPage() {
        Intent settingsIntent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(settingsIntent);
    }

    /*private void exit () {
        String username = AppHelper.getCurrSession().getUsername();
        Intent intent = new Intent();
        if(username == null)
            username = "";
        intent.putExtra("name", username);
        setResult(RESULT_OK, intent);
        finish();
    }*/

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
        if (requestCode == Constants.POST_RECIPE_ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.main_activity_posted_new_recipe, Toast.LENGTH_SHORT).show();
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(onComplete);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    // region App Updates

    private static final int REQUEST_WRITE_PERMISSION = 786;
    private File appUpdateFile;
    private Uri uri;

    private void checkIfUpdateAvailable() {
        viewModel.getDataToDownloadUpdate(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableMaybeObserver<Map<String, String>>() {
                    @Override
                    public void onSuccess(Map<String, String> map) {
                        appUpdateFile = StorageWrapper.getFileToDownloadUpdateInto(getApplicationContext(),
                                map.get(com.myapps.ron.family_recipes.layout.Constants.RESPONSE_KEY_APP_NAME));
                        uri = Uri.parse(map.get(com.myapps.ron.family_recipes.layout.Constants.RESPONSE_KEY_APP_URL));
                        new AlertDialog.Builder(getApplicationContext())
                                .setCancelable(true)
                                .setTitle(R.string.main_activity_update_available_title)
                                .setMessage(R.string.main_activity_update_available_message)
                                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                                        updateApp())
                                .create()
                                .show();

                        dispose();
                    }

                    @Override
                    public void onError(Throwable t) {
                        //Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        // up to-date
                        dispose();
                    }
                });
    }

    private void updateApp() {
        if (canReadWriteExternalAndInstallPackages()) {
            viewModel.downloadNewAppVersion(this, onComplete, uri, appUpdateFile);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle(R.string.main_activity_permission_to_install_updates_title)
                        .setMessage(R.string.main_activity_permission_to_install_updates_message)
                        .setPositiveButton(android.R.string.yes, (dialog, which) ->
                                requestPermission())
                        .create()
                        .show();
            } else
                requestPermission();
        }
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Finished", Toast.LENGTH_LONG).show();
            viewModel.installApp(getApplicationContext(), appUpdateFile);
            unregisterReceiver(this);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            viewModel.downloadNewAppVersion(this, onComplete, uri, appUpdateFile);
        else
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternalAndInstallPackages() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;
    }

    // endregion

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
            // When network state changes
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                // refresh connection status in helper
                MiddleWareForNetwork.checkInternetConnection(getApplicationContext());
            }
        }
    };
}
