package com.myapps.ron.family_recipes.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.utils.GlideApp;

import java.io.File;

/**
 * Created by ronginat on 24/10/2018.
 */
public class MyPagerAdapter extends PagerAdapter {

    private Context context;
    private StorageWrapper storageWrapper;
    private Recipe recipe;

    public MyPagerAdapter(Context context, Recipe recipe) {
        this.context = context;
        this.recipe = recipe;
        this.storageWrapper = StorageWrapper.getInstance(context);
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


    private void loadImage(final AppCompatImageView view, int position) {
        if (recipe != null && recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > position) {

            storageWrapper.getFoodFile(context, recipe, position, Constants.FOOD_DIR, new MyCallback<String>() {
                @Override
                public void onFinished(String path) {
                    if(path != null) {
                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                        circularProgressDrawable.setStrokeWidth(5f);
                        circularProgressDrawable.setCenterRadius(35f);
                        circularProgressDrawable.start();

                        GlideApp.with(context)
                                .load(Uri.fromFile(new File(path)))
                                .placeholder(circularProgressDrawable)
                                //.apply(requestOptions)
                                .into(view);
                    }
                    else
                        loadDefaultImage(view);
                }
                //.apply(RequestOptions.circleCropTransform())
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
                .load(Recipe.image)
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
