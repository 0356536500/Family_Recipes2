package com.myapps.ron.family_recipes.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.amazonaws.services.s3.util.Mimetypes;
import com.myapps.ron.family_recipes.R;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PreviewDialogFragment extends Fragment {

    private String html, path;

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

        webView.loadData(html, "text/html", "utf-8");

        //webView.loadData(html, "text/html", "utf-8");
    }
}
