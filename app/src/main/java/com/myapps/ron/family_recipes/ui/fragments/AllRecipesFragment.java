package com.myapps.ron.family_recipes.ui.fragments;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.DataViewModel;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.recycler.RecipesAdapter;
import com.myapps.ron.family_recipes.ui.MainActivity;
import com.myapps.ron.family_recipes.ui.RecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AllRecipesFragment extends MyFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<Category> {

    private static final String TAG = AllRecipesFragment.class.getSimpleName();
    private MainActivity activity;

    private int[] mColors;
    //private String[] mTitles;
    private Filter<Category> mFilter;
    private List<Category> tags;

    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private RecyclerView recyclerView;
    private RecipesAdapter mAdapter;

    private DataViewModel viewModel;
    private String orderBy;
    private boolean mayRefresh;
    private String lastQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.content_main_refresh);
        recyclerView = view.findViewById(R.id.recycler_view);

        mColors = getResources().getIntArray(R.array.colors);

        orderBy = com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT;
        mayRefresh = true;

        initViewModel();
        //initCategories();
        initRecycler();

        // Associate searchable configuration with the SearchView
        setSearchView(activity.getMenu());
        setRefreshLayout();
        //setSortToggle(activity.getMenu());

        activity.fetchCategories();
        activity.fetchRecipes(orderBy);
    }


    private void initCategories() {
        //mTitles = getResources().getStringArray(R.array.job_titles);

        mFilter = activity.findViewById(R.id.content_main_filters);
        mFilter.setAdapter(new Adapter(tags));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setNoSelectedItemText(getString(R.string.str_all_selected));
        mFilter.build();

    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                String log = "null";
                if(recipes != null)
                    log = recipes.toString();
                Log.e(TAG, "getAllRecipes from db.\n" + log);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "update from fragment");
                mAdapter.updateRecipes(recipes);
            }
        });
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if(categories != null) {
                    tags = new ArrayList<>(categories);
                    tags.add(0, new Category(getString(R.string.str_all_selected), mColors[0]));
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

    private void initRecycler() {
        List<Recipe> recipeList = new ArrayList<>(viewModel.loadLocalRecipes(activity));
        mAdapter = new RecipesAdapter(activity, recipeList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(activity, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new FiltersListItemAnimator());
    }

    private void setRefreshLayout() {
        onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mayRefresh) {
                    activity.fetchRecipes(orderBy);
                    mayRefresh = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mayRefresh = true;
                        }
                    }, Constants.REFRESH_DELAY);
                } else {
                    Toast.makeText(activity, R.string.refresh_error_message, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        swipeRefreshLayout.setColorSchemeColors(mColors);
    }

    /*private void calculateDiff(final List<Recipe> oldList, final List<Recipe> newList) {
        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).identical(newList.get(newItemPosition));
            }
        }).dispatchUpdatesTo(mAdapter);
    }*/

    private void setSearchView(Menu menu) {
        //MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        if(menu != null && searchManager != null) {
            final SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                    .getActionView();
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(activity.getComponentName()));
            searchView.setMaxWidth(Integer.MAX_VALUE);
            menu.findItem(R.id.action_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    Log.e(TAG, "expanded, query: " + lastQuery);
                    searchView.setQuery(lastQuery, false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return true;
                }
            });

            // listening to search query text change
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // filter recycler view when query submitted
                    MenuItem searchMenuItem = activity.getSearchMenuItem();
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                    }
                    Toast.makeText(activity, "Submitted, " + query, Toast.LENGTH_SHORT).show();
                    lastQuery = query;
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
    }

    private void setSortToggle(Menu menu) {
        ToggleButton toggleButton = menu.findItem(R.id.action_sort).getActionView().findViewById(R.id.toolbar_toggle_sort);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String order = isChecked ? com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT : com.myapps.ron.family_recipes.dal.Constants.SORT_POPULAR;
                viewModel.loadRecipes(activity, order);
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
            /*case R.id.action_search:
                Toast.makeText(activity, "search clicked (" + TAG + ")", Toast.LENGTH_SHORT).show();
                return true;*/
            case R.id.action_refresh:
                swipeRefreshLayout.setRefreshing(true);
                onRefreshListener.onRefresh();
                return true;
            /*case R.id.action_filter:
                Toast.makeText(activity, "filter clicked (" + TAG + ")", Toast.LENGTH_SHORT).show();
                return true;*/
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
    public boolean onBackPressed() {
        if (!mFilter.isCollapsed()) {
            mFilter.collapse();
            return true;
        }
        return false;
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

    private void setCategories() {
        //List<Category> tags = new ArrayList<>();

        for (int i = 0; i < tags.size(); ++i) {
            //tags.add(new Category(mTitles[i], mColors[i]));
            if(tags.get(i).getColor() == 0)
                tags.get(i).setColor(mColors[i]);
        }

        //return tags;
    }

    private List<String> convertCategoriesToString(ArrayList<Category> arrayList) {
        List<String> results = new ArrayList<>();
        for (Category cat: arrayList) {
            results.add(cat.getText());
        }
        return results;
    }

    @Override
    public void onFilterDeselected(Category category) {

    }

    @Override
    public void onFilterSelected(Category item) {
        if (item.getText().equals(tags.get(0).getText())) {
            mFilter.deselectAll();
            mFilter.collapse();
        }
    }

    @Override
    public void onFiltersSelected(ArrayList<Category> arrayList) {
        //List<Recipe> oldList = new ArrayList<>(mAdapter.getCurrentList());
        final List<String> newTags = convertCategoriesToString(arrayList);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.updateTags(newTags);
            }
        }, 500);

        //calculateDiff(oldList, mAdapter.getCurrentList());
    }

    @Override
    public void onNothingSelected() {
        mAdapter.updateTags(null);
    }


    class Adapter extends FilterAdapter<Category> {

        Adapter(@NonNull List<? extends Category> items) {
            super(items);
        }

        @NonNull
        @Override
        public FilterItem createView(int position, Category item) {
            FilterItem filterItem = new FilterItem(activity);

            if (item.getText().equals(tags.get(0).getText()))
                filterItem.setHeader(true);
            filterItem.setStrokeColor(mColors[0]);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCornerRadius(75f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setCheckedColor(mColors[position]);
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }

        @NonNull
        @Override
        public FilterItem createSubCategory(int position, Category item, @NonNull FilterItem parent) {
            FilterItem filterItem = new FilterItem(activity);

            filterItem.setContainer(true);
            filterItem.setStrokeColor(parent.getCheckedColor());
            filterItem.setTextColor(parent.getCheckedColor());
            filterItem.setCornerRadius(100f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setCheckedColor(item.getColor());
            filterItem.setText(item.getCategories().get(position));
            filterItem.deselect();

            return filterItem;
        }
    }
}
