package com.myapps.ron.family_recipes.ui.fragments;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Handler;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.listener.FilterListener;

import java.util.ArrayList;
import java.util.List;

public class AllRecipesFragment extends RecyclerWithFiltersAbstractFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<Category> {

    @Override
    protected void initAfterViewCreated() {
        setRefreshLayout();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firstLoadingProgressBar.setVisibility(View.VISIBLE);
                activity.fetchCategories();
                activity.fetchRecipes(orderBy);
            }
        }, 500);
    }

    @Override
    protected void optionRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefreshListener.onRefresh();
    }

    @Override
    protected void initViewModel() {
        viewModel = ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<RecipeEntity>>() {
            @Override
            public void onChanged(@Nullable List<RecipeEntity> recipesList) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                /*String log = "null";
                if (recipes != null)
                    log = recipes.toString();
                Log.e(TAG, "getAllRecipes from db.\n" + log);*/
                Log.e(TAG, String.valueOf("in recipes observer. recipes != null"));

                firstLoadingProgressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                recipes = recipesList;

                viewModel.setRecipesReady(true);
                //Log.e(TAG, "update from fragment");
                if (mAdapter != null)
                    mAdapter.updateRecipes(recipes, recipes != null && !recipes.isEmpty());
            }
        });
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if (categories != null) {
                    Log.e(TAG, "in category observer. categories != null");
                    tags = new ArrayList<>(categories);
                    tags.add(0, new Category.CategoryBuilder()
                            .name(getString(R.string.str_all_selected))
                            .color(ContextCompat.getColor(activity, R.color.search_filter_text_light))
                            .build());
                    loadFiltersColor();

                    viewModel.setCategoriesReady(true);
                }
            }
        });
        viewModel.getCanInitBothRecyclerAndFilters().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                if (value) {
                    Log.e(TAG, "in both observer");
                    initCategories();
                    if (mFilter != null &&  recipes != null)
                        mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, recipes.size()));
                    if (mAdapter == null) {
                        mAdapter = new RecipesAdapter(activity, recipes, tags, AllRecipesFragment.this);
                        recyclerView.setAdapter(mAdapter);
                    }
                    viewModel.getCanInitBothRecyclerAndFilters().removeObserver(this);
                }
            }
        });
        viewModel.getInfoFromLastFetch().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
