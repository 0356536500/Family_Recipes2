package com.myapps.ron.family_recipes.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.RecipeViewModel;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.utils.Constants;

public class RecipeActivity extends AppCompatActivity {

    private AppBarLayout appBarLayout;
    private MenuItem menuItemShare;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton like;
    //private TextView textView;
    private WebView myWebView;
    private Recipe recipe;
    private RecipeViewModel viewModel;
    private Observer<Recipe> likeObserver, commentObserver;

    private boolean showLikeMessage = false;
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
                initUI();
                loadRecipe();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void bindUI() {
        appBarLayout = findViewById(R.id.activity_recipe_app_bar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        toolbar = findViewById(R.id.recipe_toolbar);
        like = findViewById(R.id.recipe_like);
        loadLikeImage();
        //like.setTag(R.drawable.ic_favorite_border_red_36dp);
        //textView = findViewById(R.id.recipe_textView);
        myWebView = findViewById(R.id.recipe_content_view);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    private void initUI() {
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.toolbar_title);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
                appBarLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if(menuItemShare == null)
                            return;
                        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                            // Collapsed
                            menuItemShare.setIcon(R.drawable.ic_share_black_24dp);
                            toolbar.getNavigationIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_share_black_24dp);
                            //toolbar.setPopupTheme(R.style.AppTheme_PopupOverlayDark);
                        } else if (verticalOffset == 0) {
                            // Expanded
                            menuItemShare.setIcon(R.drawable.ic_share_white_24dp);
                            toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_share_white_24dp);
                            //toolbar.setPopupTheme(R.style.AppTheme_PopupOverlayLight);
                        } /*else {
                    // Somewhere in between
                }*/
                    }
                });
            }
        });
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        setTitle(recipe.getName());
    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(this).get(RecipeViewModel.class);
        likeObserver = new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                RecipeActivity.this.recipe = recipe;
                loadLikeImage();
                like.setEnabled(true);
                //viewModel.getRecipe().removeObserver(likeObserver);
            }
        };

        viewModel.getRecipe().observe(this, likeObserver);

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

        menuItemShare = menu.findItem(R.id.action_share);
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
        Log.e(this.getClass().getSimpleName(), recipe.toString());
        String message;
        if(recipe.getMeLike()) {
            Log.e(this.getClass().getSimpleName(), "showing full heart");
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_36dp));
            message = "like";
        }
        else {
            Log.e(this.getClass().getSimpleName(), "showing empty heart");
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_red_36dp));
            message = "unlike";
        }
        if(showLikeMessage)
            Snackbar.make(like, message, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        showLikeMessage = true;
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
        if(MiddleWareForNetwork.checkInternetConnection(this))
            like.setEnabled(false);
        //viewModel.getRecipe().observe(this, likeObserver);
        viewModel.changeLike(getApplicationContext(), recipe);
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

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra(Constants.RECIPE, this.recipe);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        exit();
    }
}
