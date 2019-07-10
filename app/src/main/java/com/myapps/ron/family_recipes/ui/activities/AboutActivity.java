package com.myapps.ron.family_recipes.ui.activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.HtmlHelper;

/**
 * Created by ronginat on 09/07/2019
 */
public class AboutActivity extends MyBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        WebView webView = findViewById(R.id.about_webView);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.loadDataWithBaseURL(
                Constants.ASSET_FILE_BASE_URL,
                HtmlHelper.GET_ABOUT_PAGE(this),
                "text/html", "UTF-8", null);

        /*TextView textView = findViewById(R.id.about_text);
        try {
            String html = IOUtils.toString(getResources().getAssets().open("about_eng.html"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(HtmlHelper.GET_CSS_LINK(this) + html, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(HtmlHelper.GET_CSS_LINK(this) + html));
            }
        } catch (IOException e) {
            e.printStackTrace();
            String failed = "Failed loading html.";
            textView.setText(failed);
        }*/
    }
}
