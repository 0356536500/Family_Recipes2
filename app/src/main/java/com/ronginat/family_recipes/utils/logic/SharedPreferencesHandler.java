package com.ronginat.family_recipes.utils.logic;

import android.content.Context;
import android.content.SharedPreferences;

import com.ronginat.family_recipes.R;

public class SharedPreferencesHandler {
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
    }

    /*public static void wipeSharedPreferences(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (getBoolean(ctx, "rememberMe")) {
            editor.putString("user", getString(ctx, "user"));
            editor.putString("password", getString(ctx, "password"));
        }
        editor.clear();
        editor.apply();
    }*/

    public static void writeLong(Context ctx, String key, long value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
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

    public static boolean getBoolean(Context ctx, String key, boolean defValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static long getLong(Context ctx, String key, long defValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPref.getLong(key, defValue);
    }

    public static String getString(Context ctx, String key) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }

    public static String getString(Context ctx, String key, String defValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPref.getString(key, defValue);
    }

    public static void removeString(Context ctx, String key) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }
}