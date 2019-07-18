package com.ronginat.family_recipes.ui.activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.ui.baseclasses.MyBaseActivity;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.HtmlHelper;

/**
 * Created by ronginat on 09/07/2019
 */
public class AboutActivity extends MyBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.title_activity_about);
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
    }
}
