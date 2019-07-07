package com.myapps.ron.family_recipes.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.recycler.adapters.HtmlInstructionsPagerAdapter;
import com.myapps.ron.family_recipes.utils.ui.DepthPageTransformer;
import com.myapps.ron.family_recipes.recycler.adapters.ImagesPagerAdapter;
import com.myapps.ron.family_recipes.utils.Constants;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.List;

/**
 * Created by ronginat on 25/10/2018.
 */
public class PagerDialogFragment extends DialogFragment {
    public enum PAGER_TYPE { IMAGES, INSTRUCTIONS }
    public static final String PAGER_TYPE_KEY = "pager_type";
    private List<String> foodFiles;
    private PAGER_TYPE pagerType;

    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        if (getArguments() != null) {
            pagerType = (PAGER_TYPE) getArguments().getSerializable(PAGER_TYPE_KEY);
            if (PAGER_TYPE.IMAGES.equals(pagerType))
                foodFiles = getArguments().getStringArrayList(Constants.PAGER_FOOD_IMAGES);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

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
        return inflater.inflate(R.layout.sliding_views_layout, container, false);
        /*View v = inflater.inflate(R.layout.sliding_views_layout, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return v;*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        AppCompatImageView closeImageView = view.findViewById(R.id.close_dialog_imageView);
        WormDotsIndicator wormDotsIndicator = view.findViewById(R.id.worm_dots_indicator);

        ViewPager viewPager = view.findViewById(R.id.view_pager);
        PagerAdapter adapter = getPagerAdapter();
        if (adapter != null)
            viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        wormDotsIndicator.setViewPager(viewPager);

        closeImageView.setOnClickListener(view1 -> dismiss());

        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Nullable
    private PagerAdapter getPagerAdapter() {
        if (this.pagerType != null) {
            switch (this.pagerType) {
                case IMAGES:
                    return new ImagesPagerAdapter(activity, foodFiles);
                case INSTRUCTIONS:
                    return new HtmlInstructionsPagerAdapter(activity);
            }
        }
        return null;
    }

}
