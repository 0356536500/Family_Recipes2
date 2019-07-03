package com.myapps.ron.family_recipes.utils.logic;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by ronginat on 30/06/2019
 */
public class CrashLogger {

    /**
     * Instantiate Crashlytics
     * @param context application context
     */
    public static void init(Context context) {
        Fabric.with(context, new Crashlytics());
    }

    /**
     * Set user identifier for error logs
     * @param name username of AWS Cognito account
     */
    public static void setName(String name) {
        Crashlytics.setUserName(name);
    }

    /**
     * Log an Exception to be viewed and analyzed later
     * @param throwable Throwable to be logged
     */
    public static void logException(Throwable throwable) {
        Crashlytics.logException(throwable);
    }
}
