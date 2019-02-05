package com.myapps.ron.family_recipes.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.utils.GlideApp;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by ronginat on 24/10/2018.
 */
public class MyPagerAdapter extends PagerAdapter {

    private Context context;
    private RecipeEntity recipe;

    public MyPagerAdapter(Context context, RecipeEntity recipe) {
        this.context = context;
        this.recipe = recipe;
    }


    @NonNull @Override public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View item = LayoutInflater.from(container.getContext())
                .inflate(R.layout.pager_image_container, container, false);

        AppCompatImageView imageView = item.findViewById(R.id.item_image);

        loadImage(imageView, position);

        container.addView(item);
        return item;
    }

    @Override public int getCount() {
        if(recipe!= null && recipe.getFoodFiles() != null)
            return recipe.getFoodFiles().size();
        return 0;
    }

    @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    private void loadImage(final AppCompatImageView view, final int position) {
        if (recipe != null && recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > position) {

            //.apply(RequestOptions.circleCropTransform())
            StorageWrapper.getFoodFile(context, recipe.getFoodFiles().get(position), path -> {
                if(path != null) {
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                    circularProgressDrawable.setStrokeWidth(5f);
                    circularProgressDrawable.setCenterRadius(35f);
                    circularProgressDrawable.start();

                    File file = new File(path.getPath());
                    if (position > 0)
                        file.deleteOnExit();

                    GlideApp.with(context)
                            .load(Uri.fromFile(file))
                            .placeholder(circularProgressDrawable)
                            //.apply(requestOptions)
                            .into(view);
                }
                else
                    loadDefaultImage(view);
            });
        }
        else {
            loadDefaultImage(view);
        }
    }

    private void loadDefaultImage(final AppCompatImageView view) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(35f);
        circularProgressDrawable.start();

        GlideApp.with(context)
                .load(RecipeEntity.image)
                .placeholder(circularProgressDrawable)
                /*.listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.thumbnail.setImageResource(android.R.drawable.stat_notify_error);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })*/
                //.apply(requestOptions)
                .into(view);
    }
}
