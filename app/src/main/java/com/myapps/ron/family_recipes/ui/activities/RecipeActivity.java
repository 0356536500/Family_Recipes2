package com.myapps.ron.family_recipes.ui.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.myapps.ron.family_recipes.utils.ui.MyDividerItemDecoration;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.recycler.adapters.CommentsAdapter;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.background.services.PostRecipeToServerService;
import com.myapps.ron.family_recipes.ui.fragments.PagerDialogFragment;
import com.myapps.ron.family_recipes.ui.fragments.PickImagesMethodDialog;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.viewmodels.RecipeViewModel;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RecipeActivity extends MyBaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 11;
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
    private String recipeId;
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
    private ProgressBar uploadImagesProgressBar;
    private Uri imageUri;
    private List<String> imagesPathsToUpload = new ArrayList<>(), cameraImagesToDeleteAfterUpload = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        loadColorsFromTheme();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            recipeId = extras.getString(Constants.RECIPE_ID);
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
        uploadImagesProgressBar = findViewById(R.id.recipe_upload_images_progressBar);
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
        //setTitle(recipe.getName());

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
            //TODO: fix this
            bundle.putSerializable(PagerDialogFragment.PAGER_TYPE_KEY, PagerDialogFragment.PAGER_TYPE.IMAGES);
            bundle.putStringArrayList(Constants.PAGER_FOOD_IMAGES, viewModel.getFoodFiles());
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
        viewModel.setInitialRecipe(recipeId);

        viewModel.getRecipe().observe(this, recipe -> {
            if (recipe != null) {
                setTitle(recipe.getName());
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
                        .load(path)
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

    private void loadLikeDrawable(boolean isUserLiked) {
        //Log.e(TAG, recipe.toString());
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
        commentsAdapter = new CommentsAdapter();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(mLayoutManager);
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        commentsRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        commentsRecyclerView.setItemAnimator(new FiltersListItemAnimator());
        commentsRecyclerView.setAdapter(commentsAdapter);
    }

    private void loadRecipeHtml(String path) {
        /*File file = new File(path);
        Log.e(TAG, file.getAbsolutePath());*/
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
                    toolbar.getOverflowIcon().setColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP);
                }
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setColorFilter(navigationCollapsedColor, PorterDuff.Mode.SRC_ATOP);
                }
            } else if (verticalOffset == 0) {
                // Expanded
                //menuItemShare.setIcon(R.drawable.ic_share_expanded_24dp);
                if (toolbar.getOverflowIcon() != null) {
                    toolbar.getOverflowIcon().setColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP);
                }
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setColorFilter(navigationExpandedColor, PorterDuff.Mode.SRC_ATOP);
                }
            } /*else {
                        // Somewhere in between
            }*/
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (viewModel.getRecipe().getValue() != null &&
                viewModel.getRecipe().getValue().getFoodFiles() != null &&
                viewModel.getRecipe().getValue().getFoodFiles().size() > Constants.MAX_FILES_TO_UPLOAD * 2)
            menu.findItem(R.id.action_add_photo).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe, menu);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat
                .getActionProvider(menu.findItem(R.id.action_share));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_add_photo:
                if (hasStoragePermission())
                    showChooseDialog();
                return true;
            case R.id.action_share:
                handleShareRecipe();
                //TODO: https://developer.android.com/training/sharing/send
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // region Add Photos
    private void showChooseDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        PickImagesMethodDialog pickImageDialog = new PickImagesMethodDialog();
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
                }, Throwable::printStackTrace, compositeDisposable::clear)
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
                if (photoFile != null) {
                    //imageUri = Uri.fromFile(photoFile);
                    imageUri = FileProvider.getUriForFile(this,
                            getString(R.string.appPackage),
                            photoFile);
                    //Log.e(TAG, "before shooting, file: " + imageUri.getPath());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
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

    private boolean hasStoragePermission() {
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    showChooseDialog();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        try {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    Log.e(TAG, "camera result, " + imageUri.getPath());
                    if (resultCode == RESULT_OK) {
                        File file = new File(imageUri.getPath());
                        Log.e(TAG, "camera absolute path, " + file.getAbsolutePath());
                        //Log.e(TAG, "file bytes = " + file.length());

                        imagesPathsToUpload.add(file.getAbsolutePath());
                        cameraImagesToDeleteAfterUpload.add(file.getName());
                        pickImagesConfirmationDialog();

                    } else {
                        File file = new File(imageUri.getPath());
                        if(file.delete())
                            Toast.makeText(this, R.string.post_recipe_pick_photos_camera_empty_message, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GALLERY_REQUEST:
                    if (resultCode == RESULT_OK && null != data && data.getData() != null) {
                        //single image
                        Log.e(TAG, data.getData().getPath());
                        //Log.e(TAG, StorageWrapper.getRealPathFromURI(this, data.getData()));
                        imagesPathsToUpload.add(StorageWrapper.getRealPathFromURI(this, data.getData()));

                        pickImagesConfirmationDialog();

                    } else if(data != null && null != data.getClipData()) {
                        //multiple images
                        Log.e(TAG, String.valueOf(data.getClipData().getItemCount()));

                        ClipData mClipData = data.getClipData();

                        int pickedImageCounter;

                        for (pickedImageCounter = 0; pickedImageCounter < mClipData.getItemCount(); pickedImageCounter++) {
                            Log.e(TAG, mClipData.getItemAt(pickedImageCounter).getUri().getPath());

                            imagesPathsToUpload.add(StorageWrapper.getRealPathFromURI(this, mClipData.getItemAt(pickedImageCounter).getUri()));
                        }
                        pickImagesConfirmationDialog();
                    } else {
                        Toast.makeText(this, R.string.post_recipe_pick_photos_browse_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
                    registerReceiver(mReceiver, intentFilter);
                    PostRecipeToServerService.startActionPostImages(this,
                            viewModel.getRecipe().getValue().getId(), viewModel.getRecipe().getValue().getLastModifiedDate(), imagesPathsToUpload);
                    uploadImagesProgressBar.setVisibility(View.VISIBLE);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    resetUploadDetails();
                    break;

                case DialogInterface.BUTTON_NEUTRAL:
                    showChooseDialog();
                    break;
            }
            dialog.dismiss();
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (imagesPathsToUpload.size() > Constants.MAX_FILES_TO_UPLOAD) {
            builder = builder.setMessage(getString(R.string.alert_dialog_upload_photos_max_limit, Constants.MAX_FILES_TO_UPLOAD));
            imagesPathsToUpload = imagesPathsToUpload.subList(0, Constants.MAX_FILES_TO_UPLOAD);
        }
        //builder/*.setTitle(R.string.action_add_photo)*/
        builder.setTitle(R.string.alert_dialog_upload_photos_confirmation)
                //.setMessage(R.string.alert_dialog_upload_photos_confirmation)
                .setPositiveButton(getString(R.string.alert_dialog_upload_photos_finish, imagesPathsToUpload.size()), dialogClickListener)
                .setNegativeButton(R.string.alert_dialog_upload_photos_cancel, dialogClickListener)
                .setNeutralButton(R.string.alert_dialog_upload_photos_take_more, dialogClickListener)
                .show();
    }

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
            switch(action) {
                // When network state changes
                case Constants.ACTION_UPLOAD_IMAGES_SERVICE:
                    Log.d(TAG, "Got an update from service");
                    //unregisterReceiver(mReceiver);
                    //registerReceiver(mReceiver, regularFilter);
                    if (intent.getExtras() != null) {
                        boolean finishUpload = intent.getBooleanExtra("flag", false);
                        if (finishUpload) {
                            Toast.makeText(RecipeActivity.this, "photos uploaded successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RecipeActivity.this, "failed to upload the photos", Toast.LENGTH_SHORT).show();
                        }
                        uploadImagesProgressBar.setVisibility(View.INVISIBLE);
                        resetUploadDetails();
                    }
                    unregisterReceiver(mReceiver);
                    break;
            }
        }
    };

    private void resetUploadDetails() {
        StorageWrapper.deleteFilesFromCamera(this, cameraImagesToDeleteAfterUpload);
        cameraImagesToDeleteAfterUpload.clear();
        imagesPathsToUpload.clear();
        imageUri = null;
    }

    // endregion

    // region Share

    private void handleShareRecipe() {
        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.recipe_share_pre_url) + "\n\n"
                + getString(R.string.share_url, recipeId));
        /*sendIntent.putExtra(Intent.EXTRA_STREAM, ExternalStorageHelper.getFileUri(this,
                com.myapps.ron.family_recipes.network.Constants.RECIPES_DIR, viewModel.getRecipe().getRecipeFile()));*/
        sendIntent.setType(getString(R.string.share_mime_type));
        /*sendIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION);*/
        setShareIntent(sendIntent);
        //startActivity(Intent.createChooser(sendIntent, "Share"));
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        } else
            startActivity(Intent.createChooser(shareIntent, "Share"));
    }


    // endregion
}
