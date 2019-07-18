package com.ronginat.family_recipes.ui.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.ronginat.family_recipes.BuildConfig;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.MiddleWareForNetwork;
import com.ronginat.family_recipes.layout.cognito.AppHelper;
import com.ronginat.family_recipes.logic.Injection;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.ui.baseclasses.MyBaseActivity;
import com.ronginat.family_recipes.viewmodels.SettingsViewModel;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

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
        ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(SettingsViewModel.class)
                .getInfo().observe(this, info ->
                Toast.makeText(this, info, Toast.LENGTH_SHORT).show());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new GeneralPreferenceFragment())
                .addToBackStack(null)
                .commit();

        fragmentCounter++;
    }

    /**
     * Set up the {android.app.ActionBar}, if the API is available.
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
        private FragmentActivity activity;
        private SettingsViewModel viewModel;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = getActivity();
            //bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_dark_theme)));
            //bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_language)));
            //bindPreferenceSummaryToValue(findPreference("example_text"));
            //addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            viewModel = ViewModelProviders.of(activity).get(SettingsViewModel.class);
            Preference bugPreference = findPreference(getString(R.string.preference_key_report_bug));
            if (bugPreference != null) {
                bugPreference.setOnPreferenceClickListener(preference -> sendEmail());
            }

            Preference updatePreference = findPreference(getString(R.string.preference_key_check_for_updates));
            if (updatePreference != null) {
                updatePreference.setOnPreferenceClickListener(preference -> checkForUpdates());
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

        // region App Updates

        private static final int REQUEST_WRITE_PERMISSION = 186;
        private File appUpdateFile;
        private Uri uri;

        private boolean checkForUpdates() {
            if (!MiddleWareForNetwork.checkInternetConnection(activity)) {
                Toast.makeText(activity, R.string.no_internet_message, Toast.LENGTH_SHORT).show();
                return false;
            }

            viewModel.getDataToDownloadUpdate(activity)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<Map<String, String>>() {
                        @Override
                        public void onSuccess(Map<String, String> map) {
                            appUpdateFile = StorageWrapper.getFileToDownloadUpdateInto(activity,
                                    map.get(com.ronginat.family_recipes.layout.Constants.RESPONSE_KEY_APP_NAME));
                            uri = Uri.parse(map.get(com.ronginat.family_recipes.layout.Constants.RESPONSE_KEY_APP_URL));
                            new AlertDialog.Builder(activity)
                                    .setCancelable(true)
                                    .setTitle(R.string.main_activity_update_available_title)
                                    .setMessage(R.string.main_activity_update_available_message)
                                    .setPositiveButton(android.R.string.yes, (dialog, which) ->
                                            updateApp())
                                    .create()
                                    .show();

                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Toast.makeText(activity, t.getMessage(), Toast.LENGTH_LONG).show();
                            dispose();
                        }
                    });
            return true;
        }

        private void updateApp() {
            if (canReadWriteExternalAndInstallPackages()) {
                viewModel.downloadNewAppVersion(activity, onComplete, uri, appUpdateFile);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(activity)
                            .setCancelable(true)
                            .setTitle(R.string.main_activity_permission_to_install_updates_title)
                            .setMessage(R.string.main_activity_permission_to_install_updates_message)
                            .setPositiveButton(android.R.string.yes, (dialog, which) ->
                                    requestPermission())
                            .create()
                            .show();
                } else
                    requestPermission();
            }
        }

        BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Finished", Toast.LENGTH_LONG).show();
                viewModel.installApp(activity, appUpdateFile);
                activity.unregisterReceiver(this);
            }
        };

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                viewModel.downloadNewAppVersion(activity, onComplete, uri, appUpdateFile);
            else
                Toast.makeText(activity, "Permission denied", Toast.LENGTH_LONG).show();
        }

        private void requestPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_PERMISSION);
        }

        private boolean canReadWriteExternalAndInstallPackages() {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED;
        }

        // endregion

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

                Preference sysNotificationPreference = findPreference(getString(R.string.preference_key_system_notification));
                if (sysNotificationPreference != null) {
                    sysNotificationPreference.setOnPreferenceClickListener(preference -> openSystemNotificationSettings());
                }
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

        private boolean openSystemNotificationSettings() {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            } else {
                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(new Uri.Builder().scheme("package").opaquePart(activity.getPackageName()).build());
                //intent.setData(Uri.parse("package:" + activity.getPackageName()));
            }
            startActivity(intent);
            return true;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (getActivity() != null)
                getActivity().setTitle(getPreferenceScreen().getTitle());
        }

    }


    /**
     * This fragment shows account preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountPreferenceFragment extends PreferenceFragmentCompat {
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

                EditTextPreference namePreference = findPreference(getString(R.string.preference_key_preferred_name));
                viewModel.setPreferredNamePreference(activity, namePreference);
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
            setPreferencesFromResource(R.xml.pref_account, rootKey);
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
