package com.myapps.ron.family_recipes.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.utils.Constants;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SharedActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final int OPEN_RECIPE_REQUEST = 1;

    /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }*/

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared);

        Intent receivedIntent = getIntent();
        if (Intent.ACTION_VIEW.equals(receivedIntent.getAction()) && receivedIntent.getData() != null) {
            //Log.e(TAG, receivedIntent.getType());
            Uri data = receivedIntent.getData();
            Log.e(TAG, data.toString());
            if (getString(R.string.scheme).equals(data.getScheme()) &&
                    getString(R.string.host).equals(data.getHost())) {
                //Log.e(TAG, data.getLastPathSegment());
                Log.e(TAG, data.getPathSegments().toString());
                String recipeId = data.getLastPathSegment();
                if (!"".equals(recipeId)) {
                    Intent splashIntent = new Intent(SharedActivity.this, SplashActivity.class);
                    splashIntent.putExtra(Constants.RECIPE_ID, recipeId);
                    splashIntent.putExtra(Constants.SPLASH_ACTIVITY_CODE, Constants.SPLASH_ACTIVITY_CODE_RECIPE);
                    startActivityForResult(splashIntent, OPEN_RECIPE_REQUEST);
                }
            }
            else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPEN_RECIPE_REQUEST:
                if (resultCode == RESULT_OK) {
                    /*Intent intent = new Intent(SharedActivity.this, MainActivity.class);
                    startActivity(intent);*/
                    setResult(RESULT_OK);
                    finish();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
        }
    }
}
