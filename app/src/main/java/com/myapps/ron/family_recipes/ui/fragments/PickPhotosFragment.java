package com.myapps.ron.family_recipes.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ronginat on 30/10/2018.
 */
public class PickPhotosFragment extends MyFragment {
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

        /*viewModel.getInfoFromLastFetch().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s != null)
                    Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            }
        });*/
    }

    private void setListeners() {
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
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

                        /*Toast.makeText(getActivity(), "You picked " +
                                        (mClipData.getItemCount() > 1 ? mClipData.getItemCount() + " Images" :
                                                mClipData.getItemCount() + "Image"),
                                Toast.LENGTH_LONG).show();*/

                        int pickedImageCount;

                        for (pickedImageCount = 0; pickedImageCount < mClipData.getItemCount(); pickedImageCount++) {
                            Log.e(TAG, mClipData.getItemAt(pickedImageCount).getUri().getPath());

                            ImageView productImageView = new ImageView(getActivity());
                            productImageView.setAdjustViewBounds(true);
                            productImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            productImageView.setLayoutParams(layoutParams);
                            imagesContainer.addView(productImageView);

                            productImageView.setImageURI(mClipData.getItemAt(pickedImageCount).getUri());
                            imagesUris.add(getRealPathFromURI(mClipData.getItemAt(pickedImageCount).getUri()));
                        }
                    } else {
                        Toast.makeText(getActivity(), "You haven't picked any Image",
                                Toast.LENGTH_LONG).show();
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
        int column_index = 0;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }

        return contentUri.getPath();
    }

    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
