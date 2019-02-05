package com.myapps.ron.family_recipes;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.myapps.ron.family_recipes.dal.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    ImageView imageView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = findViewById(R.id.test_imageView);

        /*Uri uri = ExternalStorageHelper.getFileUri(this, "chickenfood2.jpg", Constants.FOOD_DIR);
        if (uri != null) {
            GlideApp.with(this)
                    .load(uri)
                    .into(imageView);
        }*/
        StorageWrapper.getThumbFile(this, "chickenfood1.jpg", result -> {
            if (result != null) {
                Log.e(TAG, result.getPath());
                GlideApp.with(this)
                        .load(result)
                        .into(imageView);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*File photoFile = null;
        try {
            photoFile = createImageFile();
            Log.e(TAG, photoFile.getAbsolutePath());
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(TAG, ex.getMessage());
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.myapps.ron.family_recipes",
                    photoFile);
            Log.e(TAG, photoURI.getPath());
        }*/
        //StorageWrapper.getThumbFile(this, "chickenfood1.jpg", result -> Log.e(TAG, result));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Subscribe to the emissions of the recipe name from the view model.
        // Update the recipe name text view, at every onNext emission.
        // In case of error, log the exception.
        /*mDisposable.add(mViewModel.getRecipeName("0")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recipeName -> textView.setMessage(recipeName),
                        throwable -> Log.e(TAG, "Unable to update recipe name", throwable)));*/
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("food-pictures");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
