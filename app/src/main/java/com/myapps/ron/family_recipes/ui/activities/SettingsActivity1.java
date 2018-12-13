package com.myapps.ron.family_recipes.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;

/**
 * Created by ronginat on 12/12/2018.
 */
public class SettingsActivity1 extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Toolbar toolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication)getApplication()).applyTheme(this);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setElevation(8f);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.settings_container, new GeneralPreferenceFragment1())
                //.addToBackStack(null)
                .commit();

        getSharedPreferences(getString(R.string.sharedPreferences), MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSharedPreferences(getString(R.string.sharedPreferences), MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e(getClass().getSimpleName(), "change event, " + key);
        if (key.equals("dark_mode")) {
            Log.e(getClass().getSimpleName(), "value, " + sharedPreferences.getBoolean(key, false));
            setTheme(sharedPreferences.getBoolean(key, false) ? R.style.AppTheme_Dark : R.style.AppTheme_Dark);
            //toolbar.setPopupTheme(sharedPreferences.getBoolean(key, false) ? R.style.AppTheme_PopupOverlay_Dark : R.style.AppTheme_PopupOverlay_Light);
            SettingsActivity1.this.recreate();
        }
    }


    public static class GeneralPreferenceFragment1 extends PreferenceFragmentCompat {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(getString(R.string.sharedPreferences));
            preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.pref_general);
            getPreferenceManager().getPreferenceScreen().setIconSpaceReserved(false);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            Log.e(getClass().getSimpleName(), "onCreatePreferences, " + s);

        }
    }
}
