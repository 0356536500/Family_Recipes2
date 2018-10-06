package com.myapps.ron.family_recipes.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.utils.Constants;

public class RecipeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton like;
    //private TextView textView;
    private WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        bindUI();

        setSupportActionBar(toolbar);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String path = extras.getString(Constants.RECIPE_PATH, Constants.DEFAULT_RECIPE_PATH);
            myWebView.loadUrl(path);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void bindUI() {
        toolbar = findViewById(R.id.toolbar);
        like = findViewById(R.id.recipe_like);
        //textView = findViewById(R.id.recipe_textView);
        myWebView = findViewById(R.id.recipe_content_view);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }
}
