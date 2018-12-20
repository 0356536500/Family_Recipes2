package com.myapps.ron.family_recipes.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.utils.DepthPageTransformer;
import com.myapps.ron.family_recipes.adapters.MyPagerAdapter;
import com.myapps.ron.family_recipes.utils.Constants;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

/**
 * Created by ronginat on 25/10/2018.
 */
public class PagerDialogFragment extends DialogFragment {
    private Recipe recipe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            recipe = getArguments().getParcelable(Constants.RECIPE);
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
        return inflater.inflate(R.layout.sliding_images_layout, container, false);
        /*View v = inflater.inflate(R.layout.sliding_images_layout, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return v;*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        AppCompatImageView closeImageView = view.findViewById(R.id.close_dialog_imageView);
        WormDotsIndicator wormDotsIndicator = view.findViewById(R.id.worm_dots_indicator);

        ViewPager viewPager = view.findViewById(R.id.view_pager);
        MyPagerAdapter adapter = new MyPagerAdapter(getContext(), recipe);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        wormDotsIndicator.setViewPager(viewPager);

        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

}
