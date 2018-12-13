package com.myapps.ron.family_recipes.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.amazonaws.services.s3.util.Mimetypes;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.activities.MainActivity;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PreviewDialogFragment extends Fragment {

    private String html;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            html = getArguments().getString("html");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.html_preview_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Log.e("dialog", html);
        WebView webView = view.findViewById(R.id.html_preview_webView);
        /*TypedValue value = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.toolbarBackgroundSecondary, value, true);
        webView.setBackgroundColor(value.data);*/

        webView.loadData(html, "text/html", "utf-8");

        //webView.loadData(html, "text/html", "utf-8");
    }
}
