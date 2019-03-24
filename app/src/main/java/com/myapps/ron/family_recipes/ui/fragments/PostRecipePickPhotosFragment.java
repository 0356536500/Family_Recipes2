package com.myapps.ron.family_recipes.ui.fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.myapps.ron.family_recipes.FabExtensionAnimator;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
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
    private List<String> imagesPathsToUpload = new ArrayList<>(), cameraImagesToDeleteAfterUpload = new ArrayList<>();
    private Uri imageUri;

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
        activity.setFabMayExpand(false);

        maxImagesSnackbar = Snackbar.make(view, getString(R.string.alert_dialog_upload_photos_max_limit, Constants.MAX_FILES_TO_UPLOAD), Snackbar.LENGTH_INDEFINITE);
        maxImagesSnackbar.setAction(R.string.post_recipe_snacbar_close, view1 -> maxImagesSnackbar.dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.setFabGravity(Gravity.BOTTOM | Gravity.START);
        activity.setFabMayExpand(true);
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
            viewModel.setImagesUris(imagesPathsToUpload);
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
        if (imagesPathsToUpload.size() > Constants.MAX_FILES_TO_UPLOAD) {
            maxImagesSnackbar.show();
            return;
        }
        if (hasStoragePermission())
            showChooseDialog();
    }

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.pick_photos_reset_button)
    void resetClickListener(View view) {
        StorageWrapper.deleteFilesFromCamera(activity, cameraImagesToDeleteAfterUpload);
        cameraImagesToDeleteAfterUpload.clear();
        imagesPathsToUpload.clear();
        imageUri = null;

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
                if (photoFile != null) {
                    //imageUri = Uri.fromFile(photoFile);
                    imageUri = FileProvider.getUriForFile(activity,
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

                        displayNewImage(imageUri.getPath());

                    } else {
                        File file = new File(imageUri.getPath());
                        if(file.delete())
                            Toast.makeText(activity, R.string.post_recipe_pick_photos_camera_empty_message, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GALLERY_REQUEST:
                    if (resultCode == RESULT_OK && null != data && data.getData() != null) {
                        //single image
                        Log.e(TAG, data.getData().getPath());
                        //Log.e(TAG, StorageWrapper.getRealPathFromURI(this, data.getData()));
                        String path = StorageWrapper.getRealPathFromURI(activity, data.getData());
                        imagesPathsToUpload.add(path);
                        displayNewImage(path);

                    } else if(data != null && null != data.getClipData()) {
                        //multiple images
                        Log.e(TAG, String.valueOf(data.getClipData().getItemCount()));

                        ClipData mClipData = data.getClipData();

                        int pickedImageCounter;

                        if (imagesPathsToUpload.size() + mClipData.getItemCount() > Constants.MAX_FILES_TO_UPLOAD)
                            maxImagesSnackbar.show();

                        for (pickedImageCounter = 0; pickedImageCounter < mClipData.getItemCount()
                                && imagesPathsToUpload.size() < Constants.MAX_FILES_TO_UPLOAD; pickedImageCounter++) {
                            Log.e(TAG, mClipData.getItemAt(pickedImageCounter).getUri().getPath());
                            String path = StorageWrapper.getRealPathFromURI(activity, mClipData.getItemAt(pickedImageCounter).getUri());
                            imagesPathsToUpload.add(path);
                            displayNewImage(path);
                        }
                    } else {
                        Toast.makeText(activity, R.string.post_recipe_pick_photos_browse_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void displayNewImage(String imagePath) {
        initialContainer.setVisibility(View.GONE);
        ImageView imageView = new ImageView(getActivity());

        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setLayoutParams(layoutParams);

        imagesContainer.addView(imageView);
        GlideApp.with(activity)
                .load(imagePath)
                .into(imageView);
    }

    /*private void showWithProperRotation(String filePath, ImageView imageView) {
        // check the rotation of the image and display it properly
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            Log.d("EXIF", "Exif: " + orientation);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }*/
}
