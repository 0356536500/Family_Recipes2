package com.myapps.ron.family_recipes.ui.baseclasses;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

/**
 * Created by ronginat on 20/10/2018.
 */
public abstract class MyFragment extends Fragment {
    boolean isDestroyed = false;
    @StringRes
    private int tag;

    /**
     *
     * @return true when backPress handled, false if farther attention is required in the activity
     */
    abstract public boolean onBackPressed();

    /**
     * Checks whether this fragment was shown before and it's view subsequently
     * destroyed by placing it in the back stack
     */
    boolean restoredFromBackStack() {
        return isDestroyed;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            tag = savedInstanceState.getInt("tagRes");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
    }

    @StringRes
    public int getMyTag() {
        return this.tag;
    }

    public void setMyTag(@StringRes int tag) {
        this.tag = tag;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tag != 0)
            outState.putInt("tagRes", tag);
    }
}
