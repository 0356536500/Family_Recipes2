package com.myapps.ron.family_recipes.ui;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.RecipeViewModel;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.utils.Constants;

public class RecipeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton like;
    //private TextView textView;
    private WebView myWebView;
    private Recipe recipe;
    private RecipeViewModel viewModel;
    private Observer<Recipe> likeObserver, commentObserver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        /*like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(like.getTag() == R.drawable.ic_favorite_border_red_36dp)
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        initViewModel();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            recipe = extras.getParcelable(Constants.RECIPE);
            //String path = extras.getString(Constants.RECIPE_PATH, Constants.DEFAULT_RECIPE_PATH);
            if (recipe != null) {
                bindUI();
                setSupportActionBar(toolbar);
                collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
                collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
                setTitle(recipe.getName());

                loadRecipe();
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void bindUI() {
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        toolbar = findViewById(R.id.toolbar);
        like = findViewById(R.id.recipe_like);
        loadLikeImage();
        //like.setTag(R.drawable.ic_favorite_border_red_36dp);
        //textView = findViewById(R.id.recipe_textView);
        myWebView = findViewById(R.id.recipe_content_view);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);
    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(this).get(RecipeViewModel.class);
        likeObserver = new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                RecipeActivity.this.recipe = recipe;
                loadLikeImage();
                viewModel.getRecipe().removeObserver(likeObserver);
            }
        };

        commentObserver = new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                RecipeActivity.this.recipe = recipe;
                //TODO handle comments
                viewModel.getRecipe().removeObserver(commentObserver);
            }
        };
        /*viewModel.getRecipe().observe(this, new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        //searchMenuItem = menu.findItem(R.id.action_share);

        // Associate searchable configuration with the SearchView
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_share:
                return true;
        }
        /*
        //noinspection SimplifiableIfStatement
        if (itemId == R.id.action_search) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    private void loadLikeImage() {
        String message;
        if(recipe.getMeLike()) {
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_36dp));
            message = "like";
        }
        else {
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_red_36dp));
            message = "unlike";
        }
        Snackbar.make(like, message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @SuppressLint("CheckResult")
    private void loadRecipe() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(android.R.drawable.progress_indeterminate_horizontal);// R.drawable.ic_placeholder);
        requestOptions.error(android.R.drawable.stat_notify_error);// ic_error);
        //requestOptions.fitCenter();

        Glide.with(getApplicationContext())
                .asDrawable()
                .load(recipe.image)
                .apply(requestOptions)
                .into(new CustomViewTarget<CollapsingToolbarLayout, Drawable>(collapsingToolbarLayout) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        collapsingToolbarLayout.setBackground(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        collapsingToolbarLayout.setBackground(resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {

                    }
                });
        //myWebView.loadUrl(recipe.image);
    }

    public void doLike(View view) {
        //recipe.setMeLike(!recipe.getMeLike());
        viewModel.getRecipe().observe(this, likeObserver);
        viewModel.changeLike(getApplicationContext(), !recipe.getMeLike(), recipe.getId());
        /*String message;
        // do like
        loadRecipe();
        if((int)view.getTag() == R.drawable.ic_favorite_border_red_36dp) {
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_36dp));
            like.setTag(R.drawable.ic_favorite_red_36dp);
            message = "like";
        }
        // do unlike
        else {
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_red_36dp));
            like.setTag(R.drawable.ic_favorite_border_red_36dp);
            message = "unlike";
        }
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();*/
    }
}
