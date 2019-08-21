package com.ronginat.family_recipes.ui.fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.logic.storage.ExternalStorageHelper;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;
import com.ronginat.family_recipes.utils.ui.FabExtensionAnimator;
import com.ronginat.family_recipes.viewmodels.PostRecipeViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ronginat on 30/10/2018.
 */
public class PostRecipePickPhotosFragment extends PostRecipeBaseFragment {
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 11;
    private final String TAG = getClass().getSimpleName();

    private static final int CAMERA_REQUEST = 0;
    private static final int GALLERY_REQUEST = 1;

    @BindView(R.id.pick_photos_images_container)
    LinearLayout imagesContainer;
    @BindView(R.id.pick_photos_initial_container)
    ViewGroup initialContainer;

    private PostRecipeViewModel viewModel;

    private LinearLayout.LayoutParams layoutParams;
    private List<String> imagesNamesToUpload = new ArrayList<>();
    private Uri cameraUri;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Snackbar maxImagesSnackbar;


    @Override
    public boolean onBackPressed() {
        activity.previousFragmentDelayed();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        //activity.setTitle(getString(R.string.nav_main_post_recipe) + " 3/3");
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
        layoutParams.setMargins(10, 10, 10, 10);
        initViewModel();
        activity.setFabGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        activity.setFabExtended(true, 1000);
        activity.setFabMayChangeExpandState(false);

        maxImagesSnackbar = Snackbar.make(view, getString(R.string.alert_dialog_upload_photos_max_limit, Constants.MAX_FILES_TO_UPLOAD), Snackbar.LENGTH_INDEFINITE);
        maxImagesSnackbar.setAction(R.string.post_recipe_snacbar_close, view1 -> maxImagesSnackbar.dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.setFabGravity(Gravity.BOTTOM | Gravity.START);
        activity.setFabMayChangeExpandState(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    // region PostRecipeBaseFragment Overrides

    @Override
    protected String getTitle() {
        return getString(R.string.nav_main_post_recipe) + " 3/3";
    }

    @Override
    protected FabExtensionAnimator.GlyphState getFabState() {
        return FabExtensionAnimator.newState(R.string.post_recipe_finish, R.drawable.ic_upload_fab);
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> {
            viewModel.setImagesNames(imagesNamesToUpload);
            activity.postRecipe();
        };
    }

    // endregion

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
    }

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.pick_photos_choose_button)
    void photosClickListener(View view){
        if (imagesNamesToUpload.size() >= Constants.MAX_FILES_TO_UPLOAD) {
            maxImagesSnackbar.show();
            return;
        }
        if (hasStoragePermission())
            showChooseDialog();
    }

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.pick_photos_reset_button)
    void resetClickListener(View view) {
        StorageWrapper.deleteFilesFromLocalPictures(activity, imagesNamesToUpload);
        imagesNamesToUpload.clear();
        cameraUri = null;

        imagesContainer.removeAllViews();
        initialContainer.setVisibility(View.VISIBLE);
    }

    private void showChooseDialog() {
        PickImagesMethodDialog pickImageDialog = activity.showPickImagesDialog();

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
                            break;
                    }
                    pickImageDialog.dismiss();
                }, Throwable::printStackTrace, compositeDisposable::clear)
        );
    }

    private void dispatchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                // Create the File where the photo should go
                File photoFile = StorageWrapper.createImageFile(activity);
                cameraUri = Uri.fromFile(photoFile);
                Uri uri = ExternalStorageHelper.getFileUri(activity, photoFile);
                /*Uri uri = FileProvider.getUriForFile(activity,
                        getString(R.string.appPackage),
                        photoFile);*/
                //Log.e(TAG, "before shooting, file: " + cameraUri.getPath());
                if (uri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            } catch (IOException ex) {
                //Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private boolean hasStoragePermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; Request the permission
            requestPermissions(
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
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                showChooseDialog();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e(TAG, "onActivityResult");
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
                            if (StorageWrapper.deleteFileFromLocalPictures(activity, new File(cameraUri.getPath()).getName()))
                                Toast.makeText(activity, R.string.post_recipe_pick_photos_camera_empty_message, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(activity, R.string.post_recipe_pick_photos_browse_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            loadChosenImages(urisToCopy, cameraFile);
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inflate layouts and convert chosen images (Uris) to local compressed images.
     */
    private void loadChosenImages(List<Uri> list, @Nullable File originalFileFromCamera) {
        if (list.size() > 0) {
            List<Uri> urisToCopy = new ArrayList<>();
            if (list.size() + imagesNamesToUpload.size() > Constants.MAX_FILES_TO_UPLOAD) {
                urisToCopy.addAll(list.subList(0, Constants.MAX_FILES_TO_UPLOAD - imagesNamesToUpload.size()));
                maxImagesSnackbar.show();
            } else
                urisToCopy.addAll(list);
            String[] listPaths = new String[urisToCopy.size()];
            Drawable[] progressBars = new Drawable[urisToCopy.size()];
            int startIndex = imagesContainer.getChildCount();
            compositeDisposable.add(StorageWrapper.copyFiles(activity, urisToCopy)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(subscription -> inflateImages(urisToCopy.size(), progressBars))
                    .subscribe(entry -> {
                        listPaths[entry.getKey()] = entry.getValue();
                        Glide
                                .with(this)
                                .load(StorageWrapper.getLocalFile(activity, entry.getValue()))
                                .placeholder(progressBars[entry.getKey()])
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into((ImageView)imagesContainer.getChildAt(startIndex + entry.getKey()));
                        }, throwable -> CrashLogger.e(TAG, throwable.getMessage()), () -> {
                            if (originalFileFromCamera != null)
                                StorageWrapper.deleteFileFromLocalPictures(activity, originalFileFromCamera.getName());
                            imagesNamesToUpload.addAll(Arrays.asList(listPaths));
                        }
                    ));
        }
    }

    /**
     * Inflate a specific number of {@link ImageView}s. For each, set a loading drawable and save its reference for later.
     * @param size number of {@link ImageView}s to inflate
     * @param drawables empty array that will contain the progressBars of each inflated layout
     */
    private void inflateImages(int size, Drawable[] drawables) {
        //imagesContainer.removeAllViews();
        initialContainer.setVisibility(View.INVISIBLE);
        for (int i = 0; i < size; i++) {
            ImageView imageView = new ImageView(activity);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(layoutParams);
            imageView.setAdjustViewBounds(true);
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(activity);
            circularProgressDrawable.setStrokeWidth(15f);
            circularProgressDrawable.setCenterRadius(80f);
            //circularProgressDrawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
            circularProgressDrawable.start();
            drawables[i] = circularProgressDrawable;
            imagesContainer.addView(imageView);
            imageView.setImageDrawable(circularProgressDrawable);
        }
    }
}