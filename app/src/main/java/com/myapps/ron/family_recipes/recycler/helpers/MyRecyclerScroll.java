package com.myapps.ron.family_recipes.recycler.helpers;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ronginat on 12/12/2018.
 */
public abstract class MyRecyclerScroll extends RecyclerView.OnScrollListener {

    private static final float MINIMUM = 100;
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

        //checkOnBottom(recyclerView);
    }

    /*private void checkOnBottom(RecyclerView recyclerView) {
        if (!recyclerView.canScrollVertically(1))
            onBottom();
    }*/

    public abstract void show();
    public abstract void hide();
    //public abstract void onBottom();
}
