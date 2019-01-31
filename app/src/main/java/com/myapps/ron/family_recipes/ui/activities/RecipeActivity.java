package com.myapps.ron.family_recipes.ui.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.myapps.ron.family_recipes.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.CommentsAdapter;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.ui.fragments.PagerDialogFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.utils.MyBaseActivity;
import com.myapps.ron.family_recipes.viewmodels.RecipeViewModel;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;

import java.io.File;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

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
    private RecipeEntity recipe;
    private TextView textViewCommentTitle;
    private RecipeViewModel viewModel;

    private ViewGroup commentsLayout;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private AppCompatButton postCommentButton;
    private AppCompatEditText postCommentEditText;
    private ProgressBar postCommentProgressBar;

    private boolean showLikeMessage = false;
    private long animationDuration = 700;

    private int textColorPrimary, textColorSecondary,
            navigationCollapsedColor, navigationExpandedColor;

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
        TypedValue navigationCollapsedValue = new TypedValue();
        TypedValue navigationExpandedValue = new TypedValue();

        getTheme().resolveAttribute(android.R.attr.textColorPrimary, primaryValue, true);
        getTheme().resolveAttribute(android.R.attr.colorPrimaryDark, secondValue, true);
        getTheme().resolveAttribute(R.attr.collapsedIconsColor, navigationCollapsedValue, true);
        getTheme().resolveAttribute(R.attr.expandedIconsColor, navigationExpandedValue, true);

        textColorPrimary = primaryValue.data;
        textColorSecondary = secondValue.data;
        navigationCollapsedColor = navigationCollapsedValue.data;
        navigationExpandedColor = navigationExpandedValue.data;
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
        commentsRecyclerView = findViewById(R.id.recycler_comments);
        textViewCommentTitle = findViewById(R.id.textView_comments_title);
        postCommentButton = findViewById(R.id.recipe_content_post_button);
        postCommentEditText = findViewById(R.id.recipe_content_post_editText);
        postCommentProgressBar = findViewById(R.id.recipe_content_post_progressBar);
    }

    private void initUI() {
        //loadLikeDrawable();
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

        toolbar.setNavigationOnClickListener(v -> exit());

        appBarLayout.addOnOffsetChangedListener(this);

        //set the collapsed text color according to current theme

        collapsingToolbarLayout.setCollapsedTitleTextColor(textColorPrimary);
        collapsingToolbarLayout.setExpandedTitleColor(textColorSecondary);
        //collapsingToolbarLayout.setContentScrimColor(toolbarCollapsedColor);
        setTitle(recipe.getName());

        collapsingToolbarLayout.setOnClickListener(view -> {
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
        });

        // progressBar in expanded toolbar background, till image is loaded
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(80f);
        circularProgressDrawable.start();
        imageViewCollapsingImage.setImageDrawable(circularProgressDrawable);
    }

    private void initRecycler() {
        //commentsAdapter = new CommentsAdapter(this.recipe.getComments());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        commentsRecyclerView.setLayoutManager(mLayoutManager);
        commentsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        commentsRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 4));
        //commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setItemAnimator(new FiltersListItemAnimator());
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(RecipeViewModel.class);
        //viewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);

        viewModel.getComments().observe(this, comments -> {
            postCommentEditText.setText("");
            postCommentButton.setEnabled(true);
            postCommentButton.animate().alpha(1f).setDuration(animationDuration).start();
            postCommentProgressBar.animate().alpha(0f).setDuration(animationDuration).withEndAction(() ->
                    postCommentProgressBar.setVisibility(View.INVISIBLE)).start();
            if (comments != null) {
                textViewCommentTitle.setText(getString(R.string.comments, comments.size()));
                commentsAdapter.setComments(comments);
            }
        });

        viewModel.isUserLiked().observe(this, this::loadLikeDrawable);

        viewModel.getRecipePath().observe(this, path -> {
            if (path != null)
                loadRecipeHtml(path);
            progressBar.hide();
            new Handler().postDelayed(() ->
                    commentsLayout.setVisibility(View.VISIBLE), 1500);
        });
        viewModel.getImagePath().observe(this, path -> {
            if (path != null) {
                Log.e(TAG, "found path: " + path);
                /*CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
                circularProgressDrawable.setStrokeWidth(5f);
                circularProgressDrawable.setCenterRadius(35f);
                circularProgressDrawable.start();*/

                GlideApp.with(getApplicationContext())
                        .asDrawable()
                        .load(Uri.fromFile(new File(path)))
                        //.placeholder(circularProgressDrawable)
                        .error(android.R.drawable.stat_notify_error)
                        .into(imageViewCollapsingImage);
            } else {
                //TODO: load default picture
                GlideApp.with(getApplicationContext())
                        .asDrawable()
                        .load(RecipeEntity.image)
                        .error(android.R.drawable.stat_notify_error)
                        .into(imageViewCollapsingImage);
            }
        });
        viewModel.getInfo().observe(this, messageId -> {
            if (messageId != null)
                Toast.makeText(RecipeActivity.this, messageId, Toast.LENGTH_LONG).show();
        });
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

    private void loadLikeDrawable(boolean isUserLiked) {
        Log.e(TAG, recipe.toString());
        String message;
        if(isUserLiked) {
            //Log.e(TAG, "showing full heart");
            like.setImageResource(R.drawable.ic_favorite_red_36dp);
            //like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_36dp));
            message = "like";
        }
        else {
            //Log.e(this.getClass().getSimpleName(), "showing empty heart");
            like.setImageResource(R.drawable.ic_favorite_border_red_36dp);
            //like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_red_36dp));
            message = "unlike";
        }
        if(showLikeMessage)
            Snackbar.make(like, message, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        showLikeMessage = true;
    }

    private void loadRecipe() {
        viewModel.setInitialRecipe(recipe);

        viewModel.loadRecipeContent(this);
        viewModel.loadRecipeFoodImage(this);
        viewModel.loadComments(this);
        //loadImage();
        initCommentsRecycler();
        //loadRecipeHtml();
    }

    private void initCommentsRecycler() {
        commentsAdapter = new CommentsAdapter();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(mLayoutManager);
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        commentsRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        commentsRecyclerView.setItemAnimator(new FiltersListItemAnimator());
        commentsRecyclerView.setAdapter(commentsAdapter);
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
            //Toast.makeText(getApplicationContext(), postCommentEditText.getMessage(), Toast.LENGTH_SHORT).show();
            if (postCommentEditText.getText() != null && commentValidationCheck(postCommentEditText.getText().toString())) {
                viewModel.postComment(getApplicationContext(), postCommentEditText.getText().toString());

                postCommentButton.setEnabled(false);
                postCommentProgressBar.setVisibility(View.VISIBLE);
                postCommentButton.animate().alpha(0f).setDuration(animationDuration).start();
                postCommentProgressBar.animate().alpha(1f).setDuration(animationDuration).start();

            } else
                Toast.makeText(getApplicationContext(), R.string.post_comment_error, Toast.LENGTH_SHORT).show();
        }
    };

    private boolean commentValidationCheck(String text) {
        return text.length() > 0 && !text.startsWith(" ");
    }

    public void doLike(View view) {
        like.setEnabled(false);
        viewModel.changeLike(this);
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra(Constants.RECIPE, viewModel.getRecipe());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        appBarLayout.post(() -> {
            if(menuItemShare == null)
                return;
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // Collapsed
                menuItemShare.setIcon(R.drawable.ic_share_collapsed_24dp);
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP);
                }
            } else if (verticalOffset == 0) {
                // Expanded
                menuItemShare.setIcon(R.drawable.ic_share_expanded_24dp);
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP);
                }
            } /*else {
                        // Somewhere in between
            }*/
        });
    }
}
