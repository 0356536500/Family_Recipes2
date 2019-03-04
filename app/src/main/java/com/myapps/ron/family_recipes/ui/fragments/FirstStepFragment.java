package com.myapps.ron.family_recipes.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import static androidx.core.content.ContextCompat.getDrawable;

/**
 * Created by ronginat on 29/10/2018.
 */
public class FirstStepFragment extends PostRecipeBaseFragment implements FilterListener<CategoryEntity> {

    private View view;
    private FrameLayout parent;
    //private RelativeLayout floater;
    private AppCompatEditText editTextName, editTextDesc;
    private Filter<CategoryEntity> mFilter;
    private List<CategoryEntity> allTags;
    private List<String> chosenTags;

    private String name, desc;

    private PostRecipeViewModel viewModel;
    //private PostRecipeActivity activity;

    private int filterBackgroundColor, filterTextColor;

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (PostRecipeActivity)getActivity();
    }*/

    @Override
    public boolean onBackPressed() {
        if (!mFilter.isCollapsed()) {
            mFilter.collapse();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.e(getClass().getSimpleName(), "on attach");
        /*if (parent != null){
            parent.addView(mFilter);
            parent.addView(floater);
        }*/
    }

    @Override
    public void onDetach(){
        super.onDetach();
        Log.e(getClass().getSimpleName(), "on detach");
        /*mFilter = view.findViewById(R.id.first_step_filter);
        floater = parent.findViewById(R.id.create_recipe_first_step_layout);*/
        parent.removeAllViews();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (parent == null) {
            view = inflater.inflate(R.layout.content_post_first_step, container,false);
            parent = (FrameLayout) view;

            initViewModel();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(getClass().getSimpleName(), "on view created");
        //activity.setTitle(getString(R.string.nav_main_post_recipe) + " 1/3");

        //if (mFilter == null) {
            mFilter = view.findViewById(R.id.first_step_filter);

            editTextName = view.findViewById(R.id.recipe_name_editText);
            editTextDesc = view.findViewById(R.id.recipe_desc_editText);

            setListeners();
        //}

        togglePersistentUi();

        /*editTextDesc = view.findViewById(R.id.recipe_desc_editText);
        editTextName = view.findViewById(R.id.recipe_name_editText);

        mColors = getResources().getIntArray(R.array.colors);

        initViewModel();
        setListeners();

        activity.setTitle(getString(R.string.nav_main_post_recipe) + " 1/3");

        if (mFilter == null) {
            mFilter = view.findViewById(R.id.first_step_filter);
            viewModel.loadCategories(activity);
        }*/
    }

    // region PostRecipeBaseFragment Overrides

    @Override
    protected String getTitle() {
        return getString(R.string.nav_main_post_recipe) + " 1/3";
    }

    // endregion

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
        viewModel.getCategories().observe(this, categories -> {
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
        Log.e(getClass().getSimpleName(), "init categories");
        mFilter.setAdapter(new FirstStepFragment.Adapter(allTags));
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

    private void setListeners() {
        activity.expandedButton.setOnClickListener(view -> {
            Log.e(getClass().getSimpleName(), "next listener");
            if (checkValidation()) {
                Log.e(getClass().getSimpleName(), "data validated");
                viewModel.recipe.setName(name);
                viewModel.recipe.setDescription(desc);
                viewModel.recipe.setCategories(chosenTags);
                activity.nextFragment();
            }
        });

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()) {
                    editTextName.getBackground().setTint(Color.BLACK);
                    name = editable.toString();
                }
            }
        });

        editTextDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()) {
                    editTextDesc.getBackground().setTint(Color.BLACK);
                    desc = editable.toString();
                }
            }
        });
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

    private List<String> convertCategoriesToString(ArrayList<CategoryEntity> arrayList) {
        List<String> results = null;
        if (arrayList != null) {
            results = new ArrayList<>();
            for (CategoryEntity cat : arrayList) {
                results.add(cat.getText());
            }
        }
        return results;
    }

    @Override
    public void onFiltersSelected(@NonNull ArrayList<CategoryEntity> filters) {
        chosenTags = convertCategoriesToString(filters);
        if (chosenTags != null)
            Collections.sort(chosenTags);
    }

    @Override
    public void onNothingSelected() {
        chosenTags = null;
    }

    @Override
    public void onFilterSelected(CategoryEntity item) {
        /*if (item.getText().equals(allTags.get(0).getText())) {
            mFilter.deselectAll();
            mFilter.collapse();
        }*/
    }

    @Override
    public void onFilterDeselected(CategoryEntity item) {

    }


    class Adapter extends FilterAdapter<CategoryEntity> {

        Adapter(@NonNull List<? extends CategoryEntity> items) {
            super(items);
        }

        @NotNull
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
