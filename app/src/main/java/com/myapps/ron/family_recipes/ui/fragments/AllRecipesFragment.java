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
import com.myapps.ron.family_recipes.model.Recipe;
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
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                String log = "null";
                if (recipes != null)
                    log = recipes.toString();
                Log.e(TAG, "getAllRecipes from db.\n" + log);
                if (mFilter != null && recipes != null)
                    mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, recipes.size()));
                firstLoadingProgressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                //Log.e(TAG, "update from fragment");
                if (mAdapter == null) {
                    mAdapter = new RecipesAdapter(activity, recipes, AllRecipesFragment.this);
                    recyclerView.setAdapter(mAdapter);
                } else
                    mAdapter.updateRecipes(recipes, recipes != null && !recipes.isEmpty());
            }
        });
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if (categories != null) {
                    tags = new ArrayList<>(categories);
                    tags.add(0, new Category.CategoryBuilder()
                            .name(getString(R.string.str_all_selected))
                            .color(ContextCompat.getColor(activity, R.color.search_filter_text_light))
                            .build());
                    loadFiltersColor();
                    initCategories();
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
