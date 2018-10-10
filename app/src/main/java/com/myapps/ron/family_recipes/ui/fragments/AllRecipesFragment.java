package com.myapps.ron.family_recipes.ui.fragments;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.DataViewModel;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.recycler.RecipesAdapter;
import com.myapps.ron.family_recipes.ui.MainActivity;
import com.myapps.ron.family_recipes.ui.RecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AllRecipesFragment extends Fragment implements RecipesAdapter.RecipesAdapterListener{

    private static final String TAG = AllRecipesFragment.class.getSimpleName();
    private MainActivity activity;

    private RecyclerView recyclerView;
    private RecipesAdapter mAdapter;

    private DataViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recycler_view);

        initViewModel();
        initRecycler();

        // Associate searchable configuration with the SearchView
        setSearchView(activity.getMenu());

        activity.fetchRecipes();
    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                mAdapter.updateRecipes(recipes);
            }
        });
    }

    private void initRecycler() {
        List<Recipe> recipeList = new ArrayList<>();
        mAdapter = new RecipesAdapter(activity, recipeList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(activity, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
    }

    private void setSearchView(Menu menu) {
        //MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(activity.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                MenuItem searchMenuItem = activity.getSearchMenuItem();
                if (searchMenuItem != null) {
                    searchMenuItem.collapseActionView();
                }
                Toast.makeText(activity, "Submitted" , Toast.LENGTH_SHORT).show();
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
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_search:
                Toast.makeText(activity, "search clicked (" + TAG + ")", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_filter:
                Toast.makeText(activity, "filter clicked (" + TAG + ")", Toast.LENGTH_SHORT).show();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity)getActivity();
    }

    @Override
    public void onItemSelected(Recipe recipe) {
        Intent intent = new Intent(activity, RecipeActivity.class);
        intent.putExtra(Constants.RECIPE, recipe);
        startActivityForResult(intent, Constants.RECIPE_ACTIVITY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.RECIPE_ACTIVITY_CODE) {
            if(resultCode == RESULT_OK) {
                //mAdapter.updateRecipes(new RecipesDBHelper(this).getAllRecipes());
                Recipe updatedRecipe = data.getParcelableExtra(Constants.RECIPE);
                mAdapter.updateOneRecipe(updatedRecipe);
            }
        }
    }
}