package com.ronginat.family_recipes;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.ronginat.family_recipes.logic.storage.StorageHelper;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();
    ViewGroup imagesLayout;
    LinearLayout.LayoutParams layoutParams;
    CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        imagesLayout = findViewById(R.id.test_images_layout);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(10, 10, 10, 20);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    public void sendContentIntent(View view) {
        // 1. on Upload click call ACTION_GET_CONTENT intent
        Intent intent = new Intent(Intent.ACTION_PICK);
        // 2. pick image only
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // 3. start activity
        startActivityForResult(intent, 0);

        // define onActivityResult to do something with picked image
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            List<Uri> urisToCopy = new ArrayList<>();
            if (data.getData() != null) { // single image
                CrashLogger.e(TAG, data.getData().toString());
                CrashLogger.e(TAG, data.getData().getPath());
                CrashLogger.e(TAG, MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(data.getData())));
                //inflateImages(1);
                urisToCopy.add(data.getData());
            }
            else if (data.getClipData() != null) { // multiple images
                ClipData mClipData = data.getClipData();
                CrashLogger.e(TAG, mClipData.toString());
                //inflateImages(data.getClipData().getItemCount());
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    urisToCopy.add(mClipData.getItemAt(i).getUri());
                }
                //imageView.setImageURI(data.getData());
            }
            if (urisToCopy.size() > 0) {
                String[] listPaths = new String[urisToCopy.size()];
                Drawable[] progressBars = new Drawable[urisToCopy.size()];
                //inflateImages(urisToCopy.size(), progressBars);
                compositeDisposable.add(StorageHelper.copyFiles(this, urisToCopy)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(subscription -> inflateImages(urisToCopy.size(), progressBars))
                        .subscribe(entry -> {
                            listPaths[entry.getKey()] = entry.getValue();
                            Glide
                                    .with(this)
                                    .load(entry.getValue())
                                    .placeholder(progressBars[entry.getKey()])
                                    //.placeholder(((ImageView)imagesLayout.getChildAt(entry.getKey())).getDrawable())
                                    .into((ImageView)imagesLayout.getChildAt(entry.getKey()));
                        }, throwable -> CrashLogger.e(TAG, throwable.getMessage()), () ->
                                Toast.makeText(this, String.format("loaded %s images", listPaths.length), Toast.LENGTH_SHORT).show()));
            }
        }
    }

    private void inflateImages(int size, Drawable[] drawables) {
        imagesLayout.removeAllViews();

        for (int i = 0; i < size; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            //imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(layoutParams);
            imageView.setAdjustViewBounds(true);
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
            circularProgressDrawable.setStrokeWidth(15f);
            circularProgressDrawable.setCenterRadius(80f);
            //circularProgressDrawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
            circularProgressDrawable.start();
            drawables[i] = circularProgressDrawable;
            imagesLayout.addView(imageView);
            imageView.setImageDrawable(circularProgressDrawable);
        }
    }
}
