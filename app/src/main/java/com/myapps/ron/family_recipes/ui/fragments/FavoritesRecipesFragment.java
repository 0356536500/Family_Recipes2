package com.myapps.ron.family_recipes.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.recycler.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.listener.FilterListener;

import java.util.ArrayList;

/**
 * Created by ronginat on 07/11/2018.
 */
public class FavoritesRecipesFragment extends RecyclerWithFiltersAbstractFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<CategoryEntity> {

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(itemView, savedInstanceState);

        swipeRefreshLayout.setEnabled(false);
        //queryModel.setOrderBy(RecipeEntity.KEY_CREATED);
        queryModel.setFavorites(true);
        new Handler().postDelayed(() ->
                viewModel.applyQuery(queryModel), 500);
    }

    @Override
    protected void optionRefresh() {
    }

    @Override
    protected void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getPagedRecipes().observe(this, recipes -> {
            if(recipes != null) {
                mAdapter.submitList(recipes);
            }
        });
        // already have values from AllRecipesFragment
        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                tags = new ArrayList<>(categories);
                tags.add(0, new CategoryEntity.CategoryBuilder()
                        .name(getString(R.string.str_all_selected))
                        .color(ContextCompat.getColor(activity, R.color.search_filter_text_light))
                        .build());
                loadFiltersColor();
                initCategories();
                mAdapter.setCategoryList(categories);
            }
        });
        /*viewModel.getInfoFromLastFetch().observe(this, s -> {
            if (s != null)
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
        });*/
    }
}
