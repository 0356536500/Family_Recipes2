package com.myapps.ron.family_recipes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.activities.SettingsActivity;

/**
 * Created by ronginat on 13/12/2018.
 */
public abstract class MyBaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * this method is useful when changing language at runtime
     * @param newBase new base context for the activity
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(getClass().getSimpleName(), "on create");
        ((MyApplication)getApplication()).applyTheme(this);
        onMyCreate(savedInstanceState);
        //getSharedPreferences(getString(R.string.sharedPreferences), MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    protected abstract void onMyCreate(@Nullable Bundle savedInstanceState);

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSharedPreferences(getString(R.string.sharedPreferences), MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(getClass().getSimpleName(), "on destroy");
        getSharedPreferences(getString(R.string.sharedPreferences), MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Listens to changing in sharedPreferences and react when dark theme value had changed
     * @param sharedPreferences sharedPreferences file (name - R.string.sharedPreferences)
     *                          had changed
     * @param key the key that changed in the file
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null)
            return;
        if (key.equals(getString(R.string.preference_key_dark_mode))) {
            Log.e(getClass().getSimpleName(), "value, " + sharedPreferences.getBoolean(key, false));
            setTheme(sharedPreferences.getBoolean(key, false) ? R.style.AppTheme_Dark : R.style.AppTheme_Dark);
            //toolbar.setPopupTheme(sharedPreferences.getBoolean(key, false) ? R.style.AppTheme_PopupOverlay_Dark : R.style.AppTheme_PopupOverlay_Light);
            recreate();
        }
    }
}
