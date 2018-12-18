package com.myapps.ron.family_recipes.ui.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.recycler.MyRecyclerScroll;
import com.myapps.ron.family_recipes.ui.activities.MainActivity;
import com.myapps.ron.family_recipes.ui.activities.RecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ronginat on 18/12/2018.
 */
public abstract class RecyclerWithFiltersAbstractFragment extends MyFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<Category> {

    protected final String TAG = getClass().getSimpleName();

    protected MainActivity activity;

    private View view;
    private FrameLayout parent;

    protected int[] mColors;
    protected int filterBackgroundColor, filterTextColor;

    protected Filter<Category> mFilter;
    protected List<Category> tags;

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    protected RecyclerView recyclerView;
    protected RecipesAdapter mAdapter;

    protected DataViewModel viewModel;
    protected String orderBy;
    private boolean mayRefresh;
    private String lastQuery = "";

    ProgressBar firstLoadingProgressBar;


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
    public void onAttach(Context context) {
        Log.e(TAG, "on attach");
        super.onAttach(context);
        if (parent != null) {
            parent.addView(swipeRefreshLayout);
            parent.addView(mFilter);
        }
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "on detach");
        super.onDetach();
        parent.removeAllViews();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "on createView");
        if (mFilter == null) {
            Log.e(TAG, "on createView mFilter was null");
            view = inflater.inflate(R.layout.content_main_recipes, container, false);
            parent = (FrameLayout) view;
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "on viewCreated");
        if (mFilter == null) {
            Log.e(TAG, "on viewCreated mFilter was null");
            swipeRefreshLayout = view.findViewById(R.id.content_main_refresh);
            recyclerView = view.findViewById(R.id.recycler_view);
            mFilter = view.findViewById(R.id.content_main_filters);
            firstLoadingProgressBar = view.findViewById(R.id.content_main_fist_loading_animation);

            mColors = getResources().getIntArray(R.array.colors);

            orderBy = com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT;
            mayRefresh = true;

            initViewModel();
            //initCategories();
            initRecycler();

            // Associate searchable configuration with the SearchView
            setSearchView(activity.getMenu());
            
            initAfterViewCreated();
        }

    }

    protected abstract void initAfterViewCreated();
    protected abstract void initViewModel();
    protected abstract void optionRefresh();

    protected void initCategories() {
        //mTitles = getResources().getStringArray(R.array.job_titles);

        mFilter.setAdapter(new RecyclerWithFiltersAbstractFragment.Adapter(tags));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setCustomTextView(getString(R.string.str_all_selected));

        //set the collapsed text color according to current theme
        TypedValue value = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.searchFilterCustomTextColor, value, true);
        mFilter.setCustomTextViewColor(value.data);

        mFilter.build();
    }

    /*private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                String log = "null";
                if(recipes != null)
                    log = recipes.toString();
                Log.e(TAG, "getAllRecipes from db.\n" + log);
                if (mFilter != null && recipes != null)
                    mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, recipes.size()));
                firstLoadingProgressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                //Log.e(TAG, "update from fragment");
                if (mAdapter == null) {
                    mAdapter = new RecipesAdapter(activity, recipes, RecyclerWithFiltersAbstractFragment.this);
                    recyclerView.setAdapter(mAdapter);
                } else
                    mAdapter.updateRecipes(recipes, recipes != null && !recipes.isEmpty());
            }
        });
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if(categories != null) {
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
                    Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void initRecycler() {
        //List<Recipe> recipeList = new ArrayList<>(viewModel.loadLocalRecipesOrdered(activity, com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT));
        //mAdapter = new RecipesAdapter(activity, recipeList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(activity, DividerItemDecoration.VERTICAL, 36));
        //recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new FiltersListItemAnimator());
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                if (mFilter != null && mFilter.isCollapsed()) {
                    mFilter.animate().translationY(0).setInterpolator(new DecelerateInterpolator(1.5f)).start();
                }
            }

            @Override
            public void hide() {
                if (mFilter != null && mFilter.isCollapsed()) {
                    mFilter.animate().translationY(-mFilter.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
                }
            }
        });
    }

    protected void setRefreshLayout() {
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
                optionRefresh();
                return true;
            case R.id.action_sort:
                showPopupSortMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showPopupSortMenu() {
        final PopupMenu popup = new PopupMenu(activity, activity.findViewById(R.id.action_sort));

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String mPrevOrder = orderBy;
                orderBy = "";
                switch (item.getItemId()) {
                    case R.id.sort_action_recent:
                        orderBy = com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT;
                        break;
                    case R.id.sort_action_popular:
                        orderBy = com.myapps.ron.family_recipes.dal.Constants.SORT_POPULAR;
                        break;
                    case R.id.sort_action_last_modified:
                        orderBy = com.myapps.ron.family_recipes.dal.Constants.SORT_MODIFIED;
                        break;
                }
                if (orderBy != null) {
                    if (!orderBy.equals(mPrevOrder)) {
                        mAdapter.updateRecipesOrder(viewModel.loadLocalRecipesOrdered(activity, orderBy));
                    }
                }
                return true;
            }

        });
        popup.inflate(R.menu.sort_menu);
        popup.show();
        setSortItemChecked(popup.getMenu());
    }

    private void setSortItemChecked(Menu menu) {
        boolean[] sorts = new boolean[3];
        switch (orderBy) {
            case com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT:
                sorts[0] = true;
                break;
            case com.myapps.ron.family_recipes.dal.Constants.SORT_POPULAR:
                sorts[1] = true;
                break;
            case com.myapps.ron.family_recipes.dal.Constants.SORT_MODIFIED:
                sorts[2] = true;
                break;
        }

        menu.findItem(R.id.sort_action_recent).setChecked(sorts[0]);
        menu.findItem(R.id.sort_action_popular).setChecked(sorts[1]);
        menu.findItem(R.id.sort_action_last_modified).setChecked(sorts[2]);
    }

    @Override
    public void onItemSelected(Recipe recipe) {
        Intent intent = new Intent(activity, RecipeActivity.class);
        intent.putExtra(Constants.RECIPE, recipe);
        startActivityForResult(intent, Constants.RECIPE_ACTIVITY_CODE);
    }

    @Override
    public void onImageClicked(Recipe recipe) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new PagerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RECIPE, recipe);
        newFragment.setArguments(bundle);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void onCurrentSizeChanged(int size) {
        if (mFilter != null)
            mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, size));
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

    protected void loadFiltersColor() {
        TypedValue backgroundValue = new TypedValue();
        TypedValue textValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.searchFilterBackgroundColor, backgroundValue, true);
        activity.getTheme().resolveAttribute(R.attr.searchFilterTextColor, textValue, true);
        filterBackgroundColor = backgroundValue.data;
        filterTextColor = textValue.data;
    }

    protected void setCategories() {
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
        if (mAdapter != null)
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
            filterItem.setTextColor(filterTextColor);
            filterItem.setCornerRadius(75f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setColor(filterBackgroundColor);
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
            filterItem.setColor(filterBackgroundColor);
            filterItem.setCheckedColor(item.getColor());
            filterItem.setText(item.getCategories().get(position));
            filterItem.deselect();

            return filterItem;
        }
    }
}
