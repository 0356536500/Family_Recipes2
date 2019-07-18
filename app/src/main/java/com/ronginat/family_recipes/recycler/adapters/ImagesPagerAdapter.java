package com.ronginat.family_recipes.recycler.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.model.RecipeEntity;

import java.util.List;

/**
 * Created by ronginat on 24/10/2018.
 */
public class ImagesPagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> images;

    public ImagesPagerAdapter(Context context, List<String> foodFiles) {
        this.context = context;
        this.images = foodFiles;
    }


    @NonNull @Override public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View item = LayoutInflater.from(container.getContext())
                .inflate(R.layout.pager_image_container, container, false);

        ImageView imageView = item.findViewById(R.id.item_image);

        loadImage(imageView, position);

        container.addView(item);
        return item;
    }

    @Override public int getCount() {
        if(images != null)
            return images.size();
        return 0;
    }

    @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    private void loadImage(final ImageView view, final int position) {
        if (images != null && position < images.size()) {
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
            circularProgressDrawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
            circularProgressDrawable.setStrokeWidth(10f);
            circularProgressDrawable.setCenterRadius(75f);
            circularProgressDrawable.start();
            view.setImageDrawable(circularProgressDrawable);

            StorageWrapper.getFoodFile(context, images.get(position), path -> {
                if(path != null && path.getPath() != null) {

                    Glide.with(context)
                            .load(path)
                            .placeholder(circularProgressDrawable)
                            .into(view);
                }
                else
                    loadDefaultImage(view, circularProgressDrawable);
            });
        }
    }

    private void loadDefaultImage(@NonNull final ImageView view, @NonNull Drawable placeholder) {

        Glide.with(context)
                .load(RecipeEntity.image)
                .placeholder(placeholder)
                .into(view);
    }
}
