package com.myapps.ron.family_recipes.ui.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.CommentsAdapter;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.ui.fragments.PagerDialogFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.utils.MyBaseActivity;
import com.myapps.ron.family_recipes.viewmodels.RecipeViewModel;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;

import java.io.File;

public class RecipeActivity extends MyBaseActivity implements AppBarLayout.OnOffsetChangedListener {

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

    private ViewGroup commentsLayout;
    private RecyclerView recyclerView;
    private CommentsAdapter mAdapter;
    private AppCompatButton postCommentButton;
    private AppCompatEditText postCommentEditText;
    private ProgressBar postCommentProgressBar;

    private boolean showLikeMessage = false;
    private long animationDuration = 700;

    private int textColorPrimary, textColorSecondary,
            iconCollapsedColor, iconExpandedColor, toolbarCollapsedColor;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        initViewModel();
        loadColorsFromTheme();

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

    private void loadColorsFromTheme() {
        TypedValue primaryValue = new TypedValue();
        TypedValue secondValue = new TypedValue();
        TypedValue iconCollapsedValue = new TypedValue();
        TypedValue iconExpandedValue = new TypedValue();
        TypedValue toolbarCollapsedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, primaryValue, true);
        getTheme().resolveAttribute(android.R.attr.textColorSecondary, secondValue, true);
        getTheme().resolveAttribute(R.attr.textColorMain, iconCollapsedValue, true);
        getTheme().resolveAttribute(R.attr.textColorSecond, iconExpandedValue, true);
        getTheme().resolveAttribute(R.attr.toolbarBackgroundPrimary, toolbarCollapsedValue, true);

        textColorPrimary = primaryValue.data;
        textColorSecondary = secondValue.data;
        iconCollapsedColor = iconCollapsedValue.data;
        iconExpandedColor = iconExpandedValue.data;
        toolbarCollapsedColor = toolbarCollapsedValue.data;
    }

    private void bindUI() {
        appBarLayout = findViewById(R.id.activity_recipe_app_bar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        imageViewCollapsingImage = findViewById(R.id.recipe_collapsing_image);
        toolbar = findViewById(R.id.recipe_toolbar);
        like = findViewById(R.id.recipe_like);
        progressBar = findViewById(R.id.recipe_content_progressBar);
        myWebView = findViewById(R.id.recipe_content_webView);
        commentsLayout = findViewById(R.id.recipe_content_comments_layout);
        recyclerView = findViewById(R.id.recycler_comments);
        textViewCommentTitle = findViewById(R.id.textView_comments_title);
        postCommentButton = findViewById(R.id.recipe_content_post_button);
        postCommentEditText = findViewById(R.id.recipe_content_post_editText);
        postCommentProgressBar = findViewById(R.id.recipe_content_post_progressBar);
    }

    private void initUI() {
        loadLikeDrawable();
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        initToolbar();
        initRecycler();

        /*WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);*/
        postCommentButton.setOnClickListener(postCommentListener);
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

        appBarLayout.addOnOffsetChangedListener(this);

        //set the collapsed text color according to current theme

        collapsingToolbarLayout.setCollapsedTitleTextColor(textColorPrimary);
        collapsingToolbarLayout.setExpandedTitleColor(textColorSecondary);
        collapsingToolbarLayout.setContentScrimColor(toolbarCollapsedColor);
        /*collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);*/
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
                if (recipe != null) {
                    boolean changedLike = false, postedComment = false;
                    // user changed like value
                    if (RecipeActivity.this.recipe.isUserLiked() != recipe.isUserLiked()) {
                        changedLike = true;
                    }
                    // user posted a comment
                    if (!RecipeActivity.this.recipe.getStringComments().equals(recipe.getStringComments())) {
                        postedComment = true;
                    }

                    RecipeActivity.this.recipe = recipe;
                    if (changedLike) {
                        loadLikeDrawable();
                        like.setEnabled(true);
                    }
                    if (postedComment) {
                        loadComments();
                        postCommentEditText.setText("");
                        postCommentButton.setEnabled(true);
                        postCommentButton.animate().alpha(1f).setDuration(animationDuration).start();
                        postCommentProgressBar.animate().alpha(0f).setDuration(animationDuration).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                postCommentProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }).start();
                    }
                }
            }
        };

        viewModel.getRecipe().observe(this, recipeObserver);
        viewModel.getRecipePath().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    loadRecipeHtml(s);
                progressBar.hide();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        commentsLayout.setVisibility(View.VISIBLE);
                    }
                }, 1500);
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
            public void onChanged(@Nullable String message) {
                if (message != null)
                    Toast.makeText(RecipeActivity.this, message, Toast.LENGTH_LONG).show();
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
        if(recipe.isUserLiked()) {
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
    }

    private void loadComments() {
        if (this.recipe.getComments() != null) {
            textViewCommentTitle.setText(getString(R.string.comments, recipe.getComments().size()));

            if (mAdapter == null) {
                mAdapter = new CommentsAdapter(this.recipe.getComments());
                recyclerView.setAdapter(mAdapter);

            } else {
                mAdapter.setComments(this.recipe.getComments());
            }
        }
    }

    private void loadRecipeHtml(String path) {
        if(path != null)
            myWebView.loadUrl(path);
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

    private View.OnClickListener postCommentListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Toast.makeText(getApplicationContext(), postCommentEditText.getText(), Toast.LENGTH_SHORT).show();
            if (postCommentEditText.getText() != null) {
                viewModel.postComment(getApplicationContext(), recipe, postCommentEditText.getText().toString());
                postCommentButton.setEnabled(false);
                postCommentProgressBar.setVisibility(View.VISIBLE);
                postCommentButton.animate().alpha(0f).setDuration(animationDuration).start();
                postCommentProgressBar.animate().alpha(1f).setDuration(animationDuration).start();
            }
        }
    };

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
                        toolbar.getNavigationIcon().setColorFilter(iconCollapsedColor, PorterDuff.Mode.SRC_ATOP);
                    //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_share_black_24dp);
                    //toolbar.setPopupTheme(R.style.AppTheme_PopupOverlayDark);
                } else if (verticalOffset == 0) {
                    // Expanded
                    menuItemShare.setIcon(R.drawable.ic_share_white_24dp);
                    if (toolbar.getNavigationIcon() != null)
                        toolbar.getNavigationIcon().setColorFilter(iconExpandedColor, PorterDuff.Mode.SRC_ATOP);
                    //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_share_white_24dp);
                    //toolbar.setPopupTheme(R.style.AppTheme_PopupOverlayLight);
                } /*else {
                            // Somewhere in between
                }*/
            }
        });
    }
}
