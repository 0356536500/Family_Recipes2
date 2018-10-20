package com.myapps.ron.family_recipes;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.myapps.ron.family_recipes.dal.DataViewModel;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.recycler.RecipesAdapter;
import com.myapps.ron.family_recipes.ui.RecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TestActivity extends AppCompatActivity implements RecipesAdapter.RecipesAdapterListener, FilterListener<Category> {

    private static final String TAG = TestActivity.class.getSimpleName();

    private int[] mColors;
    private String[] mTitles;
    private Filter<Category> mFilter;

    private RecyclerView recyclerView;
    private RecipesAdapter mAdapter;

    private DataViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initViewModel();
        initCategories();
        initRecycler();

        fetchRecipes(com.myapps.ron.family_recipes.dal.Constants.SORT_RECENT);
    }

    public void fetchRecipes(String orderBy) {
        viewModel.loadRecipes(this, orderBy);
    }


    private void initCategories() {
        mColors = getResources().getIntArray(R.array.colors);
        mTitles = getResources().getStringArray(R.array.job_titles);

        mFilter = findViewById(R.id.filter);
        mFilter.setAdapter(new TestActivity.Adapter(getCategories()));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setNoSelectedItemText(getString(R.string.str_all_selected));
        mFilter.build();

    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(this).get(DataViewModel.class);
        viewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                //Toast.makeText(activity, "get recipes from DAL", Toast.LENGTH_SHORT).show();
                mAdapter.updateRecipes(recipes);
            }
        });
    }

    private void initRecycler() {
        recyclerView = findViewById(R.id.list);
        List<Recipe> recipeList = new ArrayList<>();
        mAdapter = new RecipesAdapter(this, recipeList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new FiltersListItemAnimator());
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_search:
                Toast.makeText(this, "search clicked (" + TAG + ")", Toast.LENGTH_SHORT).show();
                return true;
            /*case R.id.action_filter:
                Toast.makeText(this, "filter clicked (" + TAG + ")", Toast.LENGTH_SHORT).show();
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
    public void onItemSelected(Recipe recipe) {
        Intent intent = new Intent(this, RecipeActivity.class);
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

    private List<Category> getCategories() {
        List<Category> tags = new ArrayList<>();
        List<String> subs = new ArrayList<>(Arrays.asList(mTitles));

        for (int i = 0; i < mTitles.length; ++i) {
            tags.add(new Category(mTitles[i], mColors[i], pickTwo(i, subs)));
        }
        Log.e(TAG, tags.toString());

        return tags;
    }

    private List<String> pickTwo(int i,List<String> list) {
        List<String> result = new ArrayList<>();
        result.add(pickOne(list) + i);
        result.add(pickOne(list) + i);
       /* Random rand = new Random();
        result.add(list.get(rand.nextInt(list.size())));
        result.add(list.get(rand.nextInt(list.size())));*/
        return result;
    }

    private <T> T pickOne(List<T> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    @Override
    public void onFilterDeselected(Category category) {

    }

    @Override
    public void onFilterSelected(Category item) {
        if (item.getText().equals(mTitles[0])) {
            mFilter.deselectAll();
            mFilter.collapse();
        }
    }

    @Override
    public void onFiltersSelected(ArrayList<Category> arrayList) {
        for(Category cat: arrayList) {
            Log.e(TAG, cat.getText());
        }
    }

    @Override
    public void onNothingSelected() {

    }

    class Adapter extends FilterAdapter<Category> {

        Adapter(@NonNull List<? extends Category> items) {
            super(items);
        }

        @NonNull
        @Override
        public FilterItem createView(int position, Category item) {
            FilterItem filterItem = new FilterItem(TestActivity.this);

            if (item.getText().equals(mTitles[0]))
                filterItem.setHeader(true);
            filterItem.setStrokeColor(mColors[0]);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCornerRadius(75f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(TestActivity.this, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(TestActivity.this, android.R.color.white));
            filterItem.setCheckedColor(mColors[position]);
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }

        @NonNull
        @Override
        public FilterItem createSubCategory(int position, Category item, @NonNull FilterItem parent) {
            /*Integer[] color = new Integer[mColors.length];
            for (int i = 0; i < mColors.length; i++) {
                color[i] = mColors[i];
            }

            List<Integer> cols = new ArrayList<>(Arrays.asList(color));
            int one = pickOne(cols);*/

            FilterItem filterItem = new FilterItem(TestActivity.this);

            filterItem.setContainer(true);
            filterItem.setStrokeColor(parent.getCheckedColor());
            filterItem.setTextColor(parent.getCheckedColor());
            filterItem.setCornerRadius(100f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(TestActivity.this, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(TestActivity.this, android.R.color.white));
            filterItem.setCheckedColor(item.getColor());
            filterItem.setText(item.getCategories().get(position));
            filterItem.deselect();

            return filterItem;
        }
    }
}
