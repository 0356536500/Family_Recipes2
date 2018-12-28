package com.myapps.ron.family_recipes;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.Category1;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.viewmodels.DataViewModel;
import com.myapps.ron.searchfilter.adapter.FilterAdapter;
import com.myapps.ron.searchfilter.listener.FilterListener;
import com.myapps.ron.searchfilter.widget.Filter;
import com.myapps.ron.searchfilter.widget.FilterItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TestActivity extends AppCompatActivity implements FilterListener<Category> {

    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.flexBox_layout)
    FlexboxLayout flexboxLayout;

    FlexboxLayout.LayoutParams layoutParams;

    @BindView(R.id.test_filters)
    Filter<Category> mFilter;
    private List<Category> tags;

    private int[] mColors;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mColors = getResources().getIntArray(R.array.colors);

        ButterKnife.bind(this);

        new Handler().postDelayed(this::createMoreViews, 1000);

        DataViewModel viewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        viewModel.getCategories().observe(this, categories -> {
            /*for (Category category: categories) {
                Log.e(TAG, category.toString());
            }*/
            if (categories != null) {
                tags = new ArrayList<>(categories);
                tags.add(0, new Category.CategoryBuilder()
                        .name(getString(R.string.str_all_selected))
                        .color(mColors[0])
                        .build());
                setCategories();
                initCategories();
            }
        });

        viewModel.loadFavoritesCategories(this);
    }

    protected void setCategories() {
        //List<Category> tags = new ArrayList<>();

        for (int i = 1; i < tags.size(); ++i) {
            //tags.add(new Category(mTitles[i], mColors[i]));
            if(tags.get(i).getIntColor() == 0)
                tags.get(i).setIntColor(mColors[i % mColors.length]);
        }

        //return tags;
    }

    protected void initCategories() {
        //mTitles = getResources().getStringArray(R.array.job_titles);

        mFilter.setAdapter(new TestActivity.Adapter(tags));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setCustomTextView(getString(R.string.str_all_selected));
        mFilter.build();
    }

    private void getCategories() {
        APICallsHandler.getAllCategories("0", AppHelper.getAccessToken(), result -> {
            if (result != null) {
                for (Category cat: result) {
                    Log.e(TAG, cat.toString());
                }
            }
        });
    }

    private void createMoreViews() {
        layoutParams = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 20;
        layoutParams.bottomMargin = 20;
        layoutParams.rightMargin = 20;
        layoutParams.leftMargin = 20;
        //layoutParams.setMargins(16,16,16,16);

        for (int i = 0; i < 20; i++) {
            TextView view = (TextView) LayoutInflater.from(this).inflate(R.layout.just_text_view, flexboxLayout, false);
            //TextView view = new TextView(this);
            String text = "Message#" + i;
            view.setText(text);
            //view.setTextSize(30f);
            //view.setLayoutParams(layoutParams);

            flexboxLayout.addView(view);
        }

    }


    @Override
    public void onFiltersSelected(@Nullable @NotNull ArrayList<Category> filters) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onFilterSelected(Category item) {

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

        @NotNull
        @Override
        public FilterItem createView(Category item) {
            FilterItem filterItem = new FilterItem(TestActivity.this);

            if (item.getText().equals(tags.get(0).getText()))
                filterItem.setHeader(true);
            filterItem.setStrokeColor(mColors[0]);
            //filterItem.setTextColor(filterTextColor);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCornerRadius(70f);
            filterItem.setCheckedTextColor(ContextCompat.getColor(TestActivity.this, android.R.color.white));
            filterItem.setColor(Color.WHITE);
            //filterItem.setCheckedColor(mColors[pickColor()]);
            if (filterItem.isHeader())
                filterItem.setCheckedColor(mColors[0]);
            else
                filterItem.setCheckedColor(item.getIntColor());
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }

    }
}
