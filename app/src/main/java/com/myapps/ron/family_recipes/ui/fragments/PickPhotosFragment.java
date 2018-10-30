package com.myapps.ron.family_recipes.ui.fragments;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ronginat on 30/10/2018.
 */
public class PickPhotosFragment extends MyFragment {
    private final String TAG = getClass().getSimpleName();
    private final int CHOOSING_IMAGES_REQUEST = 3;
    private GridLayout imagesContainer;
    private AppCompatButton chooseButton;
    private PostRecipeViewModel viewModel;
    private PostRecipeActivity activity;

    private CircularProgressDrawable circularProgressDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (PostRecipeActivity)getActivity();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        imagesContainer = view.findViewById(R.id.pick_photos_images_container);
        chooseButton = view.findViewById(R.id.pick_photos_choose_button);

        activity.setTitle("create 3/3");
        setListeners();

        circularProgressDrawable = new CircularProgressDrawable(activity);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(35f);
        circularProgressDrawable.start();

    }

    private void setListeners() {
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoosingFile();
            }
        });
    }

    private void showChoosingFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // When an Image is picked
            if (requestCode == CHOOSING_IMAGES_REQUEST && resultCode == RESULT_OK
                    && null != data && null != data.getClipData()) {

                Log.e(TAG, String.valueOf(data.getClipData().getItemCount()));

                ClipData mClipData = data.getClipData();

                Toast.makeText(getActivity(), "You picked " +
                                (mClipData.getItemCount() > 1 ? mClipData.getItemCount() + " Images" :
                                        mClipData.getItemCount() + "Image"),
                        Toast.LENGTH_LONG).show();

                imagesContainer.removeAllViews();

                int pickedImageCount;

                for (pickedImageCount = 0; pickedImageCount < mClipData.getItemCount();
                     pickedImageCount++) {

                    Log.e(TAG, mClipData.getItemAt(pickedImageCount).getUri().getPath());

                    ImageView productImageView = new ImageView(getActivity());

                    productImageView.setAdjustViewBounds(true);

                    productImageView.setScaleType(ImageView.ScaleType.FIT_XY);

                    productImageView.setLayoutParams(new GridLayout.LayoutParams(
                            GridLayout.spec(1, GridLayout.CENTER),
                            GridLayout.spec(1, GridLayout.CENTER)));

                    imagesContainer.addView(productImageView);

                    productImageView.setImageURI(mClipData.getItemAt(pickedImageCount).getUri());

                    /*GlideApp.with(activity)
                            .load(mClipData.getItemAt(pickedImageCount).getUri())
                            .fitCenter()
                            .placeholder(circularProgressDrawable)
                            .error(android.R.drawable.stat_notify_error)
                            .into(productImageView);*/
                }
            } else {
                Toast.makeText(getActivity(), "You haven't picked any Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error: Something went wrong " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }
}
