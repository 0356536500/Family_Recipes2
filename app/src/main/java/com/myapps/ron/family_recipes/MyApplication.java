package com.myapps.ron.family_recipes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.core.app.ActivityCompat;

import com.myapps.ron.family_recipes.utils.LocaleHelper;


public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public boolean checkInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            //we are connected to a network
            return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        }
        return false;
    }

    public void applyTheme(Activity activity) {
        SharedPreferences sPref = activity.getSharedPreferences(getString(R.string.sharedPreferences), MODE_PRIVATE);
        activity.setTheme(sPref.getBoolean(getString(R.string.preference_key_dark_mode), false) ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    public static Context getContext() {
        return mContext;
    }
}