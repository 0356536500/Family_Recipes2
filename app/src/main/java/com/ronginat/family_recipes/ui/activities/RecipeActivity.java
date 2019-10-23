package com.ronginat.family_recipes.ui.activities;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
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

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.logic.Injection;
import com.ronginat.family_recipes.logic.storage.ExternalStorageHelper;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.model.RecipeEntity;
import com.ronginat.family_recipes.recycler.adapters.CommentsAdapter;
import com.ronginat.family_recipes.ui.baseclasses.MyBaseActivity;
import com.ronginat.family_recipes.ui.fragments.PagerDialogFragment;
import com.ronginat.family_recipes.ui.fragments.PickImagesMethodDialog;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;
import com.ronginat.family_recipes.utils.logic.DateUtil;
import com.ronginat.family_recipes.utils.logic.HtmlHelper;
import com.ronginat.family_recipes.utils.logic.SharedPreferencesHandler;
import com.ronginat.family_recipes.utils.ui.MyDividerItemDecoration;
import com.ronginat.family_recipes.viewmodels.RecipeViewModel;
import com.ronginat.searchfilter.animator.FiltersListItemAnimator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RecipeActivity extends MyBaseActivity implements AppBarLayout.OnOffsetChangedListener {

    //private static final int MY_PERMISSIONS_REQUEST_STORAGE = 11;
    private static final int CAMERA_REQUEST = 2;
    private static final int GALLERY_REQUEST = 1;

    private final String TAG = RecipeActivity.class.getSimpleName();
    private AppBarLayout appBarLayout;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView imageViewCollapsingImage;
    private FloatingActionButton like;

    private ContentLoadingProgressBar progressBar;
    private WebView myWebView;
    private String recipeId, lastModified;
    private TextView textViewCommentTitle, textViewDescription;
    private RecipeViewModel viewModel;

    private ViewGroup commentsLayout;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private AppCompatButton postCommentButton;
    private AppCompatEditText postCommentEditText;
    private ProgressBar postCommentProgressBar;

    private boolean showLikeMessage = false;
    private long animationDuration = 700;

    private int textColorPrimary, /*textColorSecondary,*/
            navigationCollapsedColor, navigationExpandedColor;
    private ProgressBar uploadImagesProgressBar;
    private Uri cameraUri;
    private List<String> imagesNamesToUpload = new ArrayList<>();
    private CompositeDisposable compositeDisposable;
    private File createdSharedImageFile;

    private ShareActionProvider mShareActionProvider;

    // region Activity Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        loadColorsFromTheme();
        compositeDisposable = new CompositeDisposable();
        createdSharedImageFile = null;
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            this.recipeId = extras.getString(Constants.RECIPE_ID);
            this.lastModified = extras.getString(Constants.LAST_MODIFIED);
            //String path = extras.getString(Constants.RECIPE_PATH, Constants.DEFAULT_RECIPE_PATH);
            if (recipeId != null) {
                bindUI();
                initUI();
                initViewModel();
                //loadRecipe();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (isWaitingForBroadcast) {
            try {
                unregisterReceiver(mReceiver);
                isWaitingForBroadcast = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (createdSharedImageFile != null &&
                createdSharedImageFile.delete())
            createdSharedImageFile = null;
    }

    // endregion

    private void loadColorsFromTheme() {
        TypedValue primaryValue = new TypedValue();
        //TypedValue secondValue = new TypedValue();
        TypedValue navigationCollapsedValue = new TypedValue();
        TypedValue navigationExpandedValue = new TypedValue();

        getTheme().resolveAttribute(/*android.R.attr.textColorPrimary*/R.attr.collapsedIconsColor, primaryValue, true);
        //getTheme().resolveAttribute(/*android.R.attr.colorPrimaryDark*/R.attr.expandedIconsColor, secondValue, true);
        getTheme().resolveAttribute(R.attr.collapsedIconsColor, navigationCollapsedValue, true);
        getTheme().resolveAttribute(R.attr.expandedIconsColor, navigationExpandedValue, true);

        textColorPrimary = primaryValue.data;
        //textColorSecondary = secondValue.data;
        navigationCollapsedColor = navigationCollapsedValue.data;
        navigationExpandedColor = navigationExpandedValue.data;
    }

    private void bindUI() {
        appBarLayout = findViewById(R.id.activity_recipe_app_bar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        imageViewCollapsingImage = findViewById(R.id.recipe_collapsing_image);
        toolbar = findViewById(R.id.recipe_toolbar);
        textViewDescription = findViewById(R.id.recipe_content_description);
        like = findViewById(R.id.recipe_like);
        progressBar = findViewById(R.id.recipe_content_progressBar);
        myWebView = findViewById(R.id.recipe_content_webView);
        commentsLayout = findViewById(R.id.recipe_content_comments_layout);
        commentsRecyclerView = findViewById(R.id.recycler_comments);
        textViewCommentTitle = findViewById(R.id.textView_comments_title);
        postCommentButton = findViewById(R.id.recipe_content_post_button);
        postCommentEditText = findViewById(R.id.recipe_content_post_editText);
        postCommentProgressBar = findViewById(R.id.recipe_content_post_progressBar);
        uploadImagesProgressBar = findViewById(R.id.recipe_upload_images_progressBar);
    }

    private void initUI() {
        //loadLikeDrawable();
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        initToolbar();
        initRecycler();

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
        //collapsingToolbarLayout.setExpandedTitleColor(textColorSecondary);
        //collapsingToolbarLayout.setContentScrimColor(toolbarCollapsedColor);
        //setTitle(recipe.getName());

        collapsingToolbarLayout.setOnClickListener(view -> {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            if (viewModel.getImages() != null && !viewModel.getImages().isEmpty())
                viewModel.updateAccessToRecipeImages(recipeId);

            // Create and show the dialog.
            DialogFragment newFragment = new PagerDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(PagerDialogFragment.PAGER_TYPE_KEY, PagerDialogFragment.PAGER_TYPE.IMAGES);
            bundle.putStringArrayList(Constants.PAGER_FOOD_IMAGES, viewModel.getImages());
            newFragment.setArguments(bundle);
            newFragment.show(ft, "dialog");
        });

        // progressBar in expanded toolbar background, till image is loaded
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
        circularProgressDrawable.setStrokeWidth(15f);
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
        viewModel.setInitialRecipe(this, this.recipeId, this.lastModified);

        viewModel.getRecipe().observe(this, recipe -> {
            if (recipe != null) {
                adjustFontScale(getResources().getConfiguration(), recipe.getName());
                collapsingToolbarLayout.setTitle(recipe.getName());
                textViewDescription.setText(recipe.getDescription());
                textViewDescription.animate().alpha(1f).setDuration(1000).start();
                //textViewDescription.setAlpha(1f);
                loadRecipe();
            }
        });

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

        viewModel.getRecipeContent().observe(this, content -> {
            if (content != null)
                loadRecipeHtml(content);
            progressBar.hide();
            new Handler().postDelayed(() ->
                    commentsLayout.setVisibility(View.VISIBLE), 1500);
        });
        viewModel.getImagePath().observe(this, path -> {
            /*CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getBaseContext());
            circularProgressDrawable.setStrokeWidth(10f);
            circularProgressDrawable.setCenterRadius(80f);
            circularProgressDrawable.start();*/

            if (path != null) {
                //Log.e(TAG, "found path: " + path);

                Glide.with(getApplicationContext())
                        .asDrawable()
                        .load(path)
                        //.placeholder(circularProgressDrawable)
                        .error(android.R.drawable.stat_notify_error)
                        .into(imageViewCollapsingImage);
            } else {
                imageViewCollapsingImage.setImageDrawable(null);
                /*Glide.with(getApplicationContext())
                        .asDrawable()
                        .load(R.drawable.food_default_medium)
                        //.placeholder(circularProgressDrawable)
                        .error(android.R.drawable.stat_notify_error)
                        .into(imageViewCollapsingImage);*/
            }
        });
        viewModel.getInfo().observe(this, message -> {
            progressBar.hide();
            if (message != null)
                Toast.makeText(RecipeActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void loadLikeDrawable(boolean isUserLiked) {
        //Log.e(TAG, recipe.toString());
        String message;
        if(isUserLiked) {
            //Log.e(TAG, "showing full heart");
            like.setImageResource(R.drawable.ic_favorite_red_36dp);
            //like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_red_36dp));
            message = "liked";
        }
        else {
            //Log.e(this.getClass().getSimpleName(), "showing empty heart");
            like.setImageResource(R.drawable.ic_favorite_border_red_36dp);
            //like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_red_36dp));
            message = "unliked";
        }
        // don't show the snackbar when first loading the activity
        if(showLikeMessage)
            Snackbar.make(like, message, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        showLikeMessage = true;
        like.setEnabled(true);
    }

    private void loadRecipe() {
        viewModel.loadRecipeContent(this);
        viewModel.loadRecipeFoodImage(this);
        viewModel.loadComments(this);
        //loadImage();
        initCommentsRecycler();
        //loadRecipeHtml();
    }

    private void initCommentsRecycler() {
        commentsAdapter = new CommentsAdapter(username -> viewModel.getDisplayedName(this, username));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(mLayoutManager);
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        commentsRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        commentsRecyclerView.setItemAnimator(new FiltersListItemAnimator());
        commentsRecyclerView.setAdapter(commentsAdapter);
    }

    private void loadRecipeHtml(String content) {
        //myWebView.loadData(content, "text/html; charset=utf-8", "UTF-8");

        // load recipe content with css file from assets folder
        myWebView.loadDataWithBaseURL(Constants.ASSET_FILE_BASE_URL,
                HtmlHelper.GET_CSS_LINK() + content, "text/html", "UTF-8", null);
    }

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
        intent.putExtra(Constants.RECIPE_ID, recipeId);
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
            /*if(menuItemShare == null)
                return;*/
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // Collapsed
                //menuItemShare.setIcon(R.drawable.ic_share_collapsed_24dp);
                if (toolbar.getOverflowIcon() != null) {
                    //toolbar.getOverflowIcon().setColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP);
                    toolbar.getOverflowIcon().setColorFilter(new PorterDuffColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP));
                }
                if (toolbar.getNavigationIcon() != null) {
                    //toolbar.getNavigationIcon().setColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP);
                    toolbar.getNavigationIcon().setColorFilter(new PorterDuffColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP));
                }
            } else if (verticalOffset == 0) {
                // Expanded
                //menuItemShare.setIcon(R.drawable.ic_share_expanded_24dp);
                if (toolbar.getOverflowIcon() != null) {
                    //toolbar.getOverflowIcon().setColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP);
                    toolbar.getOverflowIcon().setColorFilter(new PorterDuffColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP));
                }
                if (toolbar.getNavigationIcon() != null) {
                    //toolbar.getNavigationIcon().setColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP);
                    toolbar.getNavigationIcon().setColorFilter(new PorterDuffColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP));
                }
            } /*else {
                        // Somewhere in between
            }*/
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        RecipeEntity recipe = viewModel.getRecipe().getValue();
        if (recipe != null &&
                recipe.getImages() != null &&
                recipe.getImages().size() >= Constants.MAX_FILES_TO_UPLOAD * 2)
            menu.findItem(R.id.action_add_photo).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat
                .getActionProvider(menu.findItem(R.id.action_share));
        updateKeepScreenOn(menu.findItem(R.id.action_keep_screen_on), null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_add_photo:
                //if (hasStoragePermission())
                showChooseDialog(viewModel.getImagePath().getValue() != null);
                return true;
            case R.id.action_share:
                handleShareRecipe();
                // https://developer.android.com/training/sharing/send
                return true;
            case R.id.action_share_as_image:
                handleShareRecipeAsImage();
                return true;
            case R.id.action_keep_screen_on:
                updateKeepScreenOn(item, !item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Read/Write Screen On attribute from {@link R.string#action_key_keep_screen_on} with {@link SharedPreferencesHandler}
     *
     * @param menuItem item to set its value with {@link MenuItem#setChecked(boolean)}
     * @param keepScreenOn what the latest user choice. if null, read the previous from {@link SharedPreferencesHandler}
     */
    private void updateKeepScreenOn(@NonNull MenuItem menuItem, @Nullable Boolean keepScreenOn) {
        if (keepScreenOn == null) {
            boolean preferenceScreenOn = SharedPreferencesHandler.getBoolean(this,
                    getString(R.string.action_key_keep_screen_on));
            menuItem.setChecked(preferenceScreenOn);
            findViewById(android.R.id.content).setKeepScreenOn(preferenceScreenOn);
        } else {
            // write keep screen setting and change menu item value
            SharedPreferencesHandler.writeBoolean(this,
                    getString(R.string.action_key_keep_screen_on), keepScreenOn);
            menuItem.setChecked(keepScreenOn);
            findViewById(android.R.id.content).setKeepScreenOn(keepScreenOn);
        }
    }

    public void adjustFontScale(Configuration configuration, String name) {
        if (collapsingToolbarLayout != null) {
            if (configuration.fontScale >= 1.2f && name.length() > 13) {
                if (name.length() < 20) { // below that means it's a very short title
                    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Title_Medium);
                } else if (name.length() < 30) {
                    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Title_Small);
                } else {
                    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Title_Tiny);
                }
            } else {
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.Title_Large);
            }
        }
    }

    // region Add Photos

    /**
     * Show message only when the recipe doesn't have thumbnail linked to it
     * @param showMessage whether or not to show first image message about landscape shooting
     */
    private void showChooseDialog(boolean showMessage) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        PickImagesMethodDialog pickImageDialog = new PickImagesMethodDialog();
        if (showMessage) {
            Bundle bundle = new Bundle();
            bundle.putCharSequence(Constants.BODY, getText(R.string.pick_image_dialog_message));
            pickImageDialog.setArguments(bundle);
        }
        pickImageDialog.show(ft, "dialog");

        compositeDisposable.add(pickImageDialog.dispatchInfo
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    switch (next) {
                        case CAMERA:
                            dispatchCameraIntent();
                            break;
                        case GALLERY:
                            dispatchGalleryIntent();
                            break;
                        case CANCEL:
                            resetUploadDetails();
                            break;
                    }
                    pickImageDialog.dismiss();
                }, Throwable::printStackTrace)
        );
    }

    private void dispatchCameraIntent() {
        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                // Create the File where the photo should go
                File photoFile = StorageWrapper.createImageFile(this);
                cameraUri = Uri.fromFile(photoFile);
                Uri uri = ExternalStorageHelper.getFileUri(this, photoFile);
                /*cameraUri = FileProvider.getUriForFile(this,
                        getString(R.string.appPackage),
                        photoFile);*/
                //Log.e(TAG, "before shooting, file: " + cameraUri.getPath());
                if (uri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_REQUEST);

        /*Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_REQUEST);*/
    }

    /*private boolean hasStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
            return false;

        } else {
            // Permission has already been granted
            return true;
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                showChooseDialog();
            }
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            List<Uri> urisToCopy = new ArrayList<>();
            File cameraFile = null;
            switch (requestCode) {
                case CAMERA_REQUEST:
                    if (cameraUri != null && cameraUri.getPath() != null) {
                        if (resultCode == RESULT_OK) {
                            if (cameraUri.getPath() != null) {
                                urisToCopy.add(cameraUri);
                                cameraFile = new File(cameraUri.getPath());
                            }
                        } else {
                            if (StorageWrapper.deleteFileFromLocalPictures(this, new File(cameraUri.getPath()).getName()))
                                Toast.makeText(this, R.string.post_recipe_pick_photos_camera_empty_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case GALLERY_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        if (data.getData() != null) { // single image
                            urisToCopy.add(data.getData());
                        }
                        else if (data.getClipData() != null) { // multiple images
                            ClipData mClipData = data.getClipData();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                urisToCopy.add(mClipData.getItemAt(i).getUri());
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.post_recipe_pick_photos_browse_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            loadChosenImages(urisToCopy, cameraFile);
        } catch (Exception e) {
            CrashLogger.logException(e);
            e.printStackTrace();
        }
    }

    /**
     * Inflate layouts and convert chosen images (Uris) to local compressed images.
     */
    private void loadChosenImages(List<Uri> list, @Nullable File originalFileFromCamera) {
        if (list.size() > 0 && imagesNamesToUpload.size() < Constants.MAX_FILES_TO_UPLOAD) {
            List<Uri> urisToCopy = new ArrayList<>();
            if (list.size() + imagesNamesToUpload.size() > Constants.MAX_FILES_TO_UPLOAD) {
                urisToCopy.addAll(list.subList(0, Constants.MAX_FILES_TO_UPLOAD - imagesNamesToUpload.size()));
            } else
                urisToCopy.addAll(list);
            String[] listPaths = new String[urisToCopy.size()];
            compositeDisposable.add(StorageWrapper.copyFiles(this, urisToCopy)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(subscription -> uploadImagesProgressBar.setVisibility(View.VISIBLE))
                    .subscribe(entry ->
                                    listPaths[entry.getKey()] = entry.getValue(),
                            throwable -> CrashLogger.e(TAG, throwable.getMessage()), () -> {
                                if (originalFileFromCamera != null)
                                    StorageWrapper.deleteFileFromLocalPictures(this, originalFileFromCamera.getName());
                                imagesNamesToUpload.addAll(Arrays.asList(listPaths));
                                uploadImagesProgressBar.setVisibility(View.INVISIBLE);
                                pickImagesConfirmationDialog();
                            }
                    ));
        } else {
            if (originalFileFromCamera != null)
                StorageWrapper.deleteFileFromLocalPictures(this, originalFileFromCamera.getName());
            uploadImagesProgressBar.setVisibility(View.INVISIBLE);
            pickImagesConfirmationDialog();
        }
    }

    private void pickImagesConfirmationDialog() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, button) -> {
            if (viewModel.getRecipe().getValue() == null) {
                dialog.dismiss();
                return;
            }
            switch (button){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Constants.ACTION_UPLOAD_IMAGES_SERVICE);
                    isWaitingForBroadcast = true;
                    registerReceiver(mReceiver, intentFilter);
                    viewModel.postImages(this, imagesNamesToUpload);
                    uploadImagesProgressBar.setVisibility(View.VISIBLE);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    resetUploadDetails();
                    break;

                case DialogInterface.BUTTON_NEUTRAL:
                    showChooseDialog(false);
                    break;
            }
            dialog.dismiss();
        };

        DialogInterface.OnCancelListener dialogCancelListener = dialog -> resetUploadDetails();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (imagesNamesToUpload.size() > Constants.MAX_FILES_TO_UPLOAD) {
            builder = builder.setMessage(getString(R.string.alert_dialog_upload_photos_max_limit, Constants.MAX_FILES_TO_UPLOAD));
            imagesNamesToUpload = imagesNamesToUpload.subList(0, Constants.MAX_FILES_TO_UPLOAD);
        }

        //builder/*.setTitle(R.string.action_add_photo)*/
        builder.setTitle(R.string.alert_dialog_upload_photos_confirmation)
                //.setMessage(R.string.alert_dialog_upload_photos_confirmation)
                .setPositiveButton(getString(R.string.alert_dialog_upload_photos_finish, imagesNamesToUpload.size()), dialogClickListener)
                .setNegativeButton(R.string.alert_dialog_upload_photos_cancel, dialogClickListener)
                .setNeutralButton(R.string.alert_dialog_upload_photos_take_more, dialogClickListener)
                .setOnCancelListener(dialogCancelListener)
                .show();
    }

    private boolean isWaitingForBroadcast = false;
    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * uploading is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (Constants.ACTION_UPLOAD_IMAGES_SERVICE.equals(action)) {
                //Log.e(TAG, "Got an update from service");
                if (intent.getExtras() != null) {
                    boolean finishUpload = intent.getBooleanExtra("flag", false);
                    if (finishUpload) {
                        Toast.makeText(RecipeActivity.this, R.string.upload_images_success, Toast.LENGTH_SHORT).show();
                        viewModel.refreshRecipeDelayed(getApplicationContext());
                    } else {
                        Toast.makeText(RecipeActivity.this, R.string.upload_images_failed, Toast.LENGTH_SHORT).show();
                    }
                    uploadImagesProgressBar.setVisibility(View.INVISIBLE);
                    //resetUploadDetails();
                }
                isWaitingForBroadcast = false;
                unregisterReceiver(mReceiver);
            }
        }
    };

    private void resetUploadDetails() {
        if (cameraUri != null && cameraUri.getPath() != null && new File(cameraUri.getPath()).exists())
            imagesNamesToUpload.add(new File(cameraUri.getPath()).getName());
        StorageWrapper.deleteFilesFromLocalPictures(this, imagesNamesToUpload);
        imagesNamesToUpload.clear();
        cameraUri = null;
    }

    // endregion

    // region Share

    private void handleShareRecipe() {
        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
        RecipeEntity entity = viewModel.getRecipe().getValue();
        String lastModifiedDate = entity != null && entity.getLastModifiedDate() != null ? entity.getLastModifiedDate() : "";
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.recipe_share_pre_url) + "\n\n" +
                getString(R.string.share_url,
                        Uri.encode(recipeId),
                        Uri.encode("" + DateUtil.getDateFromStringTimestamp(lastModifiedDate).getTime())));
        /*sendIntent.putExtra(Intent.EXTRA_STREAM, ExternalStorageHelper.getFileUri(this,
                com.myapps.family_recipes.network.Constants.RECIPES_DIR, viewModel.getMaybeRecipeImages().getContent()));*/
        sendIntent.setType(getString(R.string.share_mime_type));
        /*sendIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION);*/
        setShareIntent(sendIntent);
        //startActivity(Intent.createChooser(sendIntent, "Share"));
    }

    /**
     * Reload the WebView with title (the recipe's name) and save it to an image file to be shared.
     * After creating an image file, reload the original content again.
     */
    private void handleShareRecipeAsImage() {
        String content = viewModel.getRecipeContent().getValue();
        RecipeEntity entity = viewModel.getRecipe().getValue();
        if (content != null && entity != null) {
            String contentWithTitle = HtmlHelper.INSERT_TITLE(content, entity.getName());
            myWebView.loadDataWithBaseURL(Constants.ASSET_FILE_BASE_URL,
                    HtmlHelper.GET_CSS_LINK() + contentWithTitle, "text/html", "UTF-8", null);
            myWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    new Handler().postDelayed(() -> {
                        Bitmap bitmap = createBitmapFromWebView();
                        try {
                            createdSharedImageFile = StorageWrapper.createImageFile(getApplicationContext(), entity.getName(), "png");
                            FileOutputStream out = new FileOutputStream(createdSharedImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.close();
                            // reload the webView with the original content
                            myWebView.setWebViewClient(null);
                            loadRecipeHtml(content);
                            shareRecipeAsImage();
                        } catch (IOException e) {
                            CrashLogger.logException(e);
                        }
                    }, 100);
                }
            });
        }
    }

    /**
     * Share image with ACTION_SEND.
     * {@link #createdSharedImageFile} represents the image file to be shared
     */
    private void shareRecipeAsImage() {
        Uri uri = ExternalStorageHelper.getFileUri(this, createdSharedImageFile);
        if (uri != null) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.setDataAndType(uri, getContentResolver().getType(uri));
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sendIntent, getString(R.string.action_share)));
        }
    }

    private Bitmap createBitmapFromWebView() {
        //myWebView.measure(0, 0);
        myWebView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED));
        Bitmap bitmap = Bitmap.createBitmap(myWebView.getMeasuredWidth(), myWebView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        int height = bitmap.getHeight();
        canvas.drawBitmap(bitmap, 0, height, paint);
        myWebView.draw(canvas);
        return bitmap;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        } else
            startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    // endregion
}
