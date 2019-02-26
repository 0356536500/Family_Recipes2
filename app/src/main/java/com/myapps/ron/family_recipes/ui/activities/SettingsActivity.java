package com.myapps.ron.family_recipes.ui.activities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.utils.LocaleHelper;
import com.myapps.ron.family_recipes.utils.MyBaseActivity;
import com.myapps.ron.family_recipes.viewmodels.SettingsViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ronginat on 12/12/2018.
 */
public class SettingsActivity extends MyBaseActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{

    private int fragmentCounter = 0;
    public static PublishSubject<Integer> publishSubject = PublishSubject.create();

    @Override
    protected void onMyCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);

        //new Handler().postDelayed(this::startPublish, 1000);

        setupActionBar();
        ViewModelProviders.of(this).get(SettingsViewModel.class)
                .getInfo().observe(this, info ->
                Toast.makeText(this, info, Toast.LENGTH_SHORT).show());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new GeneralPreferenceFragment())
                .addToBackStack(null)
                .commit();

        fragmentCounter++;

        //Log.e(getClass().getSimpleName(), "call worker");
        //WorkManager.getInstance().enqueue(PostRecipeScheduledWorker.createPostRecipesWorker());
    }

    private void startPublish() {
        try {
            for (int i = 0; i < 4; i++) {
                Thread.sleep(1000);
                publishSubject.onNext(i);
            }
            publishSubject.onComplete();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Log.e(getClass().getSimpleName(), "return home");
            if (fragmentCounter > 1)
                getSupportFragmentManager().popBackStack();
            else
                finish();
            fragmentCounter--;
            //startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fragmentCounter > 1)
            super.onBackPressed();
        else
            finish();
        fragmentCounter--;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Log.e(getClass().getSimpleName(), "change event, " + key + ", value = " + sharedPreferences.getBoolean(key, false));
        if (key == null)
            return;

        if (key.equals(getString(R.string.preference_key_dark_theme))) {
            recreate();
        }
        if (key.equals(getString(R.string.preference_key_language))) {
            Log.e(getClass().getSimpleName(), "new lang, " + sharedPreferences.getString(key, "he"));
            LocaleHelper.setLocale(SettingsActivity.this, sharedPreferences.getString(key, "he"));
            recreate();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        //Log.e(getClass().getSimpleName(), "onPreferenceStartFragment, caller: " + caller.getClass().getSimpleName() + ", pref: " + pref.getTitle());
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment(),
                args);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_container, fragment)
                .addToBackStack(null)
                .commit();
        fragmentCounter++;
        return true;
    }

    /*
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    /*private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                SharedPreferencesHandler
                        .getSharedPreferences(context)
                        .getString(preference.getKey(), ""));
    }
*/

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /*PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(getString(R.string.sharedPreferences));
            preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);*/

            //bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_dark_theme)));
            //bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_language)));
            //bindPreferenceSummaryToValue(findPreference("example_text"));
            //addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(getString(R.string.sharedPreferences));
            preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);*/
            setPreferencesFromResource(R.xml.pref_general, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (getActivity() != null)
                getActivity().setTitle(getPreferenceScreen().getTitle());
        }

        /*@Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Log.e(getClass().getSimpleName(), "return home");
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }*/
    }


    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SwitchPreferenceCompat switchNewRecipe = findPreference("notifications_new_message_vibrate");
            switchNewRecipe.setOnPreferenceChangeListener((preference, newValue) -> {
                new Handler().postDelayed(() -> Log.e(getClass().getSimpleName(), "delayed operation, value = " + newValue), 1500);
                return true;
            });

            if (getActivity() != null) {
                SettingsViewModel viewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
                viewModel.changeKeyToValue.observe(this, entry -> {
                    TwoStatePreference statePreference = findPreference(entry.getKey());
                    statePreference.setChecked(entry.getValue());
                });
            }
            //addPreferencesFromResource(R.xml.pref_notification);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getActivity() != null) {
                RecyclerView recyclerView = getListView();
                DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), RecyclerView.VERTICAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            //Log.e(getClass().getSimpleName(), "onCreatePreferences, " + s);
            setPreferencesFromResource(R.xml.pref_notification, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (getActivity() != null)
                getActivity().setTitle(getPreferenceScreen().getTitle());
        }

    }

    /*
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    /*private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    };*/

}
