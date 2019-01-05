package com.myapps.ron.family_recipes.ui.fragments;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.listener.FilterListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 07/11/2018.
 */
public class FavoritesRecipesFragment extends RecyclerWithFiltersAbstractFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<CategoryEntity> {

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
        viewModel.getFavorites().observe(this, new Observer<List<RecipeEntity>>() {
            @Override
            public void onChanged(@Nullable List<RecipeEntity> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "in favorite recipes observer");
                if(recipes != null) {
                    Log.e(TAG, recipes.toString());
                    if (mFilter != null)
                        mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, recipes.size()));
                    //Log.e(TAG, "update from fragment");
                    if (mAdapter == null) {
                        mAdapter = new RecipesAdapter(activity, recipes, tags, FavoritesRecipesFragment.this);
                        recyclerView.setAdapter(mAdapter);
                    } else
                        mAdapter.updateRecipes(recipes, !recipes.isEmpty());
                }
            }
        });
        // already have values from AllRecipesFragment
        viewModel.getCategories().observe(this, new Observer<List<CategoryEntity>>() {
            @Override
            public void onChanged(@Nullable List<CategoryEntity> categories) {
                Log.e(TAG, "in categories observer");
                if(categories != null) {
                    Log.e(TAG, categories.toString());
                    tags = new ArrayList<>(categories);
                    tags.add(0, new CategoryEntity.CategoryBuilder()
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
                    Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            }
        });
    }
}
