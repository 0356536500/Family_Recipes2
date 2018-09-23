package com.myapps.ron.family_recipes.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    private static final String DATE_TEMPLATE = "yyyy-MM-dd HH:mm:ss";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_TEMPLATE, Locale.ENGLISH);

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

    public static String lastUpdateTime() {
        return "";
    }

}
