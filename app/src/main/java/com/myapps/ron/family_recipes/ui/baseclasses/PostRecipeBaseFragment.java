package com.myapps.ron.family_recipes.ui.baseclasses;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.myapps.ron.family_recipes.FabExtensionAnimator;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.activities.PostRecipeActivity;

import androidx.annotation.Nullable;

import static androidx.core.content.ContextCompat.getDrawable;

/**
 * Created by ronginat on 04/03/2019.
 */
public abstract class PostRecipeBaseFragment extends MyFragment {

    protected PostRecipeActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (PostRecipeActivity)getActivity();
        //togglePersistentUi();
    }

    public void toggleFab(boolean show) { activity.toggleFab(show); }

    public boolean showsFab() { return false; }

    public void togglePersistentUi() {
        //toggleFab(showsFab());
        //if (!restoredFromBackStack()) setFabExtended(true);
        setFabExtended(true);
        new Handler().postDelayed(() -> setFabExtended(false), 2000);

        activity.setTitle(getTitle());
        activity.updateFab(getFabState());
        activity.setFabClickListener(getFabClickListener());
    }

    protected void setFabExtended(boolean extended) {
        activity.setFabExtended(extended);
    }

    protected boolean isFabExtended() { return activity.isFabExtended(); }

    protected String getTitle() {
        return getClass().getSimpleName();
    }

    protected FabExtensionAnimator.GlyphState getFabState() {
        return FabExtensionAnimator.newState(R.string.post_recipe_next, R.drawable.ic_done_fab);
    }

    protected View.OnClickListener getFabClickListener() { return view -> {
        Log.e(getClass().getSimpleName(), "Click event");
        activity.nextFragment();
    }; }

    @Override
    public void onStart() {
        super.onStart();
        //togglePersistentUi();
    }
}
