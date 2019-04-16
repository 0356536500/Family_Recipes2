package com.myapps.ron.family_recipes.ui.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.myapps.ron.family_recipes.BuildConfig;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.viewmodels.SettingsViewModel;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ronginat on 12/12/2018.
 */
public class SettingsActivity extends MyBaseActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{

    private int fragmentCounter = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        Log.e(getClass().getSimpleName(), getResources().getConfiguration().locale.toLanguageTag());
        //Log.e(getClass().getSimpleName(), getResources().getConfiguration().getLocales().get(0).toLanguageTag());
        Log.e(getClass().getSimpleName(), Locale.getDefault().toLanguageTag());
    }*/

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
            if (fragmentCounter > 1)
                getSupportFragmentManager().popBackStack();
            else
                finish();
            fragmentCounter--;
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
            updateLocale(new Locale(sharedPreferences.getString(key, "he")));
            /*LocaleHelper.setLocale(SettingsActivity.this, sharedPreferences.getString(key, "he"));
            recreate();*/
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        //Log.e(getClass().getSimpleName(), "onPreferenceStartFragment, caller: " + caller.getClass().getSimpleName() + ", pref: " + pref.getTitle());
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
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

            //bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_dark_theme)));
            //bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_language)));
            //bindPreferenceSummaryToValue(findPreference("example_text"));
            //addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Preference bugPreference = findPreference(getString(R.string.preference_key_report_bug));
            if (bugPreference != null) {
                bugPreference.setOnPreferenceClickListener(preference -> sendEmail());
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            /*PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(getString(R.string.sharedPreferences));
            preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);*/
            setPreferencesFromResource(R.xml.pref_general, rootKey);
        }

        @SuppressWarnings("StringBufferReplaceableByString")
        private boolean sendEmail() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(getString(R.string.settings_report_bug_mail_list_problem_here))
                    .append("\n\n----------------------------\n")
                    .append("Application and System information:\n")
                    .append("Device Model: ")
                    .append(Build.BRAND.toUpperCase().concat(" "))
                    .append(Build.BRAND.concat(" "))
                    .append(Build.MODEL.concat("\n"))
                    .append("Android OS Version: ")
                    .append(Build.VERSION.RELEASE.concat("\n"))
                    .append(getString(R.string.app_name).concat(" App Version: "))
                    .append(BuildConfig.VERSION_NAME.concat("\n"));

            String emailSubject = AppHelper.getCurrUser() +
                    " -- I've got an issue with " +
                    getString(R.string.app_name) + " App";


            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            //emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{getString(R.string.support_email)});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            emailIntent.putExtra(Intent.EXTRA_TEXT   , stringBuilder.toString());
            if (getActivity() != null && emailIntent.resolveActivity(getActivity().getPackageManager()) == null) {
                return false;
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Send using"));
                return true;
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            if (getActivity() != null)
                getActivity().setTitle(getPreferenceScreen().getTitle());
        }
    }


    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragmentCompat {
        private FragmentActivity activity;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = getActivity();

            //addPreferencesFromResource(R.xml.pref_notification);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (activity != null) {
                SettingsViewModel viewModel = ViewModelProviders.of(activity).get(SettingsViewModel.class);
                viewModel.setSwitchListener(findPreference(getString(R.string.preference_key_notification_new_recipe)));
                viewModel.setSwitchListener(findPreference(getString(R.string.preference_key_notification_comment)));
                viewModel.setSwitchListener(findPreference(getString(R.string.preference_key_notification_likes)));

                viewModel.getBindListenerAgain().observe(this, key -> viewModel.setSwitchListener(findPreference(key)));
                viewModel.changeKeyToValue.observe(this, entry -> {
                    TwoStatePreference statePreference = findPreference(entry.getKey());
                    if (statePreference != null) {
                        statePreference.setChecked(entry.getValue());
                    }
                });
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (activity != null) {
                RecyclerView recyclerView = getListView();
                DividerItemDecoration itemDecoration = new DividerItemDecoration(activity, RecyclerView.VERTICAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
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
