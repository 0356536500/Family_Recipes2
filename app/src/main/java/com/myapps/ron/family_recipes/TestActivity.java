package com.myapps.ron.family_recipes;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.button.MaterialButton;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class TestActivity extends AppCompatActivity {
    SpeedDialView mSpeedDialView;
    MaterialButton expandedButton;
    FabExtensionAnimator fabExtensionAnimator;
    ViewHider fabHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mSpeedDialView = findViewById(R.id.test_speedDial);
        expandedButton = findViewById(R.id.test_expanded_button);

        fabHider = ViewHider.of(expandedButton).setDirection(ViewHider.BOTTOM).build();
        fabExtensionAnimator = new FabExtensionAnimator(expandedButton);
        fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState(R.string.post_recipe_next, R.drawable.ic_action_post_black));
        fabExtensionAnimator.setExtended(false);

        new Handler().postDelayed(() -> fabExtensionAnimator.setExtended(true), 1500);
        new Handler().postDelayed(() -> fabHider.hide(), 3000);
        new Handler().postDelayed(() -> fabHider.show(), 4500);

        initFloatingMenu(savedInstanceState == null);
    }

    private void initFloatingMenu(boolean addActionItems) {
        if (addActionItems) {
            /*Drawable drawable = AppCompatResources.getDrawable(activity, R.drawable.ic_custom_color);
            FabWithLabelView fabWithLabelView = mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_custom_color, drawable)
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary, getTheme()))
                    .setLabel(R.string.label_custom_color)
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());
            if (fabWithLabelView != null) {
                fabWithLabelView.setSpeedDialActionItem(fabWithLabelView.getSpeedDialActionItemBuilder()
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white_1000,
                                getTheme()))
                        .create());
            }*/

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_action, R.drawable
                    .ic_action_post_black)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_100,
                            getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_add)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_600,
                            getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_preview_action, R.drawable.ic_preview_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_700,
                            getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_preview)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_700,
                            getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_template_action, R.drawable.ic_template_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_light_green_800,
                            getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_template)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_light_green_800,
                            getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    //.setTheme(R.style.AppTheme_Purple)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_reset_action, R.drawable.ic_reset_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_900,
                            getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_reset)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_900,
                            getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    .create());

        }

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.fab_add_action:
                    mSpeedDialView.close(); // To close the Speed Dial with animation
                    return true; // false will close it without animation
                case R.id.fab_preview_action:
                    break;
                case R.id.fab_template_action:
                    return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                case R.id.fab_reset_action:
                    break;

                default:
                    break;
            }
            return true; // To keep the Speed Dial open
        });

    }
}
