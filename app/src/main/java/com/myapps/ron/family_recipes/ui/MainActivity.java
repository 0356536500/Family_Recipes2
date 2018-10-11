package com.myapps.ron.family_recipes.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.DataViewModel;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.ui.fragments.AllRecipesFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private NavigationView navDrawer;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;

/*    private RecyclerView recyclerView;
    private List<Recipe> recipeList;
    private RecipesAdapter mAdapter;*/
    //private SearchView searchView;

    public Menu menu;
    public MenuItem searchMenuItem;
    public MenuItem filterMenuItem;
    public MenuItem sortMenuItem;

    private Fragment currentFragment;
    private DataViewModel viewModel;

    private CognitoUser user;
    private IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        bindUI();
        configureToolbar();
        configureNavigationDrawer();

        init();

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

        //fetchRecipes();
    }

    private void configureToolbar() {
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_black);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void configureNavigationDrawer() {
        // Set navigation drawer for this screen
        setNavDrawer();
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        View navigationHeader = navDrawer.getHeaderView(0);
        TextView navHeaderSubTitle = navigationHeader.findViewById(R.id.textViewNavUserSub);
        navHeaderSubTitle.setText(AppHelper.getCurrUser());
    }

    private void init() {
        user = AppHelper.getPool().getUser(SharedPreferencesHandler.getString(getApplicationContext(), com.myapps.ron.family_recipes.network.Constants.USERNAME));

        //initRecycler();

        filter = new IntentFilter();
        filter.addAction (ConnectivityManager.CONNECTIVITY_ACTION);
    }

    private void bindUI() {
        toolbar = findViewById(R.id.main_toolbar);
        mDrawer = findViewById(R.id.main_drawer_layout);
        navDrawer = findViewById(R.id.nav_view);
        //recyclerView = findViewById(R.id.recycler_view);
    }

    /*private void initRecycler() {
        recipeList = new ArrayList<>();
        mAdapter = new RecipesAdapter(this, recipeList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
    }*/

    /*private void initViewModel() {
        viewModel =  ViewModelProviders.of(this).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                mAdapter.updateRecipes(recipes);
            }
        });
    }*/

    public void fetchRecipes(String orderBy) {
        viewModel.loadRecipes(this, orderBy);
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

        setToggle(menu);
        //filterMenuItem = menu.findItem(R.id.action_filter);
        //sortMenuItem = menu.findItem(R.id.action_sort);

        // Associate searchable configuration with the SearchView
        //setSearchView(menu);
        return true;
    }

    private void setToggle(Menu menu) {
        MenuItem toggleItem = menu.findItem(R.id.action_sort);
        toggleItem.setActionView(R.layout.toggle_toolbar_layout);
        toggleItem.setChecked(true);
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
            case R.id.action_filter:
                currentFragment.onOptionsItemSelected(item);
                return true;
        }
        /*
        //noinspection SimplifiableIfStatement
        if (itemId == R.id.action_search) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        navDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // set item as selected to persist highlight
                item.setChecked(true);
                // close drawer when item is tapped
                mDrawer.closeDrawers();
                performAction(item);
                return true;
            }
        });
    }

   /* private void setSearchView(Menu menu) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                MenuItem searchMenuItem = getSearchMenuItem();
                if (searchMenuItem != null) {
                    searchMenuItem.collapseActionView();
                }
                Toast.makeText(getApplicationContext(), "Submitted" , Toast.LENGTH_SHORT).show();
                mAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }*/

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
/*        // Close the navigation drawer
        mDrawer.closeDrawers();*/
        Fragment fragment = null;
        // Find which item was selected
        switch(item.getItemId()) {
            case R.id.nav_main_all_recipes:
                // Add a new attribute
                fragment = new AllRecipesFragment();
                break;
            case R.id.nav_main_favorites:
                // Add a new attribute
                break;
            case R.id.nav_main_settings:
                // Show user settings
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
        }

        if(fragment != null) {
            currentFragment = fragment;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_frame, fragment);
            transaction.commit();
        }
    }

    private void startWithDefaultFragment() {
        currentFragment = new AllRecipesFragment();
        navDrawer.getMenu().getItem(0).setChecked(true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_frame, currentFragment);
        transaction.commit();
    }

    // Sign out user
    private void signOut() {
        user.signOut();
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
        super.onBackPressed();
    }

/*    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }*/

/*    public void onItemSelected(Recipe recipe) {
        //Toast.makeText(getApplicationContext(), "Selected: " + recipe.getName() + ", " + recipe.getDescription(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
        intent.putExtra(Constants.RECIPE, recipe);
        startActivityForResult(intent, Constants.RECIPE_ACTIVITY_CODE);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.RECIPE_ACTIVITY_CODE) {
            if(resultCode == RESULT_OK) {
                Log.d(TAG, "results from RecipeActivity...");
                //mAdapter.updateRecipes(new RecipesDBHelper(this).getAllRecipes());
                /*Recipe updatedRecipe = data.getParcelableExtra(Constants.RECIPE);
                mAdapter.updateOneRecipe(updatedRecipe);*/
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver (mReceiver, filter);
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
            assert action != null;
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                // When network state changes

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
            }
        }
    };
}
