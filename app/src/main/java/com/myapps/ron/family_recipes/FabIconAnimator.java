package com.myapps.ron.family_recipes;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.google.android.material.button.MaterialButton;


import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

public class FabIconAnimator {

    private static final String ROTATION_Y_PROPERTY = "rotationY";

    private static final float TWITCH_END = 20F;
    private static final float TWITCH_START = 0F;
    private static final int DURATION = 200;

    @DrawableRes
    private int currentIcon;
    @StringRes
    private int currentText;
    private boolean isAnimating;

    private final MaterialButton button;
    private final ConstraintLayout container;
    private final Transition.TransitionListener listener = new Transition.TransitionListener() {
        public void onTransitionStart(@NonNull Transition transition) { isAnimating = true; }

        public void onTransitionEnd(@NonNull Transition transition) { isAnimating = false; }

        public void onTransitionCancel(@NonNull Transition transition) { isAnimating = false; }

        public void onTransitionPause(@NonNull Transition transition) { }

        public void onTransitionResume(@NonNull Transition transition) { }
    };

    public FabIconAnimator(ConstraintLayout container) {
        this.container = container;
        this.button = container.findViewById(R.id.fab);
    }

    public void update(@DrawableRes int icon, @StringRes int text) {
        boolean isSame = currentIcon == icon && currentText == text;
        currentIcon = icon;
        currentText = text;
        animateChange(icon, text, isSame);
    }

    public void setExtended(boolean extended) {
        setExtended(extended, false);
    }

    public void setOnClickListener(@Nullable View.OnClickListener clickListener) {
        if (clickListener == null) {
            button.setOnClickListener(null);
            return;
        }
        AtomicBoolean flag = new AtomicBoolean(true);
        button.setOnClickListener(view -> {
            if (!flag.getAndSet(false)) return;
            clickListener.onClick(view);
            button.postDelayed(() -> flag.set(true), 2000);
        });
    }

    private boolean isExtended() { // R.dimen.triple_and_half_margin is 56 dp.
        return button.getLayoutParams().height != button.getResources().getDimensionPixelSize(R.dimen.triple_and_half_margin);
    }

    private void animateChange(@DrawableRes int icon, @StringRes int text, boolean isSame) {
        boolean extended = isExtended();
        button.setText(text);
        button.setIconResource(icon);
        setExtended(extended, !isSame);
        if (!extended) twitch();
    }

    private void setExtended(boolean extended, boolean force) {
        if (isAnimating || (extended && isExtended() && !force)) return;

        ConstraintSet set = new ConstraintSet();
        set.clone(container.getContext(), extended ? R.layout.fab_extended : R.layout.fab_collapsed);

        TransitionManager.beginDelayedTransition(container, new AutoTransition()
                .addListener(listener).setDuration(150));

        if (extended) button.setText(currentText);
        else button.setText("");

        set.applyTo(container);
    }

    private void twitch() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator twitchA = animateProperty(ROTATION_Y_PROPERTY, TWITCH_START, TWITCH_END);
        ObjectAnimator twitchB = animateProperty(ROTATION_Y_PROPERTY, TWITCH_END, TWITCH_START);

        set.play(twitchB).after(twitchA);
        set.start();
    }

    @NonNull
    private ObjectAnimator animateProperty(String property, float start, float end) {
        return ObjectAnimator.ofFloat(container, property, start, end).setDuration(DURATION);
    }
}