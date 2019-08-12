package com.ronginat.family_recipes.ui.fragments;

import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.model.CategoryEntity;
import com.ronginat.family_recipes.model.QueryModel;
import com.ronginat.family_recipes.model.RecipeEntity;
import com.ronginat.family_recipes.model.RecipeMinimal;
import com.ronginat.family_recipes.recycler.adapters.RecipesAdapter;
import com.ronginat.family_recipes.recycler.helpers.MyRecyclerScroll;
import com.ronginat.family_recipes.ui.activities.MainActivity;
import com.ronginat.family_recipes.ui.activities.RecipeActivity;
import com.ronginat.family_recipes.ui.baseclasses.MyFragment;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;
import com.ronginat.family_recipes.utils.ui.ViewHider;
import com.ronginat.family_recipes.viewmodels.DataViewModel;
import com.ronginat.searchfilter.Constant;
import com.ronginat.searchfilter.adapter.FilterAdapter;
import com.ronginat.searchfilter.animator.FiltersListItemAnimator;
import com.ronginat.searchfilter.listener.FilterListener;
import com.ronginat.searchfilter.widget.Filter;
import com.ronginat.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ronginat on 18/12/2018.
 */
public abstract class RecyclerWithFiltersAbstractFragment extends MyFragment implements RecipesAdapter.RecipesAdapterListener, FilterListener<CategoryEntity> {

    protected final String TAG = getClass().getSimpleName();

    protected MainActivity activity;

    private View view;
    private FrameLayout parent;

    private int filterBackgroundColor, filterTextColor;

    ViewHider filtersViewHider;
    private Filter<CategoryEntity> mFilter;
    List<CategoryEntity> tags;
    protected List<RecipeEntity> recipes;

    SwipeRefreshLayout swipeRefreshLayout;
    SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private RecyclerView recyclerView;
    RecipesAdapter mAdapter;

    protected DataViewModel viewModel;
    String orderBy;
    private String lastQuery = "";

    ProgressBar firstLoadingProgressBar;

    QueryModel queryModel;

    //private int verticalScrollPosition;

    // region Fragment Override Methods

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //verticalScrollPosition = -1;

        if (savedInstanceState != null) {
            //verticalScrollPosition = savedInstanceState.getInt("position");
            queryModel = (QueryModel) savedInstanceState.getSerializable("query");
        }
        if (queryModel == null)
            queryModel = new QueryModel.Builder()
                    .order(RecipeEntity.KEY_CREATED)
                    .build();

