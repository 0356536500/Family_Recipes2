package com.myapps.ron.family_recipes.recycler.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ronginat on 10/03/2019.
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
public class SwipeAndDragHelper extends ItemTouchHelper.Callback {
    private final float ALPHA_FULL = 1.0f;
    private final int PAINT_ALPHA_FULL = 255;
    private ActionCompletionContract contract;

    public SwipeAndDragHelper(ActionCompletionContract contract) {
        this.contract = contract;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        /*if (viewHolder instanceof SectionHeaderViewHolder) {
            return 0;
        }*/
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START; //ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        contract.onViewMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        contract.onViewSwiped(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            /*float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
            viewHolder.itemView.setAlpha(alpha);*/

            View itemView = viewHolder.itemView;

            Paint p = new Paint();
            Bitmap icon;

            if (dX > 0) {
                final int paintAlpha = (int) (PAINT_ALPHA_FULL - (Math.abs(dX) / (float) viewHolder.itemView.getWidth()) * PAINT_ALPHA_FULL);
                icon = getBitmapFromVectorDrawable(MyApplication.getContext(), R.drawable.ic_reset_fab, paintAlpha);
                p.setAlpha(paintAlpha);

                /*icon = BitmapFactory.decodeResource(
                        MyApplication.getContext().getResources(), R.drawable.ic_reset_fab);*/

                /* Set your color for positive displacement */
                //p.setARGB(255, 255, 0, 0);
                p.setColor(ContextCompat.getColor(MyApplication.getContext(), R.color.swipeBehindBackground));

                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), p);

                // Set the image icon for Right swipe
                c.drawBitmap(icon,
                        (float) itemView.getLeft() + convertDpToPx(16),
                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                        p);
            } else {
                final int paintAlpha = (int) (Math.abs(dX) / (float) viewHolder.itemView.getWidth() * PAINT_ALPHA_FULL);
                icon = getBitmapFromVectorDrawable(MyApplication.getContext(), R.drawable.ic_reset_fab, paintAlpha);
                p.setAlpha(paintAlpha);
                /*icon = BitmapFactory.decodeResource(
                        MyApplication.getContext().getResources(), R.drawable.ic_reset_fab);*/

                /* Set your color for negative displacement */
                //p.setARGB(255, 0, 255, 0);
                p.setColor(ContextCompat.getColor(MyApplication.getContext(), R.color.swipeBehindBackground));

                // Draw Rect with varying left side, equal to the item's right side
                // plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), p);

                //Set the image icon for Left swipe
                c.drawBitmap(icon,
                        (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                        p);
            }

            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            //viewHolder.itemView.setTranslationX(dX);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int alpha) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(Objects.requireNonNull(drawable).getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (alpha > PAINT_ALPHA_FULL)
            alpha = PAINT_ALPHA_FULL;
            drawable.setAlpha(alpha);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private int convertDpToPx(int dp){
        return Math.round(dp * (MyApplication.getContext().getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public interface ActionCompletionContract {
        void onViewMoved(int oldPosition, int newPosition);

        void onViewSwiped(int position);
    }
}
