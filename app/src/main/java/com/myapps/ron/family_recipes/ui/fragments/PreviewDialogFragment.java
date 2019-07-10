package com.myapps.ron.family_recipes.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.HtmlHelper;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PreviewDialogFragment extends Fragment {

    private Activity activity;
    private String html;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        if (getArguments() != null) {
            html = getArguments().getString("html");
            //Log.e(getClass().getSimpleName(), "html");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.html_preview_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebView webView = view.findViewById(R.id.html_preview_webView);
        /*TypedValue value = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.toolbarBackgroundSecondary, value, true);
        webView.setBackgroundColor(value.data);*/

        //webView.loadData(html, "text/html", "utf-8");
        webView.loadDataWithBaseURL(Constants.ASSET_FILE_BASE_URL,
                HtmlHelper.GET_CSS_LINK() + html, "text/html", "UTF-8", null);
    }
}
