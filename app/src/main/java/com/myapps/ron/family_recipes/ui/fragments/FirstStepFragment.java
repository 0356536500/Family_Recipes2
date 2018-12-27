package com.myapps.ron.family_recipes.ui.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.ui.activities.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ronginat on 29/10/2018.
 */
public class FirstStepFragment extends MyFragment implements FilterListener<Category> {

    private View view;
    private FrameLayout parent;
    private RelativeLayout floater;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(getClass().getSimpleName(), "on attach");
        if (parent != null){
            parent.addView(mFilter);
            parent.addView(floater);
        }
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
        if (mFilter == null) {
            view = inflater.inflate(R.layout.content_post_first_step, container,false);
            parent = (FrameLayout) view;

            mFilter = view.findViewById(R.id.first_step_filter);
            //editTextName = view.findViewById(R.id.recipe_name_editText);
            //editTextDesc = view.findViewById(R.id.recipe_desc_editText);

            mColors = getResources().getIntArray(R.array.colors);

            //setListeners();

            initViewModel();
            viewModel.loadCategories(activity);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(getClass().getSimpleName(), "on view created");
        activity.setTitle(getString(R.string.nav_main_post_recipe) + " 1/3");

        editTextName = view.findViewById(R.id.recipe_name_editText);
        editTextDesc = view.findViewById(R.id.recipe_desc_editText);

        setListeners();

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

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                if(categories != null) {
                    allTags = new ArrayList<>(categories);
                    allTags.add(0, new Category.CategoryBuilder()
                            .name(getString(R.string.str_all_selected))
                            .color(mColors[0])
                            .build());
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
        Log.e(getClass().getSimpleName(), "init categories");
        mFilter.setAdapter(new Adapter(allTags));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setCustomTextView(getString(R.string.str_all_selected));
        mFilter.build();
    }

    private void setCategories() {
        for (int i = 0; i < allTags.size(); ++i) {
            if(allTags.get(i).getIntColor() == 0)
                allTags.get(i).setIntColor(mColors[i % mColors.length]);
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

        private int pickColor() {
            Random rand = new Random(System.currentTimeMillis());
            return rand.nextInt(mColors.length);
        }

        @Override
        public FilterItem createView(Category item) {
            FilterItem filterItem = new FilterItem(activity);

            if (item.getText().equals(allTags.get(0).getText()))
                filterItem.setHeader(true);
            filterItem.setStrokeColor(mColors[0]);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCornerRadius(75f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setCheckedColor(item.getIntColor() == 0 ? pickColor() : item.getIntColor());
            filterItem.setText(item.getText());
            filterItem.deselect();


            return filterItem;
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
            filterItem.setCheckedColor(item.getIntColor());
            filterItem.setText(item.getCategories().get(position).getText());
            filterItem.deselect();

            return filterItem;
        }
    }
}
