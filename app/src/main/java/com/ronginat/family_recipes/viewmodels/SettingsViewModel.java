package com.ronginat.family_recipes.viewmodels;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.ronginat.family_recipes.MyApplication;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.layout.MiddleWareForNetwork;
import com.ronginat.family_recipes.layout.cognito.AppHelper;
import com.ronginat.family_recipes.layout.firebase.db.FirestoreHelper;
import com.ronginat.family_recipes.logic.repository.AppRepository;
import com.ronginat.family_recipes.utils.logic.SharedPreferencesHandler;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by ronginat on 17/02/2019.
 */
public class SettingsViewModel extends ViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    //private final String TAG = getClass().getSimpleName();
    private MutableLiveData<String> info = new MutableLiveData<>();
    // rebind preference after finishing its process
    private MutableLiveData<String> bindListenerAgain = new MutableLiveData<>();
    public MutableLiveData<Map.Entry<String, Boolean>> changeKeyToValue = new MutableLiveData<>();
    private Context context;

    private CompositeDisposable compositeDisposable;
    private final AppRepository appRepository;

    private AtomicReference<String> skipKey = new AtomicReference<>("");

    public SettingsViewModel(AppRepository appRepository) {
        this.context = MyApplication.getContext();
        this.appRepository = appRepository;
        this.compositeDisposable = new CompositeDisposable();
        SharedPreferencesHandler.getSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    public void setSwitchListener(@Nullable TwoStatePreference statePreference) {
        // register switch listener
        // called from every two state preference in SettingsActivity
        if (statePreference != null)
            statePreference.setOnPreferenceChangeListener(mainPreferenceListener);
    }

    private Preference.OnPreferenceChangeListener mainPreferenceListener = (preference, newValue) -> {
        if (MiddleWareForNetwork.checkInternetConnection(context)) {
            if (AppHelper.getAccessToken() != null) {
                // everything is valid
                // disable listener for the meantime
                preference.setOnPreferenceChangeListener((preference1, newValue1) -> false);
                return true;
            }
            // invalid access token
            setInfo(context.getString(R.string.invalid_access_token));
            return false;
        } else {
            // no internet
            setInfo(context.getString(R.string.no_internet_message));
            return false;
        }
    };

    public void setPreferredNamePreference(Context context, @Nullable EditTextPreference namePreference) {
        if (namePreference != null) {
            namePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                //Log.e(getClass().getSimpleName(), "changing attribute, " + preference.getKey());
                AppHelper.modifyAttribute(context, preference.getKey(), String.valueOf(newValue));
                return false;
            });
        }

        compositeDisposable.add(AppHelper.updateAttributeSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success && namePreference != null) {
                        String nameFromPreferences = SharedPreferencesHandler.getString(context, context.getString(R.string.preference_key_preferred_name));
                        if (nameFromPreferences != null) {
                            namePreference.setText(nameFromPreferences);
                            SharedPreferencesHandler.writeString(context, Constants.FIRESTORE_SAVE_NAME, nameFromPreferences);
                            updateDisplayedNameInFirebase(context, nameFromPreferences);
                        }
                    } else
                        setInfo(context.getString(R.string.change_displayed_name_error));
                }));
    }

    private void updateDisplayedNameInFirebase(Context context, String name) {
        compositeDisposable.add(FirestoreHelper.getInstance().setDisplayedName(context, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> SharedPreferencesHandler.removeString(context, Constants.FIRESTORE_SAVE_NAME),
                        throwable -> setInfo(throwable.getMessage()))
        );
    }

    //private final Preference.OnPreferenceChangeListener blockingPreferenceListener = (preference1, newValue1) -> false;

    private void changeNotificationSetting(String key, boolean changeToValue) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            // call to server
            Map<String, String> queries = new HashMap<>();
            queries.put(key, changeToValue ? Constants.SUBSCRIPTION_SUBSCRIBE : Constants.SUBSCRIPTION_UNSUBSCRIBE);
            compositeDisposable.add(appRepository.manageSubscriptions(context, queries, new HashMap<>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        // 200 ok will return true
                        if (success) {
                            skipKey.set(key);
                            changeKeyToValue.setValue(new AbstractMap.SimpleEntry<>(key, !changeToValue));
                            new Handler().postDelayed(() -> skipKey.set(""), 200);
                        }
                        setBindListenerAgain(key);

                        // Local and server errors will be displayed as simplified like 'error has occurred'
                        // Errors will be returned throwable
                    }, throwable -> setInfo(throwable.getMessage()))
            );
        }
    }

    private void setInfo(String info) {
        this.info.setValue(info);
    }

    public LiveData<String> getInfo() {
        return info;
    }

    public LiveData<String> getBindListenerAgain() {
        return bindListenerAgain;
    }

    private void setBindListenerAgain(String key) {
        this.bindListenerAgain.setValue(key);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // changing notification preferences
        if (key.equals(context.getString(R.string.preference_key_notification_new_recipe))
                || key.equals(context.getString(R.string.preference_key_notification_comment))
                || key.equals(context.getString(R.string.preference_key_notification_likes))) {

            if (!key.equals(skipKey.get())) {
                changeNotificationSetting(key, SharedPreferencesHandler.getBoolean(context, key));
            }
        }
    }

    // App update

    public Single<Map<String, String>> getDataToDownloadUpdate(ContextWrapper context) {
        return Single.create(emitter ->
                compositeDisposable.add(appRepository.getDataToDownloadUpdate(context)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(map -> {
                            if (map != null) // always true
                                emitter.onSuccess(map);
                        }, emitter::onError,
                                () -> emitter.onError(new Throwable(context.getString(R.string.main_activity_app_up_to_date)))
                        ))
        );
    }

    public void downloadNewAppVersion(ContextWrapper context, BroadcastReceiver onComplete, Uri uri, File appUpdateFile) {
        context.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        DownloadManager downloadManager = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                            DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(appUpdateFile.getName())
                    .setDescription("Downloading app update")
                    .setDestinationUri(Uri.fromFile(appUpdateFile))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            );
        }
    }

    public void installApp(Context context, File appUpdateFile) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addCategory("android.intent.category.DEFAULT");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName(), appUpdateFile), "application/vnd.android.package-archive");
        //installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(installIntent);
    }

    // endregion

    @Override
    protected void onCleared() {
        super.onCleared();
        this.compositeDisposable.clear();
        SharedPreferencesHandler.getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
    }
}
