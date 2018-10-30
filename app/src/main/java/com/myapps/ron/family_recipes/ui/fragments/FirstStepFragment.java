package com.myapps.ron.family_recipes.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.ui.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class FirstStepFragment extends MyFragment implements FilterListener<Category> {

    private AppCompatEditText editTextName, editTextDesc;
    private Filter<Category> mFilter;
    private List<Category> allTags;
    private List<String> tags;
    private int[] mColors;
    private String name, desc;

    private PostRecipeViewModel viewModel;
    private PostRecipeActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (PostRecipeActivity)getActivity();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.content_post_first_step, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextName = view.findViewById(R.id.recipe_name_editText);
        editTextDesc = view.findViewById(R.id.recipe_desc_editText);
        mFilter = view.findViewById(R.id.first_step_filter);

        mColors = getResources().getIntArray(R.array.colors);

        initViewModel();
        setListeners();

        activity.setTitle("post 1/3");
        viewModel.loadCategories(activity);
    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if(categories != null) {
                    allTags = new ArrayList<>(categories);
                    allTags.add(0, new Category(getString(R.string.str_all_selected), mColors[0]));
                    setCategories();
                    initCategories();
                }
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

        mFilter.setAdapter(new FirstStepFragment.Adapter(allTags));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setCustomTextView(getString(R.string.str_all_selected));
        mFilter.build();

    }

    private void setCategories() {
        for (int i = 0; i < allTags.size(); ++i) {
            if(allTags.get(i).getColor() == 0)
                allTags.get(i).setColor(mColors[i]);
        }
    }

    private void setListeners() {
        activity.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValidation()) {
                    viewModel.recipe.setName(name);
                    viewModel.recipe.setDescription(desc);
                    viewModel.recipe.setCategories(tags);
                    activity.nextFragment();
                }
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
        if (tags == null || tags.size() < Constants.MIN_TAGS) {
            if (valid)
                Toast.makeText(activity, getString(R.string.post_recipe_tags_required, Constants.MIN_TAGS), Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private List<String> convertCategoriesToString(@Nullable ArrayList<Category> arrayList) {
        if (arrayList == null)
            return  null;
        List<String> results = new ArrayList<>();
        for (Category cat: arrayList) {
            results.add(cat.getText());
        }
        return results;
    }

    @Override
    public void onFiltersSelected(@Nullable ArrayList<Category> filters) {
        tags = convertCategoriesToString(filters);
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onFilterSelected(Category item) {
        if (item.getText().equals(allTags.get(0).getText())) {
            mFilter.deselectAll();
            mFilter.collapse();
        }
    }

    @Override
    public void onFilterDeselected(Category item) {

    }


    class Adapter extends FilterAdapter<Category> {

        Adapter(@NonNull List<? extends Category> items) {
            super(items);
        }

        @NonNull
        @Override
        public FilterItem createView(int position, Category item) {
            FilterItem filterItem = new FilterItem(activity);

            if (item.getText().equals(allTags.get(0).getText()))
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