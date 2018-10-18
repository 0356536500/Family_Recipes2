package com.myapps.ron.family_recipes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.myapps.ron.family_recipes.R;

public class SharedPreferencesHandler {
    public static void wipeSharedPreferences(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (getBoolean(ctx, "rememberMe")) {
            editor.putString("user", getString(ctx, "user"));
            editor.putString("password", getString(ctx, "password"));
        }
        editor.clear();
        editor.apply();
    }

    public static void writeInt(Context ctx, String key, int value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void writeString(Context ctx, String key, String value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void writeBoolean(Context ctx, String key, boolean value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public static String getString(Context ctx, String key) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }

    public static String getString(Context ctx, String key, String defValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPref.getString(key, defValue);
    }
}