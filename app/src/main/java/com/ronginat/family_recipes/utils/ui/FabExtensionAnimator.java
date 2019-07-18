package com.ronginat.family_recipes.utils.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.ripple.RippleUtils;
import com.ronginat.family_recipes.R;

import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

/**
 * taken from:
 * {@link "https://github.com/tunjid/android-bootstrap/blob/master/material/src/main/java/com/tunjid/androidbootstrap/material/animator/FabExtensionAnimator.java"}
 */

public class FabExtensionAnimator {

    private static final float TWITCH_END = 20.0f;
    private static final float TWITCH_START = 0.0f;
    private static final int TWITCH_DURATION = 200;
    public static final int EXTENSION_DURATION = 150;
    private static final String ROTATION_Y_PROPERTY = "rotationY";

    private final int initialCornerRadius;
    private final int collapsedFabSize;
    private final int extendedFabHeight;
    private boolean isAnimating;

    private GlyphState glyphState;
    private final MaterialButton button;

    private final Transition.TransitionListener listener = new Transition.TransitionListener() {
        public void onTransitionStart(@NonNull Transition transition) {
            isAnimating = true;
            button.setBackground(getDrawable());
        }

        public void onTransitionEnd(@NonNull Transition transition) { isAnimating = false; }

        public void onTransitionCancel(@NonNull Transition transition) { isAnimating = false; }

        public void onTransitionPause(@NonNull Transition transition) { }

        public void onTransitionResume(@NonNull Transition transition) {}
    };

    public FabExtensionAnimator(MaterialButton button) {
        this.button = button;
        this.initialCornerRadius = button.getCornerRadius();
        collapsedFabSize = button.getResources().getDimensionPixelSize(R.dimen.collapsed_fab_size);
        extendedFabHeight = button.getResources().getDimensionPixelSize(R.dimen.extended_fab_height);
        button.setBackground(getDrawable());
    }

    public static GlyphState newState(@StringRes int resId, @DrawableRes int icon) { return new SimpleGlyphState(resId, icon);}

    public void updateGlyphs(@NonNull GlyphState glyphState) {
        boolean isSame = glyphState.equals(this.glyphState);
        this.glyphState = glyphState;
        animateChange(glyphState, isSame);
    }

    public void setExtended(boolean extended) { setExtended(extended, false); }

    public void setExtended(boolean extended, long delay) {
        new Handler().postDelayed(() -> setExtended(extended, false), delay);
    }

    //@SuppressWarnings("WeakerAccess")
    public boolean isExtended() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();//ViewUtil.getLayoutParams(button);
        return !(params.height == params.width && params.width == getCollapsedFabSize());
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    private void animateChange(GlyphState glyphState, boolean isSame) {
        boolean extended = isExtended();
        this.button.setText(glyphState.getText());
        this.button.setIconResource(glyphState.getIcon());
        setExtended(extended, !isSame);
        if (!extended) onPreExtend();
    }

    private void setExtended(boolean extended, boolean force) {
        if (isAnimating || (extended && isExtended() && !force)) return;

        int collapsedFabSize = getCollapsedFabSize();
        int width = extended ? ViewGroup.LayoutParams.WRAP_CONTENT : collapsedFabSize;
        int height = extended ? getExpandedFabHeight() : collapsedFabSize;

        ViewGroup.LayoutParams params = button.getLayoutParams();//ViewUtil.getLayoutParams(button);
        ViewGroup group = (ViewGroup) button.getParent();

        params.width = width;
        params.height = height;

        TransitionManager.beginDelayedTransition(group, new AutoTransition()
                .setDuration(EXTENSION_DURATION)
                .addListener(listener)
                .addTarget(button));

        if (extended) this.button.setText(this.glyphState.getText());
        else this.button.setText("");

        button.requestLayout();
        button.invalidate();
    }

    @SuppressWarnings("WeakerAccess")
    public void onPreExtend() {
        AnimatorSet set = new AnimatorSet();
        set.play(animateProperty(TWITCH_END, TWITCH_START)).after(animateProperty(TWITCH_START, TWITCH_END));
        set.start();
    }

    private int getInitialCorenerRadius() {
        return this.initialCornerRadius;
    }

    @SuppressWarnings("WeakerAccess")
    protected int getCollapsedFabSize() { return collapsedFabSize;}

    @SuppressWarnings("WeakerAccess")
    protected int getExpandedFabHeight() { return extendedFabHeight;}

    @NonNull
    private ObjectAnimator animateProperty(float start, float end) {
        return ObjectAnimator.ofFloat(button, ROTATION_Y_PROPERTY, start, end).setDuration(TWITCH_DURATION);
    }

    @SuppressLint("RestrictedApi")
    private Drawable getDrawable() {
        int cornerRadius = isExtended() ? getInitialCorenerRadius() : getCollapsedFabSize();
        int strokeWidth = button.getStrokeWidth();
        ColorStateList rippleColor = button.getRippleColor();
        ColorStateList strokeColor = button.getStrokeColor();

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setCornerRadius(cornerRadius);
        backgroundDrawable.setColor(-1);

        GradientDrawable strokeDrawable = new GradientDrawable();
        strokeDrawable.setStroke(strokeWidth, strokeColor);
        strokeDrawable.setCornerRadius(cornerRadius);
        strokeDrawable.setColor(0);

        GradientDrawable maskDrawable = new GradientDrawable();
        maskDrawable.setCornerRadius(cornerRadius);
        maskDrawable.setColor(-1);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{backgroundDrawable, strokeDrawable});
        return new RippleDrawable(RippleUtils.convertToRippleDrawableColor(rippleColor), layerDrawable, maskDrawable);
    }

    public static abstract class GlyphState {

        public abstract int getIcon();

        public abstract int getText();
    }

    private static class SimpleGlyphState extends GlyphState {

        @DrawableRes
        final int icon;
        @StringRes
        final int text;

        private SimpleGlyphState(int text, int icon) {
            this.text = text;
            this.icon = icon;
        }

        public int getText() { return text; }

        public int getIcon() { return icon; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleGlyphState that = (SimpleGlyphState) o;
            return Objects.equals(icon, that.icon) &&
                    Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(icon, text);
        }
    }
}
