package com.myapps.ron.family_recipes.ui.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.DepthPageTransformer;
import com.myapps.ron.family_recipes.ui.MainActivity;
import com.myapps.ron.family_recipes.ui.MyPagerAdapter;
import com.myapps.ron.family_recipes.ui.ZoomOutPageTransformer;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

/**
 * Created by ronginat on 25/10/2018.
 */
public class PagerDialogFragment extends DialogFragment {
    int mNum;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
/*
    static PagerDialogFragment newInstance(int num) {
        PagerDialogFragment f = new PagerDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }
*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        mNum = 4;
        if (getArguments() != null) {
            mNum = getArguments().getInt("num");
        }

        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        switch ((mNum-1)%6) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }
        switch ((mNum-1)%6) {
            case 4: theme = android.R.style.Theme_Holo; break;
            case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            case 6: theme = android.R.style.Theme_Holo_Light; break;
            case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
            case 8: theme = android.R.style.Theme_Holo_Light; break;
        }
        setStyle(style, theme);*/
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        /*if(getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
            //getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
            getDialog().getWindow().setLayout(
                    getResources().getDisplayMetrics().widthPixels - 150,
                    getResources().getDisplayMetrics().heightPixels / 2
            );

        }
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.40f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);*/
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.sliding_images_layout, container, false);
        View v = inflater.inflate(R.layout.sliding_images_layout, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //View tv = v.findViewById(R.id.text);
        /*((TextView)tv).setText("Dialog #" + mNum + ": using style "
                + getNameForNum(mNum));*/

        // Watch for button clicks.
        /*Button button = v.findViewById(R.id.show);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                ((MainActivity)getActivity()).showDialog();
            }
        });*/

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        DotsIndicator dotsIndicator = view.findViewById(R.id.dots_indicator);
        SpringDotsIndicator springDotsIndicator = view.findViewById(R.id.spring_dots_indicator);
        WormDotsIndicator wormDotsIndicator = view.findViewById(R.id.worm_dots_indicator);

        ViewPager viewPager = view.findViewById(R.id.view_pager);
        MyPagerAdapter adapter = new MyPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        dotsIndicator.setViewPager(viewPager);
        springDotsIndicator.setViewPager(viewPager);
        wormDotsIndicator.setViewPager(viewPager);
    }
}
