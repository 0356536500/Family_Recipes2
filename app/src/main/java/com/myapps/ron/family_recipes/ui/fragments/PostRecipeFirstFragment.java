package com.myapps.ron.family_recipes.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeFirstFragment extends PostRecipeBaseFragment implements FilterListener<CategoryEntity> {
    //private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recipe_name_editText)
    EditText editTextName;
    @BindView(R.id.recipe_desc_editText)
    EditText editTextDesc;
    @BindView(R.id.first_step_filter)
    Filter<CategoryEntity> mFilter;
    private List<CategoryEntity> allTags;
    private List<String> chosenTags;

    private String name, desc;

    private PostRecipeViewModel viewModel;

    private int filterBackgroundColor, filterTextColor;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_first_step, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        initViewModel();
    }

    // region PostRecipeBaseFragment Overrides

    @Override
    protected String getTitle() {
        return getString(R.string.nav_main_post_recipe) + " 1/3";
    }

    @Override
    protected boolean showExtendedFab() {
        return true;
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return (view -> {
            if (checkValidation()) {
                //Log.e(getClass().getSimpleName(), "data validated");
                viewModel.recipe.setName(name);
                viewModel.recipe.setDescription(desc);
                viewModel.recipe.setCategories(chosenTags);
                activity.nextFragmentDelayed();
            } else
                setFabExtended(false, 1000);
        });
    }

    // endregion

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
        viewModel.getCategories().observe(this, categories -> {
            //Log.e(TAG, "categories observer, " + Boolean.toString(categories != null));
            if(categories != null) {
                allTags = new ArrayList<>(categories);
                loadFiltersColor();
                initCategories();
            }
        });
        /*viewModel.getInfoFromLastFetch().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            }
        });*/
    }

    private void initCategories() {
        //Log.e(getClass().getSimpleName(), "setName categories");
        mFilter.setAdapter(new PostRecipeFirstFragment.Adapter(allTags));
        mFilter.setListener(this);

        //set the collapsed text color according to current theme
        TypedValue value = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.searchFilterCustomTextColor, value, true);
        mFilter.setCustomTextViewColor(value.data);
        //the text to show when there's no selected items
        mFilter.setCustomTextView(getString(R.string.str_all_selected));
        mFilter.build();
    }

    private void loadFiltersColor() {
        TypedValue backgroundValue = new TypedValue();
        TypedValue textValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.searchFilterBackgroundColor, backgroundValue, true);
        activity.getTheme().resolveAttribute(R.attr.searchFilterTextColor, textValue, true);
        filterBackgroundColor = backgroundValue.data;
        filterTextColor = textValue.data;
    }

    @OnTextChanged(value = R.id.recipe_name_editText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChangedNameListener(Editable editable) {
        if (editable != null) {
            afterTextChangedGeneral(editTextName, editable);
            name = editable.toString();
        }
    }

    @OnTextChanged(value = R.id.recipe_desc_editText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChangedDescriptionListener(Editable editable) {
        if (editable != null) {
            afterTextChangedGeneral(editTextDesc, editable);
            desc = editable.toString();
        }
    }

    private void afterTextChangedGeneral(@NonNull View view, @NonNull Editable editable) {
        if(!editable.toString().isEmpty())
            view.getBackground().setTint(Color.BLACK);
    }

    private boolean checkValidation() {
        boolean valid = true;
        if (editTextName.getText() == null || editTextName.getText().toString().isEmpty()) {
            valid = false;
            editTextName.setError(getString(R.string.post_recipe_field_required));
            editTextName.getBackground().setTint(Color.RED);
        }
        if (editTextDesc.getText() == null || editTextDesc.getText().toString().isEmpty()) {
            valid = false;
            editTextDesc.setError(getString(R.string.post_recipe_field_required));
            editTextDesc.getBackground().setTint(Color.RED);
        }
        if (chosenTags == null || chosenTags.size() < Constants.MIN_TAGS) {
            if (valid)
                Toast.makeText(activity, getString(R.string.post_recipe_tags_required, Constants.MIN_TAGS), Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
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

    @Override
    public void onFiltersSelected(@NonNull ArrayList<CategoryEntity> filters) {
        chosenTags = convertCategoriesToSortedStringList(filters);
    }

    @Override
    public void onNothingSelected() {
        chosenTags = null;
    }

    @Override
    public void onFilterSelected(CategoryEntity item) {

    }

    @Override
    public void onFilterDeselected(CategoryEntity item) {

    }


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

            filterItem.deselect();

            return filterItem;
        }

    }
}