        activity = (MainActivity)getActivity();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", recyclerView.getVerticalScrollbarPosition());
        outState.putSerializable("query", queryModel);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //Log.e(TAG, "onDetach");
        /*parent.removeAllViews();
        parent = null;
        view = null;
        mFilter = null;*/
    }

    @Override
    public boolean onBackPressed() {
        if (!mFilter.isCollapsed()) {
            mFilter.collapse();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Log.e(TAG, "on createView");
        if (parent == null) {
            CrashLogger.e(TAG, "on createView parent was null");
            view = inflater.inflate(R.layout.content_main_recipes, container, false);
            parent = (FrameLayout) view;
        } else
            view = parent;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        //Log.e(TAG, "on viewCreated");
        if (mFilter == null) {
            CrashLogger.e(TAG, "on viewCreated mFilter was null");
            swipeRefreshLayout = view.findViewById(R.id.content_main_refresh);
            recyclerView = view.findViewById(R.id.recycler_view);
            mFilter = view.findViewById(R.id.content_main_filters);
            filtersViewHider = ViewHider.of(mFilter).setDirection(ViewHider.TOP).setDuration(Constants.FILTERS_TRANSLATE_ANIMATION_DURATION).build();
            firstLoadingProgressBar = view.findViewById(R.id.content_main_fist_loading_animation);

            orderBy = com.ronginat.family_recipes.logic.Constants.SORT_RECENT;

            initViewModel();
            //initCategories();
            initRecycler();

            // Associate searchable configuration with the SearchView
            setSearchView(activity.getMenu());
        }
    }

    protected abstract void initViewModel();
    protected abstract void optionRefresh();

    void initCategories() {
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

    /**
     * Change {@link #recyclerView} padding according to
     */
    private void changeRecyclerPadding(int from, int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(Constants.RECYCLER_TRANSLATE_ANIMATION_DURATION);
        animator.addUpdateListener(valueAnimator -> recyclerView.post(() -> recyclerView.setPadding(0, (int) valueAnimator.getAnimatedValue() , 0, 0)));
        animator.start();
    }

    // region setName Views
    private void initRecycler() {
        recyclerView.setItemAnimator(new FiltersListItemAnimator());
        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                if (mFilter != null && mFilter.isCollapsed()) {
                    filtersViewHider.show();
                    changeRecyclerPadding(0, getResources().getDimensionPixelSize(R.dimen.container_height));
                    //recyclerView.post(() -> recyclerView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.container_height) , 0, 0));
                    //mFilter.animate().translationY(0).setInterpolator(new DecelerateInterpolator(1.5f)).start();
                }
            }

            @Override
            public void hide() {
                if (mFilter != null && mFilter.isCollapsed()) {
                    filtersViewHider.hide();
                    changeRecyclerPadding(getResources().getDimensionPixelSize(R.dimen.container_height), 0);
                    //recyclerView.post(() -> recyclerView.setPadding(0, 0, 0, 0));
                    //mFilter.animate().translationY(-mFilter.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
                }
            }
        });
        mAdapter = new RecipesAdapter(activity, this);
        recyclerView.setAdapter(mAdapter);
    }

    void setRefreshLayout() {
        /*ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        ((FrameLayout)view.findViewById(R.id.main_container)).addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);*/
        onRefreshListener = () -> viewModel.fetchFromServer(activity, true);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.colors));
        //swipeRefreshLayout.setProgressViewOffset(false, swipeRefreshLayout.getProgressViewStartOffset(), swipeRefreshLayout.getProgressViewEndOffset());
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
                    CrashLogger.e(TAG, "expanded, search: " + lastQuery);
                    searchView.post(() ->
                            searchView.setQuery(lastQuery, false));
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return true;
                }
            });

            // listening to search search text change
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // filter recycler view when search submitted
                    MenuItem searchMenuItem = activity.getSearchMenuItem();
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                    }
                    Toast.makeText(activity, getString(R.string.search_submit_message, query), Toast.LENGTH_SHORT).show();
                    lastQuery = query;
                    queryModel.setSearch(lastQuery);
                    viewModel.applyQuery(queryModel);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    // filter recycler view when text is changed
                    if ("".equals(query))
                        lastQuery = query;
                    queryModel.setSearch(query);
                    viewModel.applyQuery(queryModel);
                    return true;
                }
            });
        }
    }

    // endregion

    // region Option Menu

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

    private void showPopupSortMenu() {
        TypedValue theme = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.toolbar_theme, theme, true);
        Context wrapper = new ContextThemeWrapper(activity, theme.resourceId);
        final PopupMenu popup = new PopupMenu(wrapper, activity.findViewById(R.id.action_sort));

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            String mPrevOrder = orderBy;
            orderBy = "";
            switch (item.getItemId()) {
                case R.id.sort_action_recent:
                    orderBy = com.ronginat.family_recipes.logic.Constants.SORT_RECENT;
                    break;
                case R.id.sort_action_popular:
                    orderBy = com.ronginat.family_recipes.logic.Constants.SORT_POPULAR;
                    break;
                case R.id.sort_action_last_modified:
                    orderBy = com.ronginat.family_recipes.logic.Constants.SORT_MODIFIED;
                    break;
            }
            if (orderBy != null) {
                if (!orderBy.equals(mPrevOrder)) {
                    queryModel.setOrderBy(orderBy);
                    viewModel.applyQuery(queryModel);
                }
            }
            return true;
        });
        popup.inflate(R.menu.sort_menu);
        popup.show();
        setSortItemChecked(popup.getMenu());
    }

    private void setSortItemChecked(Menu menu) {
        boolean[] sorts = new boolean[3];
        switch (orderBy) {
            case com.ronginat.family_recipes.logic.Constants.SORT_RECENT:
                sorts[0] = true;
                break;
            case com.ronginat.family_recipes.logic.Constants.SORT_POPULAR:
                sorts[1] = true;
                break;
            case com.ronginat.family_recipes.logic.Constants.SORT_MODIFIED:
                sorts[2] = true;
                break;
        }

        menu.findItem(R.id.sort_action_recent).setChecked(sorts[0]);
        menu.findItem(R.id.sort_action_popular).setChecked(sorts[1]);
        menu.findItem(R.id.sort_action_last_modified).setChecked(sorts[2]);
    }

    // endregion

    // region Recycler Listener

    @Override
    public void onFavoriteClicked(RecipeMinimal recipe, Runnable onError) {
        // change recipe on server
        viewModel.changeLike(activity, recipe.getId(), onError);
    }

    @Override
    public void onThumbnailAccessed(String id) {
        viewModel.updateAccessToRecipeThumbnail(id);
    }

    @Override
    public void onItemSelected(RecipeMinimal recipeMinimal) {
        Intent intent = new Intent(activity, RecipeActivity.class);
        intent.putExtra(Constants.RECIPE_ID, recipeMinimal.getId());
        startActivity(intent);
    }

    @Override
    public void onImageClicked(RecipeMinimal recipeMinimal) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        viewModel.getRecipeImages(recipeMinimal.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableMaybeObserver<List<String>>() {
                    @Override
                    public void onSuccess(List<String> foodFiles) {
                        if (foodFiles != null) {
                            viewModel.updateAccessToRecipeImages(recipeMinimal.getId());
                            DialogFragment newFragment = new PagerDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(PagerDialogFragment.PAGER_TYPE_KEY, PagerDialogFragment.PAGER_TYPE.IMAGES);
                            bundle.putStringArrayList(Constants.PAGER_FOOD_IMAGES, new ArrayList<>(foodFiles));
                            newFragment.setArguments(bundle);
                            newFragment.show(ft, "dialog");
                        }
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(activity, R.string.recipe_has_no_images_message, Toast.LENGTH_SHORT).show();
                        dispose();
                    }
                });
    }

    @Override
    public void onCurrentSizeChanged(int size) {
        if (mFilter != null)
            mFilter.setCustomTextView(getString(R.string.number_of_recipes_indicator, size));
    }

    @Override
    public Single<String> getDisplayedName(String username) {
        return viewModel.getDisplayedName(activity, username);
    }

    void scrollToTop() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            recyclerView.postDelayed(() -> layoutManager.scrollToPositionWithOffset(0, 0), 300);
            filtersViewHider.show();
        }
    }

    // endregion

    void loadFiltersColor() {
        TypedValue backgroundValue = new TypedValue();
        TypedValue textValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.searchFilterBackgroundColor, backgroundValue, true);
        activity.getTheme().resolveAttribute(R.attr.searchFilterTextColor, textValue, true);
        filterBackgroundColor = backgroundValue.data;
        filterTextColor = textValue.data;
    }

    private List<String> convertCategoriesToSortedStringList(@NonNull ArrayList<CategoryEntity> arrayList) {
        List<String> results = null;
        if (!arrayList.isEmpty()) {
            results = new ArrayList<>();
            for (CategoryEntity cat : arrayList) {
                results.add(cat.getText());
            }
            Collections.sort(results);
        }
        return results;
    }

    // region SearchFilter listeners

    @Override
    public void onFilterDeselected(CategoryEntity category) {

    }

    @Override
    public void onFilterSelected(CategoryEntity item) {

    }

    @Override
    public void onFiltersSelected(@NonNull ArrayList<CategoryEntity> arrayList) {
        //List<Recipe> oldList = new ArrayList<>(mAdapter.getCurrentList());
        final List<String> newTags = convertCategoriesToSortedStringList(arrayList);
        new Handler().postDelayed(() -> {
            queryModel.setFilters(newTags);
            viewModel.applyQuery(queryModel);
        }, Constant.ANIMATION_DURATION + 100L);

        //calculateDiff(oldList, mAdapter.getCurrentList());
    }

    @Override
    public void onNothingSelected() {
        new Handler().postDelayed(() -> {
            queryModel.setFilters(null);
            viewModel.applyQuery(queryModel);
        }, Constant.ANIMATION_DURATION + 100L);
        /*queryModel.setFilters(null);
        viewModel.applyQuery(queryModel);*/
    }

    // endregion


    class Adapter extends FilterAdapter<CategoryEntity> {

        Adapter(@NonNull List<? extends CategoryEntity> items) {
            super(items);
        }

        @NonNull
        @Override
        public FilterItem createView(CategoryEntity item, CategoryEntity parent) {
            FilterItem filterItem = new FilterItem(activity);

            filterItem.setTextColor(filterTextColor);
            //filterItem.setTextColor(ContextCompat.getColor(activity, R.color.search_filter_text_light));
            filterItem.setCheckedTextColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setStrokeColor(ContextCompat.getColor(activity, R.color.search_filter_stoke));
            filterItem.setColor(filterBackgroundColor);
            //filterItem.setColor(Color.WHITE);
            filterItem.setCheckedColor(item.getIntColor());

            filterItem.setText(item.getText());

            if (parent != null) {
                filterItem.setStrokeColor(parent.getIntColor());
            }

            if (item.hasSubCategories()) {
                filterItem.setCornerRadius(60f);
                filterItem.setStrokeWidth(7);

            }
            else {
                filterItem.setCornerRadius(80f);
                filterItem.setStrokeWidth(5);
            }

            if (item.getText().equals(tags.get(0).getText())) {
                filterItem.setDeselectHead(true);
                filterItem.setCornerRadius(60f);
                filterItem.setStrokeWidth(7);
            }

            filterItem.deselect();

            return filterItem;
        }
    }
}
