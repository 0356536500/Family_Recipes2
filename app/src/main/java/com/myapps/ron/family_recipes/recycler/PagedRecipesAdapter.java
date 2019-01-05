package com.myapps.ron.family_recipes.recycler;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.RecipesAdapter;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.utils.MyCallback;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

/**
 * Created by ronginat on 03/01/2019.
 */
public class PagedRecipesAdapter extends PagedListAdapter<RecipeMinimal, PagedRecipesAdapter.SimpleViewHolder> {

    private List<CategoryEntity> categoryList;
    private RecipesAdapter.RecipesAdapterListener listener;
    private Context context;

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.row_simple_name);
        }

        void bindTo(RecipeMinimal recipe) {
            this.name.setText(recipe.getName());
        }
    }

    class PagedViewHolder extends RecyclerView.ViewHolder {

        TextView name, description, uploader, numberOfLikes;
        AppCompatImageView thumbnail;
        HorizontalScrollView horizontalScrollView;

        PagedViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            uploader = itemView.findViewById(R.id.uploader);
            numberOfLikes = itemView.findViewById(R.id.number_of_likes);
            horizontalScrollView = itemView.findViewById(R.id.categories_scroll_container);

            this.itemView.setTag(this);

            itemView.setOnClickListener(view -> {
                // send selected contact in callback
                //listener.onItemSelected(recipeListFiltered.get(getAdapterPosition()));
            });

            thumbnail.setOnClickListener(view -> {
                //listener.onImageClicked(recipeListFiltered.get(getAdapterPosition()));
            });

        }

        void bindTo(RecipeMinimal recipe) {
            if (recipe.getName() != null)
                this.name.setText(recipe.getName());
            else
                this.name.setText(com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_NAME);

            if (recipe.getDescription() != null)
                this.description.setText(recipe.getDescription());
            else
                this.description.setText(com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_DESC);

            if (recipe.getUploader() != null)
                this.uploader.setText(recipe.getUploader());
            else
                this.uploader.setText(com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_UPLOADER);

            this.numberOfLikes.setText(String.valueOf(recipe.getLikes()));

            //inflate categories
            //inflateCategories(this, recipe);

            //load image of the food or default if not exists
            loadImage(this, recipe);
        }

        public void clear() {
            horizontalScrollView.removeAllViews();
        }
    }

    public PagedRecipesAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_simple, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
        RecipeMinimal recipe = getItem(position);
        if (recipe != null) {
            holder.bindTo(recipe);
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically
            // invalidates this row when the actual object is loaded from the
            // database.
            Log.e(getClass().getSimpleName(), "bind with null object");
            //holder.clear();
        }
    }


    /*@Override
    public void onBindViewHolder(@NonNull PagedViewHolder holder, int position) {
        RecipeMinimal recipe = getItem(position);
        if (recipe != null) {
            holder.bindTo(recipe);
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically
            // invalidates this row when the actual object is loaded from the
            // database.
            holder.clear();
        }
    }*/

    private static DiffUtil.ItemCallback<RecipeMinimal> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RecipeMinimal>() {
                // Recipe details may have changed if reloaded from the database,
                // but ID is fixed.
                @Override
                public boolean areItemsTheSame(RecipeMinimal oldRecipe, RecipeMinimal newRecipe) {
                    return oldRecipe.getId().equals(newRecipe.getId());
                }

                @Override
                public boolean areContentsTheSame(RecipeMinimal oldRecipe,
                                                  RecipeMinimal newRecipe) {
                    return oldRecipe.equals(newRecipe);
                }
            };

    private void inflateCategories(PagedViewHolder holder, RecipeMinimal recipe) {
        if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
            /*LinearLayout internalWrapper = new LinearLayout(context);
            internalWrapper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            internalWrapper.setOrientation(LinearLayout.HORIZONTAL);*/

            //only child of the scroll view is a linear layout containing all the views
            LinearLayout internalWrapper = holder.horizontalScrollView.findViewById(R.id.categories_layout_container);
            internalWrapper.removeAllViews();

            //margins of every view in linear layout
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginLayoutParams.setMarginStart(6);
            marginLayoutParams.setMarginEnd(6);

            for (String category: recipe.getCategories()) {
                View view = LayoutInflater.from(context).inflate(R.layout.category_item_layout, internalWrapper, false);
                //view.setLayoutParams(marginLayoutParams);
                ((TextView) view.findViewById(R.id.category_text)).setText(category);
                //view.findViewById(R.id.category_text).getBackground().setTint(pickColor());
                view.findViewById(R.id.category_text).getBackground().setColorFilter(RecipesAdapterHelper.getCategoryColorByName(categoryList, category), PorterDuff.Mode.SRC_ATOP);
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.setForegroundGravity(Gravity.CENTER);
                }*/

                //view.getBackground().setColorFilter(pickColor(), PorterDuff.Mode.SRC_ATOP);
                //((GradientDrawable)view.getBackground()).setStroke(5, Color.BLACK);
                //((TextView) view.findViewById(R.id.category_text)).setTextColor(color);
                internalWrapper.addView(view, marginLayoutParams);
            }

            holder.horizontalScrollView.removeAllViews();
            holder.horizontalScrollView.addView(internalWrapper);
        }
    }

    private void loadImage(final PagedViewHolder holder, final RecipeMinimal recipe) {
        if(recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > 0) {
            //.apply(RequestOptions.circleCropTransform())
            StorageWrapper.getThumbFile(context, recipe.getFoodFiles().get(0), path -> {
                if(path != null) {
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                    circularProgressDrawable.setStrokeWidth(5f);
                    circularProgressDrawable.setCenterRadius(35f);
                    circularProgressDrawable.start();

                    GlideApp.with(context)
                            .load(Uri.fromFile(new File(path)))
                            .placeholder(circularProgressDrawable)
                            //.apply(requestOptions)
                            .into(holder.thumbnail);
                }
                else
                    loadDefaultImage(holder);
            });
        }
        else {
            loadDefaultImage(holder);
        }
    }

    private void loadDefaultImage(final PagedViewHolder holder) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(35f);
        circularProgressDrawable.start();

        GlideApp.with(context)
                .load(RecipeEntity.image)
                .placeholder(circularProgressDrawable)
                .into(holder.thumbnail);
    }

}
