package com.myapps.ron.family_recipes.recycler.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.myapps.ron.family_recipes.R;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by ronginat on 17/03/2019.
 */
public class HtmlInstructionsPagerAdapter extends PagerAdapter {

    //@SuppressWarnings("FieldCanBeLocal")
    private final int INSTRUCTIONS_SIZE = 4;
    private Animation fadeIn = new AlphaAnimation(0f, 1f);

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (position < INSTRUCTIONS_SIZE)
            return inflateItem(container, position);
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return INSTRUCTIONS_SIZE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    // region Init Layouts

    @NonNull
    private View inflateItem(@NonNull ViewGroup container, int position) {
        View view;
        switch (position) {
            case 0: //flow down
            case 4:
                view = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.pager_instructions_general_text, container, false);
                break;
            default:
                view = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.pager_instructions_cardview_with_webview, container, false);
                break;
        }
        fillItemWithContent(view, position);
        return view;
    }

    private void fillItemWithContent(@NonNull View view, int position) {
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    // region Init Content
    private void generateGeneralExplanation(@NonNull View view) {
        // bind the views
        TextView textView1 = view.findViewById(R.id.pager_instructions_general_textView1);
        TextView textView2 = view.findViewById(R.id.pager_instructions_general_textView2);
        TextView textView3 = view.findViewById(R.id.pager_instructions_general_textView3);

        //textView1.setTypeface();
        textView1.setText(R.string.post_recipe_instructions_general_help1);

        // animate the views with fade in animation
        textView1.startAnimation(fadeIn);
        textView2.startAnimation(fadeIn);
        textView3.startAnimation(fadeIn);
    }

    // endregion

    // endregion
}
