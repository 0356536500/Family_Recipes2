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
        queryModel.setOrderBy(RecipeEntity.KEY_CREATED);
        queryModel.setFavorites(true);
        new Handler().postDelayed(() ->
                viewModel.applyQuery(queryModel), 500);
                //viewModel.loadLocalFavoritesOrdered(activity, com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT), 500);
    }

    @Override
    protected void optionRefresh() {
    }

    @Override
    protected void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getPagedRecipes().observe(this, recipes -> {
            //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "in favorite recipes observer");
            if(recipes != null) {
                Log.e(TAG, recipes.toString());
                /*if (mFilter != null)
                    mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, recipes.size()));*/
                //Log.e(TAG, "update from fragment");
                mAdapter.submitList(recipes);
            }
        });
        // already have values from AllRecipesFragment
        viewModel.getCategories().observe(this, categories -> {
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
        });
        viewModel.getInfoFromLastFetch().observe(this, s -> {
            if (s != null)
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
        });
    }
}
