package com.myapps.ron.family_recipes;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.myapps.ron.family_recipes.logic.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.logic.storage.StorageWrapper;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();
    ImageView imageView;
    String fileName;
    ColorMatrixColorFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        fileName = "chickenfood1.jpg";
        imageView = findViewById(R.id.test_image);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        filter = new ColorMatrixColorFilter(colorMatrix);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void download(View view) {
        StorageWrapper.getThumbFile(this, fileName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e(TAG, "download callback, " + uri.getPath());
                        //imageView.setImageURI(uri);
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imageView);

                        imageView.setColorFilter(filter);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.e(TAG, throwable.getMessage());
                    }
                });
    }

    public void delete(View view) {
        imageView.setImageResource(R.mipmap.ic_logo_foreground);
        imageView.setColorFilter(filter);
        Uri uri = ExternalStorageHelper.getFileAbsolutePath(this, "thumbnails", fileName);
        if (uri != null) {
            Log.e(TAG, "deleting file, " + new File(uri.getPath()).delete());
        }
    }

}
