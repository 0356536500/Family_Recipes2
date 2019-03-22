package com.myapps.ron.family_recipes.utils;

import android.content.Context;
import android.text.format.DateUtils;

import com.myapps.ron.family_recipes.network.Constants;

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
    private static final String DATE_TIME_TEMPLATE = "yyyy-MM-dd HH:mm:ss";//.SSS";
    private static final String DATE_TEMPLATE = "dd/MM/yyyy";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_TIME_TEMPLATE, Locale.ENGLISH);

    /*public static String getUTCTimeOld() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(new Date());
    }*/

    public static long getUTCTime() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
    }

    public static String getLocalFromUTC(long dateUTC) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(dateUTC);//DATE_FORMAT.parse(dateUTC);
        DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return DATE_FORMAT.format(date);
    }

    public static CharSequence getPrettyDateFromTime(long utcTime) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date(utcTime));
        calendar.setTimeZone(TimeZone.getDefault());
        return DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis());
    }

    public static long getLastUpdateTime(Context context) {
        return SharedPreferencesHandler.getLong(context, UPDATED_TIME_KEY, Constants.DEFAULT_UPDATED_TIME);
    }

    public static void updateServerTime(Context context, long updatedTime) {
        SharedPreferencesHandler.writeLong(context, UPDATED_TIME_KEY, updatedTime);
    }

    //region categories
    public static boolean shouldUpdateCategories(Context context) {
        Date lastUpdate = new Date(getLastCategoriesUpdateTime(context));

        Calendar c = new GregorianCalendar();
        c.setTime(lastUpdate);
        c.add(Calendar.DATE, Constants.CATEGORIES_ELAPSED_TIME_TO_UPDATE);
        Date lastPlusDefault = c.getTime();
        Date now = new Date();

        return now.after(lastPlusDefault);
    }

    public static long getLastCategoriesUpdateTime(Context context) {
        return SharedPreferencesHandler.getLong(context, UPDATED_CATS_KEY, Constants.DEFAULT_UPDATED_TIME);
    }

    public static void updateCategoriesServerTime(Context context, long updatedTime) {
        SharedPreferencesHandler.writeLong(context, UPDATED_CATS_KEY, updatedTime);
    }
    //endregion

}
