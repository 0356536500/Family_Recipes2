package com.myapps.ron.family_recipes.recycler.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;

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
            circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            circularProgressDrawable.setStrokeWidth(10f);
            circularProgressDrawable.setCenterRadius(75f);
            circularProgressDrawable.start();
            view.setImageDrawable(circularProgressDrawable);

            StorageWrapper.getFoodFile(context, images.get(position), path -> {
                if(path != null) {

                    File file = new File(path.getPath());
                    if (position > 0)
                        file.deleteOnExit();

                    Glide.with(context)
                            .load(Uri.fromFile(file))
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
