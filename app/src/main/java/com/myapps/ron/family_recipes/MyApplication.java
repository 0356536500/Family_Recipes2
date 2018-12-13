package com.myapps.ron.family_recipes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;


public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
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
        activity.setTheme(sPref.getBoolean("dark_mode", false) ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }
}