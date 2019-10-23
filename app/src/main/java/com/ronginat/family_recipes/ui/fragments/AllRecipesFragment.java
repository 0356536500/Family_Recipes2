package com.ronginat.family_recipes.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.background.services.GetUserDetailsService;
import com.ronginat.family_recipes.model.CategoryEntity;
import com.ronginat.family_recipes.recycler.adapters.RecipesAdapter;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.viewmodels.DataViewModel;
import com.ronginat.searchfilter.listener.FilterListener;

import java.util.ArrayList;

public class AllRecipesFragment extends RecyclerWithFiltersAbstractFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<CategoryEntity> {

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(itemView, savedInstanceState);

        setRefreshLayout();

        new Handler().postDelayed(() -> viewModel.applyQuery(queryModel), 500);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(Constants.FIRST_LOAD_FRAGMENT, false)) {
            new Handler().postDelayed(() -> {
                if (savedInstanceState == null) {
                    firstLoadingProgressBar.setVisibility(View.VISIBLE);
                    viewModel.fetchFromServer(activity, false);
                }
                //viewModel.applyQuery(queryModel);
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
                tags.add(0, new CategoryEntity.Builder()
                        .name(getString(R.string.str_all_selected))
                        .color(ContextCompat.getColor(activity, R.color.search_filter_text_dark))
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
