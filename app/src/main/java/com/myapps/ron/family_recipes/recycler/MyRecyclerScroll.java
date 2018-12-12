package com.myapps.ron.family_recipes.recycler;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Created by ronginat on 12/12/2018.
 */
public abstract class MyRecyclerScroll extends RecyclerView.OnScrollListener {

    private static final float MINIMUM = 40;
    private int scrollDist = 0;
    private boolean isVisible = true;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (isVisible && scrollDist > MINIMUM) {
            hide();
            scrollDist = 0;
            isVisible = false;
        }
        else if (!isVisible && scrollDist < -MINIMUM) {
            show();
            scrollDist = 0;
            isVisible = true;
        }

        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }

    }

    public abstract void show();
    public abstract void hide();
}
