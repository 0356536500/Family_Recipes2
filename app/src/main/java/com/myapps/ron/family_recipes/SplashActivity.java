package com.myapps.ron.family_recipes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.myapps.ron.family_recipes.utils.DateUtil;
import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_TIME_OUT = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //writeToSharedPref();

        final String userParam = SharedPreferencesHandler.getString(this, "username");
        final String passParam = SharedPreferencesHandler.getString(this, "password");

        
        //APICallsHandler.getAllRecipes("0", Constants.TOKEN);

        /*new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
            if (SharedPreferencesHandler.getBoolean(getApplicationContext(), "rememberMe")) {

            }
            }
        }, SPLASH_TIME_OUT);*/
    }

    private void writeToSharedPref() {
        SharedPreferencesHandler.writeString(getApplicationContext(), "username", "hello");
        SharedPreferencesHandler.writeString(getApplicationContext(), "password", "world");
        SharedPreferencesHandler.writeBoolean(getApplication(), "rememberMe", true);
    }
}
