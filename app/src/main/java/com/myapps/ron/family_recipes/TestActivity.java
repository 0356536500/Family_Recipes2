package com.myapps.ron.family_recipes;

import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
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
        /*Date date = Date.from(Instant.parse("2019-03-25T16:45:26.357Z"));
        Log.e(getClass().getSimpleName(), date.toString());*/

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String first = "text";
        String second = "some more";
        String third = "last";

        List<String> words = new ArrayList<>();
        words.add(first);
        words.add(second);
        words.add(third);
        for(String word: words) {
            builder
                    .append(word)
                    .setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            Log.e(TestActivity.class.getSimpleName(), "onClick, " + word);
                            Toast.makeText(TestActivity.this, word + " clicked", Toast.LENGTH_SHORT).show();
                        }

                        // optional - for styling the specific text
                        /*@Override
                        public void updateDrawState(@NonNull TextPaint textPaint) {
                            textPaint.setColor(textPaint.linkColor);    // you can use custom color
                            textPaint.setUnderlineText(false);    // this remove the underline
                        }*/
                    }, builder.length() - word.length(), builder.length(), 0);
            builder.append(", ");
        }
        ((TextView)findViewById(R.id.test_text)).setText(builder, TextView.BufferType.SPANNABLE);
        ((TextView)findViewById(R.id.test_text)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void text() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String originalString = "#Your string";
        String[] words = originalString.split(" ");
        for(String word : words){
            if(word.startsWith("#")){
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View textView) {
                        //use word here to make a decision
                    }
                };
                builder
                        .append(word)
                        .setSpan(clickableSpan, builder.length() - word.length(), builder.length(), 0);
            }
        }


        TextView textView = findViewById(R.id.test_text);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
