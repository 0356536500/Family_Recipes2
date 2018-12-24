package com.myapps.ron.family_recipes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.flexBox_layout)
    FlexboxLayout flexboxLayout;

    FlexboxLayout.LayoutParams layoutParams;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ButterKnife.bind(this);

        new Handler().postDelayed(this::createMoreViews, 1000);
    }

    private void createMoreViews() {
        layoutParams = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 20;
        layoutParams.bottomMargin = 20;
        layoutParams.rightMargin = 20;
        layoutParams.leftMargin = 20;
        //layoutParams.setMargins(16,16,16,16);

        for (int i = 0; i < 20; i++) {
            TextView view = (TextView) LayoutInflater.from(this).inflate(R.layout.just_text_view, flexboxLayout, false);
            //TextView view = new TextView(this);
            String text = "Message#" + i;
            view.setText(text);
            //view.setTextSize(30f);
            //view.setLayoutParams(layoutParams);

            flexboxLayout.addView(view);
        }

    }


}
