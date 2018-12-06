package com.myapps.ron.family_recipes.ui.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.CommentsAdapter;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.ui.fragments.PagerDialogFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.viewmodels.RecipeViewModel;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;

import java.io.File;

public class RecipeActivity extends AppCompatActivity {

    private final String TAG = RecipeActivity.class.getSimpleName();
    private AppBarLayout appBarLayout;
    private MenuItem menuItemShare;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView imageViewCollapsingImage;
    private FloatingActionButton like;
    //private TextView textView;
    ContentLoadingProgressBar progressBar;
    private WebView myWebView;
    private Recipe recipe;
    private TextView textViewCommentTitle;
    private RecipeViewModel viewModel;
    private Observer<Recipe> recipeObserver;//, commentObserver;

    private RecyclerView recyclerView;
    private CommentsAdapter mAdapter;

    private boolean showLikeMessage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

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

    //@SuppressLint("SetJavaScriptEnabled")
    private void bindUI() {
        appBarLayout = findViewById(R.id.activity_recipe_app_bar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        imageViewCollapsingImage = findViewById(R.id.recipe_collapsing_image);
        toolbar = findViewById(R.id.recipe_toolbar);
        like = findViewById(R.id.recipe_like);
        progressBar = findViewById(R.id.recipe_content_progressBar);
        myWebView = findViewById(R.id.recipe_content_webView);
        recyclerView = findViewById(R.id.recycler_comments);
        textViewCommentTitle = findViewById(R.id.textView_comments_title);
        //WebSettings webSettings = myWebView.getSettings();
        //webSettings.setJavaScriptEnabled(true);
    }

    private void initUI() {
        loadLikeDrawable();
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        initToolbar();
        initRecycler();
    }

    private void initToolbar() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_black);
        }

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
                            if (toolbar.getNavigationIcon() != null)
                                toolbar.getNavigationIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_share_black_24dp);
                            //toolbar.setPopupTheme(R.style.AppTheme_PopupOverlayDark);
                        } else if (verticalOffset == 0) {
                            // Expanded
                            menuItemShare.setIcon(R.drawable.ic_share_white_24dp);
                            if (toolbar.getNavigationIcon() != null)
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

        collapsingToolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = new PagerDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.RECIPE, recipe);
                newFragment.setArguments(bundle);
                newFragment.show(ft, "dialog");
            }
        });
    }

    private void initRecycler() {
        //mAdapter = new CommentsAdapter(this.recipe.getComments());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 4));
        //recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new FiltersListItemAnimator());
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        recipeObserver = new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                RecipeActivity.this.recipe = recipe;
                loadLikeDrawable();
                like.setEnabled(true);
                //mAdapter.setComments(RecipeActivity.this.recipe.getComments());
                //viewModel.getRecipe().removeObserver(recipeObserver);
            }
        };

        viewModel.getRecipe().observe(this, recipeObserver);
        viewModel.getRecipePath().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    loadRecipeHtml(s);
                progressBar.hide();
            }
        });
        viewModel.getImagePath().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String path) {
                if (path != null) {
                    Log.e(TAG, "found path: " + path);
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
                    circularProgressDrawable.setStrokeWidth(5f);
                    circularProgressDrawable.setCenterRadius(35f);
                    circularProgressDrawable.start();

                    GlideApp.with(getApplicationContext())
                            .asDrawable()
                            .load(Uri.fromFile(new File(path)))
                            .placeholder(circularProgressDrawable)
                            .error(android.R.drawable.stat_notify_error)
                            .into(imageViewCollapsingImage);
                }
            }
        });
        viewModel.getInfo().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    Toast.makeText(RecipeActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

        /*commentObserver = new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                RecipeActivity.this.recipe = recipe;
                //TOD_O handle comments
                viewModel.getRecipe().removeObserver(commentObserver);
            }
        };*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        menuItemShare = menu.findItem(R.id.action_share);

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
        return super.onOptionsItemSelected(item);
    }

    private void loadLikeDrawable() {
        Log.e(TAG, recipe.toString());
        String message;
        if(recipe.getMeLike()) {
            Log.e(TAG, "showing full heart");
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

    private void loadRecipe() {
        viewModel.loadRecipeContent(this, recipe);
        viewModel.loadRecipeFoodImage(this, recipe);
        //loadImage();
        loadComments();
        //loadRecipeHtml();
        //loadComments();
    }

    private void loadComments() {
        if (this.recipe.getComments() != null) {
            textViewCommentTitle.setText(getString(R.string.comments, recipe.getComments().size()));

            if (mAdapter == null) {
                mAdapter = new CommentsAdapter(this.recipe.getComments());
                recyclerView.setAdapter(mAdapter);

                textViewCommentTitle.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);

            } else {
                mAdapter.setComments(this.recipe.getComments());
            }
        }
    }

    private void loadRecipeHtml(String path) {
        if(path != null)
            myWebView.loadUrl(path);


        //textView.setMovementMethod(LinkMovementMethod.getInstance());
        /*File htmlFile = StorageWrapper.getInstance(this).createHtmlFile(this, "demoFile.html", buildDemoHtml());
        if(htmlFile != null)
            myWebView.loadUrl(htmlFile.getPath());*/

        /*File file = new File(getFilesDir().getPath(), "demoFile.html");
        if (file.exists()) {
            Log.e("Recipe", "file exists");
            myWebView.loadUrl("file:///" + file.getAbsolutePath());
        }*/

        //textView.setText(buildDemoHtml());
        //myWebView.loadData(buildDemoHtml().toString(), null, "utf-8");
        //myWebView.loadUrl(recipe.image);
    }

    /*private void loadImage() {
        if(recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > 0) {
            StorageWrapper.getFoodFile(this, recipe.getFoodFiles().get(0), new MyCallback<String>() {
                @Override
                public void onFinished(String path) {
                    if(path != null) {
                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
                        circularProgressDrawable.setStrokeWidth(5f);
                        circularProgressDrawable.setCenterRadius(35f);
                        circularProgressDrawable.start();

                        GlideApp.with(getApplicationContext())
                                .asDrawable()
                                .load(Uri.fromFile(new File(path)))
                                .placeholder(circularProgressDrawable)
                                .error(android.R.drawable.stat_notify_error)
                                .into(imageViewCollapsingImage);
                    }
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
                    }
                    else
                        collapsingToolbarLayout.setBackground(getDrawable(android.R.drawable.stat_notify_error));
                }
            });
        }
    }*/


    public void doLike(View view) {
        //recipe.setMeLike(!recipe.getMeLike());
        //if(MiddleWareForNetwork.checkInternetConnection(this))
        like.setEnabled(false);
        //viewModel.getRecipe().observe(this, recipeObserver);
        viewModel.changeLike(getApplicationContext(), recipe);
        /*String message;
        // do like
        loadRecipeContent();
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
