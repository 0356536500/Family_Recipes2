package com.myapps.ron.family_recipes.ui;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.DataViewModel;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.recycler.RecipesAdapter;
import com.myapps.ron.family_recipes.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecipesAdapter.RecipesAdapterListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<Recipe> recipeList;
    private RecipesAdapter mAdapter;
    private SearchView searchView;

    private MenuItem searchMenuItem;

    private DataViewModel viewModel;

    // url to fetch contacts json
    //private static final String URL = "http://192.168.1.5:3000/api/books";
    //private static final String URL = "https://api.androidhive.info/json/contacts.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        // white background notification bar
        whiteNotificationBar(recyclerView);

        initViewModel();

        fetchRecipes();
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        bindUI();

        initRecycler();
    }

    private void bindUI() {
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void initRecycler() {
        recipeList = new ArrayList<>();
        mAdapter = new RecipesAdapter(this, recipeList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(this).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                mAdapter.updateRecipes(recipes);
            }
        });
    }

    private void fetchRecipes() {
        viewModel.loadRecipes(this);
    }

    public MenuItem getSearchMenuItem() {
        return searchMenuItem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchMenuItem = menu.findItem(R.id.action_search);

        // Associate searchable configuration with the SearchView
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_search:
                return true;
            case R.id.action_filter:
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

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onItemSelected(Recipe recipe) {
        //Toast.makeText(getApplicationContext(), "Selected: " + recipe.getName() + ", " + recipe.getDescription(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
        intent.putExtra(Constants.RECIPE, recipe);
        startActivityForResult(intent, 0);
    }
}
