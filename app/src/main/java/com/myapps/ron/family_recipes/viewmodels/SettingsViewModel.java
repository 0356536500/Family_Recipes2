package com.myapps.ron.family_recipes.viewmodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by ronginat on 17/02/2019.
 */
public class SettingsViewModel extends ViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    //private final String TAG = getClass().getSimpleName();
    private MutableLiveData<String> info = new MutableLiveData<>();
    public MutableLiveData<Map.Entry<String, Boolean>> changeKeyToValue = new MutableLiveData<>();
    private Context context;

    private AtomicReference<String> skipKey = new AtomicReference<>("");

    public SettingsViewModel() {
        this.context = MyApplication.getContext();
        SharedPreferencesHandler.getSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    private void changeNotificationSetting(String key, boolean changeToValue) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            // call to server
            Map<String, String> queries = new HashMap<>();
            queries.put(key, changeToValue ? Constants.SUBSCRIPTION_SUBSCRIBE : Constants.SUBSCRIPTION_UNSUBSCRIBE);
            APICallsHandler.manageSubscriptions(AppHelper.getAccessToken(), MyApplication.getDeviceId(), queries, new HashMap<>(), message -> {
                if (message != null) {
                    setInfo(message);
                    skipKey.set(key);
                    changeKeyToValue.setValue(new AbstractMap.SimpleEntry<>(key, !changeToValue));
                    new Handler().postDelayed(() ->
                            skipKey.set(""), 200);
                }
            });
        } else { // no internet. reset the preference value
            setInfo(context.getString(R.string.no_internet_message));
            new Handler().postDelayed(() ->
                    SharedPreferencesHandler.writeBoolean(context, key, !changeToValue), 1500);
        }
    }

    private void setInfo(String info) {
        this.info.setValue(info);
    }

    public LiveData<String> getInfo() {
        return info;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // changing notification preferences
        if (key.equals(context.getString(R.string.preference_key_notification_new_recipe))
                || key.equals(context.getString(R.string.preference_key_notification_comment))
                || key.equals(context.getString(R.string.preference_key_notification_likes))) {

            if (!key.equals(skipKey.get()))
                changeNotificationSetting(key, SharedPreferencesHandler.getBoolean(context, key));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        SharedPreferencesHandler.getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
    }
}
