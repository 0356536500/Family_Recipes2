package com.myapps.ron.family_recipes.ui.fragments;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.ui.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ronginat on 30/10/2018.
 */
public class PickPhotosFragment extends MyFragment {
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 11;
    private final String TAG = getClass().getSimpleName();

    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_REQUEST = 1;

    private LinearLayout imagesContainer;
    private AppCompatButton browseButton, takeButton, resetButton;
    private PostRecipeViewModel viewModel;
    private PostRecipeActivity activity;

    private LinearLayout.LayoutParams layoutParams;
    private List<String> imagesUris = new ArrayList<>();
    private Uri imageUri;

    private final int BROWSE_CODE = 1;
    private final int CAMERA_CODE = -1;

    private int storageRelatedButtonClicked = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (PostRecipeActivity)getActivity();
    }

    @Override
    public boolean onBackPressed() {
        activity.previousFragment();
        activity.nextButton.setText(R.string.post_recipe_next);
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        imagesContainer = view.findViewById(R.id.pick_photos_images_container);
        browseButton = view.findViewById(R.id.pick_photos_choose_button);
        takeButton = view.findViewById(R.id.pick_photos_take_photo_button);
        resetButton = view.findViewById(R.id.pick_photos_reset_button);

        activity.setTitle(getString(R.string.nav_main_post_recipe) + " 3/3");
        layoutParams = new LinearLayout.LayoutParams(750, 750);
        layoutParams.setMargins(10, 10, 10, 10);
        initViewModel();
        setListeners();
    }

    private void initViewModel() {
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
    }

    private void setListeners() {
        storageRelatedButtonClicked = 0;
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storageRelatedButtonClicked = BROWSE_CODE;
                checkStoragePermission();
                //openGallery();
            }
        });

        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storageRelatedButtonClicked = CAMERA_CODE;
                checkStoragePermission();
                //openCamera();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(viewModel.getImagesUris() != null) {
                    viewModel.setImagesUris(new ArrayList<Uri>());
                }*/
                imagesUris.clear();
                imagesContainer.removeAllViews();
            }
        });

        activity.nextButton.setText(R.string.post_recipe_finish);
        activity.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.setImagesUris(imagesUris);
                activity.postRecipe();
            }
        });
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {*/
                // No explanation needed; request the permission
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);

            //}
        } else {
            Log.e(TAG, "granted");
            switch (storageRelatedButtonClicked) {
                case BROWSE_CODE:
                    openGallery();
                    break;
                case CAMERA_CODE:
                    openCamera();
                    break;
            }
            storageRelatedButtonClicked = 0;
            // Permission has already been granted
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

                    switch (storageRelatedButtonClicked) {
                        case BROWSE_CODE:
                            openGallery();
                            break;
                        case CAMERA_CODE:
                            openCamera();
                            break;
                    }
                    storageRelatedButtonClicked = 0;

                } /*else {

                }*/
                break;
            }
        }
    }

    private void openCamera() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            File image = StorageWrapper.createImageFile(activity);
            Log.e(TAG, image.getAbsolutePath());
            imageUri = Uri.fromFile(image);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void openGallery() {
        /*Intent filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.setType("image/*");
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "images/*");
        //pickIntent.setType("image/*");


        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {filesIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);*/
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        startActivityForResult(intent, GALLERY_REQUEST);
    }

   /* private void showChoosingFile() {
        *//*Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGES_REQUEST);
        *//*
        Intent filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.setType("image/*");
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "images/*");
        //pickIntent.setType("image/*");


        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {filesIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);

        *//*Intent chooser = Intent.createChooser(galleryIntent, "Some text here");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
        startActivityForResult(chooser, requestCode);*//*
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // When an Image is picked
            switch (requestCode) {
                case GALLERY_REQUEST:
                    if (resultCode == RESULT_OK && null != data && data.getData() != null) {
                        //single image
                        Log.e(TAG, data.getData().getPath());
                        Log.e(TAG, getRealPathFromURI(data.getData()));
                        //imagesContainer.removeAllViews();

                        ImageView productImageView = new ImageView(getActivity());

                        productImageView.setAdjustViewBounds(true);
                        productImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        productImageView.setLayoutParams(layoutParams);

                        imagesContainer.addView(productImageView);

                        productImageView.setImageURI(data.getData());

                        imagesUris.add(getRealPathFromURI(data.getData()));
                    } else if(data != null && null != data.getClipData()) {
                        //multiple images
                        Log.e(TAG, String.valueOf(data.getClipData().getItemCount()));

                        ClipData mClipData = data.getClipData();

                        int pickedImageCounter;

                        for (pickedImageCounter = 0; pickedImageCounter < mClipData.getItemCount(); pickedImageCounter++) {
                            Log.e(TAG, mClipData.getItemAt(pickedImageCounter).getUri().getPath());

                            ImageView productImageView = new ImageView(getActivity());
                            productImageView.setAdjustViewBounds(true);
                            productImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            productImageView.setLayoutParams(layoutParams);
                            imagesContainer.addView(productImageView);

                            productImageView.setImageURI(mClipData.getItemAt(pickedImageCounter).getUri());
                            imagesUris.add(getRealPathFromURI(mClipData.getItemAt(pickedImageCounter).getUri()));
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.post_recipe_pick_photos_browse_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case CAMERA_REQUEST:
                    if (resultCode == RESULT_OK) {
                        String selectedImage = imageUri.getPath();
                        final ImageView imageView = new ImageView(getActivity());

                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setLayoutParams(layoutParams);

                        imagesContainer.addView(imageView);

                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(activity.getBaseContext());
                        circularProgressDrawable.setStrokeWidth(5f);
                        circularProgressDrawable.setCenterRadius(35f);
                        circularProgressDrawable.start();

                        GlideApp.with(activity.getApplicationContext())
                                .asDrawable()
                                .load(selectedImage)
                                .placeholder(circularProgressDrawable)
                                .into(new CustomViewTarget<ImageView, Drawable>(imageView) {
                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        imageView.setImageDrawable(errorDrawable);
                                    }

                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        imageView.setImageDrawable(resource);
                                    }

                                    @Override
                                    protected void onResourceCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                        //imageView.setImageURI(selectedImage);
                        imagesUris.add(selectedImage);
                    } else {
                        File file = new File(imageUri.getPath());
                        if(file.delete())
                            Toast.makeText(activity, R.string.post_recipe_pick_photos_camera_empty_message, Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error: Something went wrong " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(activity, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }

        return contentUri.getPath();
    }
}
