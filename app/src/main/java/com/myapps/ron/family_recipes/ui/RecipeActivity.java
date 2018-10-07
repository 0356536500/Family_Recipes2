package com.myapps.ron.family_recipes.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.utils.Constants;

public class RecipeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton like;
    //private TextView textView;
    private WebView myWebView;
    private Recipe recipe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        bindUI();

        setSupportActionBar(toolbar);

        /*like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(like.getTag() == R.drawable.ic_favorite_border_red_36dp)
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            recipe = extras.getParcelable(Constants.RECIPE);
            //String path = extras.getString(Constants.RECIPE_PATH, Constants.DEFAULT_RECIPE_PATH);
            if (recipe != null)
                loadRecipe();
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
    }

    private void loadLikeImage() {
        if(recipe.getMeLike())
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_36dp));
        else
            like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_red_36dp));
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
        String message;
        // do like
        recipe.setMeLike(!recipe.getMeLike());

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
                    .setAction("Action", null).show();
    }
}
