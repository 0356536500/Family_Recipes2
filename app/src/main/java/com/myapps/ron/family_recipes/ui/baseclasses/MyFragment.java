package com.myapps.ron.family_recipes.ui.baseclasses;

import androidx.fragment.app.Fragment;

/**
 * Created by ronginat on 20/10/2018.
 */
public abstract class MyFragment extends Fragment {
    boolean isDestroyed = false;

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
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
    }

}
