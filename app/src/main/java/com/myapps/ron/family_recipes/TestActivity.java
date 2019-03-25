package com.myapps.ron.family_recipes;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.internal.bind.util.ISO8601Utils;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.text.ParseException;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        /*DateUtil.DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Z"));
        try {
            Date date = DateUtil.DATE_FORMAT.parse("2019-03-25T16:45:26.357Z");
            Log.e(getClass().getSimpleName(), date.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        /*try {
            Date date = ISO8601Utils.parse("", new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        Date date = Date.from(Instant.parse("2019-03-25T16:45:26.357Z"));
        Log.e(getClass().getSimpleName(), date.toString());
    }

}
