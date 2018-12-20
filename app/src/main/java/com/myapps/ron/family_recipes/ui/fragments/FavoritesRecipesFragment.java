package com.myapps.ron.family_recipes.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.listener.FilterListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 07/11/2018.
 */
public class FavoritesRecipesFragment extends RecyclerWithFiltersAbstractFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<Category> {

    @Override
    protected void initAfterViewCreated() {
        swipeRefreshLayout.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewModel.loadLocalFavoritesOrdered(activity, com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT);
            }
        }, 500);
    }

    @Override
    protected void optionRefresh() {
    }

    @Override
    protected void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getFavorites().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "in favorite recipes observer");
                if(recipes != null) {
                    Log.e(TAG, recipes.toString());
                    if (mFilter != null)
                        mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, recipes.size()));
                    //Log.e(TAG, "update from fragment");
                    if (mAdapter == null) {
                        mAdapter = new RecipesAdapter(activity, recipes, FavoritesRecipesFragment.this);
                        recyclerView.setAdapter(mAdapter);
                    } else
                        mAdapter.updateRecipes(recipes, !recipes.isEmpty());
                }
            }
        });
        // already have values from AllRecipesFragment
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                Log.e(TAG, "in categories observer");
                if(categories != null) {
                    Log.e(TAG, categories.toString());
                    tags = new ArrayList<>(categories);
                    tags.add(0, new Category(getString(R.string.str_all_selected), mColors[0]));
                    loadFiltersColor();
                    setCategories();
                    initCategories();
                }
            }
        });
        viewModel.getInfoFromLastFetch().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            }
        });
    }
}
