package com.ronginat.family_recipes.utils.logic;

import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;

import com.ronginat.family_recipes.layout.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    private static final String TAG = "DateUtil";

    private static final String UPDATED_TIME_KEY = "last_updated_time";
    private static final String UPDATED_CATS_KEY = "last_updated_categories";
    private static final String DATE_TIME_TEMPLATE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_TIME_TEMPLATE, Locale.ENGLISH);

    /*public static String getUTCTimeOld() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(new Date());
    }*/

    public static String getUTCTime() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(new Date());
    }

    public static String getLocalFromUTC(String dateUTC) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = getDateFromStringTimestamp(dateUTC);
        DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return DATE_FORMAT.format(date);
    }

    public static CharSequence getPrettyDateFromTime(String utcTime) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(getDateFromStringTimestamp(utcTime));
        calendar.setTimeZone(TimeZone.getDefault());
        return DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis());
    }

    public static String getLastUpdateTime(Context context) {
        return SharedPreferencesHandler.getString(context, UPDATED_TIME_KEY, Constants.DEFAULT_UPDATED_TIME);
    }

    public static void updateServerTime(Context context, String updatedTime) {
        SharedPreferencesHandler.writeString(context, UPDATED_TIME_KEY, updatedTime);
    }

    //region categories
    public static boolean shouldUpdateCategories(Context context) {
        Date lastUpdate = getDateFromStringTimestamp(getLastCategoriesUpdateTime(context));

        Calendar c = new GregorianCalendar();
        c.setTime(lastUpdate);
        c.add(Calendar.DATE, Constants.CATEGORIES_ELAPSED_TIME_TO_UPDATE);
        Date lastPlusDefault = c.getTime();
        Date now = new Date();

        return now.after(lastPlusDefault);
    }

    public static String getLastCategoriesUpdateTime(Context context) {
        return SharedPreferencesHandler.getString(context, UPDATED_CATS_KEY, Constants.DEFAULT_UPDATED_TIME);
    }

    public static void updateCategoriesServerTime(Context context, String updatedTime) {
        SharedPreferencesHandler.writeString(context, UPDATED_CATS_KEY, updatedTime);
    }

    private static Date getDateFromStringTimestamp(String timestamp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Date.from(Instant.parse(timestamp));
        } else {
            try {
                return DATE_FORMAT.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new Date();
        }
    }
    //endregion

}
