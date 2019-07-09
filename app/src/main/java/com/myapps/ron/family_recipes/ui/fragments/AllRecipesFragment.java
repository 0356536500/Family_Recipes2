package com.myapps.ron.family_recipes.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.background.services.GetUserDetailsService;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.recycler.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.listener.FilterListener;

import java.util.ArrayList;

public class AllRecipesFragment extends RecyclerWithFiltersAbstractFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<CategoryEntity> {

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(itemView, savedInstanceState);

        setRefreshLayout();

        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(Constants.FIRST_LOAD_FRAGMENT, false)) {
            new Handler().postDelayed(() -> {
                firstLoadingProgressBar.setVisibility(View.VISIBLE);
                viewModel.fetchFromServer(activity, false);
                viewModel.applyQuery(queryModel);
            }, 500);

            if (activity.getIntent().getBooleanExtra("login", false)) {
                new Handler().postDelayed(() ->
                        GetUserDetailsService.startActionFetchUserDetails(getContext()), 5000);
            }
        }
    }

    @Override
    protected void optionRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        onRefreshListener.onRefresh();
    }

    @Override
    protected void initViewModel() {
        //viewModel =  ViewModelProviders.of(this, Injection.provideViewModelFactory(activity)).get(DataViewModel.class);
        viewModel = ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getPagedRecipes().observe(this, recipesList -> {

            firstLoadingProgressBar.setVisibility(View.GONE);
            filtersViewHider.show();
            swipeRefreshLayout.setRefreshing(false);

            if (recipesList != null) {
                mAdapter.submitList(recipesList);
            }
        });

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
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

        viewModel.getInfo().observe(this, message -> {
            //Log.e(TAG, message);
            swipeRefreshLayout.setRefreshing(false);
            firstLoadingProgressBar.setVisibility(View.GONE);
            if (!message.equals(""))
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        });
    }
}
