package com.ronginat.family_recipes.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

public class SharedActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final int OPEN_RECIPE_REQUEST = 1;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared);

        Intent receivedIntent = getIntent();
        if (Intent.ACTION_VIEW.equals(receivedIntent.getAction()) && receivedIntent.getData() != null) {
            //Log.e(TAG, receivedIntent.getType());
            Uri data = receivedIntent.getData();
            CrashLogger.e(TAG, data.toString());
            if (getString(R.string.scheme).equals(data.getScheme()) &&
                    getString(R.string.host).equals(data.getHost())) {
                //Log.e(TAG, data.getLastPathSegment());
                CrashLogger.e(TAG, data.getPathSegments().toString());
                String recipeId = data.getLastPathSegment();
                String recipeDate = data.getQueryParameter(Constants.LAST_MODIFIED);
                if (recipeId != null && !"".equals(recipeId) && recipeDate != null && !"".equals(recipeDate)) {
                    Intent splashIntent = new Intent(SharedActivity.this, SplashActivity.class);
                    splashIntent.putExtra(Constants.RECIPE_ID, new String(Base64.decode(recipeId.getBytes(), Base64.URL_SAFE)));
                    splashIntent.putExtra(Constants.LAST_MODIFIED, new String(Base64.decode(recipeId.getBytes(), Base64.URL_SAFE)));
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
        CrashLogger.e(TAG, "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_RECIPE_REQUEST) {
            setResult(resultCode == RESULT_OK ? RESULT_OK : RESULT_CANCELED);
            finish();
        }
    }
}
