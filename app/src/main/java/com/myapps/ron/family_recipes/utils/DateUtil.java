package com.myapps.ron.family_recipes.utils;

import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.network.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    private static final String TAG = "DateUtil";

    private static final String UPDATED_TIME_KEY = "last_updated_time";
    private static final String UPDATED_CATS_KEY = "last_updated_categories";
    private static final String DATE_TEMPLATE = "yyyy-MM-dd HH:mm:ss";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_TEMPLATE, Locale.ENGLISH);

    public static String getUTCTime() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(new Date());
    }

    public static String getLocalFromUTC(String dateUTC) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = DATE_FORMAT.parse(dateUTC);
            DATE_FORMAT.setTimeZone(TimeZone.getDefault());
            return DATE_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateUTC;
    }

    public static String getLastUpdateTime(Context context) {
        return SharedPreferencesHandler.getString(context, UPDATED_TIME_KEY, Constants.DEFAULT_UPDATED_TIME);
    }

    public static void updateServerTime(Context context, String updatedTime) {
        SharedPreferencesHandler.writeString(context, UPDATED_TIME_KEY, updatedTime);
    }

    //region categories
    public static boolean shouldUpdateCategories(Context context) {
        try {
            Date lastUpdate = DATE_FORMAT.parse(getLastCategoriesUpdateTime(context));
            Calendar c = new GregorianCalendar();
            c.setTime(lastUpdate);
            c.add(Calendar.DATE, Constants.CATEGORIES_ELAPSED_TIME_TO_UPDATE);
            Date lastPlusDefault = c.getTime();
            Date now = new Date();
/*            Log.e(TAG, "last update time: " + lastUpdate.toString());
            Log.e(TAG, "last update plus time: " + lastPlusDefault.toString());
            Log.e(TAG, "current time: " + new Date().toString());
            Log.e(TAG, "should update? " + now.after(lastPlusDefault));*/
            return now.after(lastPlusDefault);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    public static String getLastCategoriesUpdateTime(Context context) {
        return SharedPreferencesHandler.getString(context, UPDATED_CATS_KEY, Constants.DEFAULT_UPDATED_TIME);
    }

    public static void updateCategoriesServerTime(Context context, String updatedTime) {
        SharedPreferencesHandler.writeString(context, UPDATED_CATS_KEY, updatedTime);
    }
    //endregion

}
