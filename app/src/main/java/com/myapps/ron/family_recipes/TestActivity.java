package com.myapps.ron.family_recipes;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        fileName = "chickenfood1.jpg";
        imageView = findViewById(R.id.test_image);
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
                        Log.e(TAG, "download callback");
                        Glide.with(getApplicationContext()).load(uri).into(imageView);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.e(TAG, throwable.getMessage());
                    }
                });
    }

    public void delete(View view) {
        imageView.setImageBitmap(null);
        Uri uri = ExternalStorageHelper.getFileAbsolutePath(this, "thumbnails", fileName);
        if (uri != null) {
            Log.e(TAG, "deleting file, " + new File(uri.getPath()).delete());
        }
    }

}
