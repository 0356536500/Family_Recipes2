package com.myapps.ron.family_recipes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ronginat on 13/12/2018.
 */
public abstract class MyBaseActivity extends AppCompatActivity {

    /**
     * this method is useful when changing language at runtime
     * @param newBase new base context for the activity
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
