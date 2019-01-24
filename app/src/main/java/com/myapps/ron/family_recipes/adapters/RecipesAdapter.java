package com.myapps.ron.family_recipes.adapters;

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
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.recycler.RecipesAdapterHelper;
import com.myapps.ron.family_recipes.utils.GlideApp;

import java.io.File;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;


public class RecipesAdapter extends PagedListAdapter<RecipeMinimal, RecipesAdapter.MyViewHolder> {
    private int[] colors;
    private Random random;
    private Context context;

    private List<CategoryEntity> categoryList;

    private RecipesAdapterListener listener;
    //private StorageWrapper storageWrapper;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, uploader, numberOfLikes;
        AppCompatImageView thumbnail;
        HorizontalScrollView horizontalScrollView;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            uploader = itemView.findViewById(R.id.uploader);
            numberOfLikes = itemView.findViewById(R.id.number_of_likes);
            horizontalScrollView = itemView.findViewById(R.id.categories_scroll_container);

            this.itemView.setTag(this);

            itemView.setOnClickListener(view ->
                    listener.onItemSelected(getItem(getAdapterPosition())));

            thumbnail.setOnClickListener(view ->
                    listener.onImageClicked(getItem((getAdapterPosition()))));

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
            if (categoryList != null)
                inflateCategories(this, recipe);
            //inflateCategories(this, recipe);

            //load image of the food or default if not exists
            loadImage(this, recipe);
        }
    }


    public RecipesAdapter(Context context, RecipesAdapterListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;

        this.colors = context.getResources().getIntArray(R.array.colors);
        this.random = new Random();
    }

    public void setCategoryList(List<CategoryEntity> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_recipe, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final RecipeMinimal recipe = getItem(position);
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

    private void inflateCategories(MyViewHolder holder, RecipeMinimal recipe) {
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

    private void loadImage(final MyViewHolder holder, final RecipeMinimal recipe) {
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

    private int pickColor() {
        return colors[random.nextInt(colors.length)];
    }

    private void loadDefaultImage(final MyViewHolder holder) {
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
                .into(holder.thumbnail);
    }

    private int lastSize = 0;
    @Override
    public int getItemCount() {
        if (getCurrentList() == null)
            return 0;
        if (lastSize != getCurrentList().size()) {
            lastSize = getCurrentList().size();
            listener.onCurrentSizeChanged(lastSize);
        }
        return getCurrentList().size();
    }

    private static DiffUtil.ItemCallback<RecipeMinimal> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RecipeMinimal>() {
                // Recipe details may have changed if reloaded from the database,
                // but ID is fixed.
                @Override
                public boolean areItemsTheSame(@NonNull RecipeMinimal oldRecipe,
                                               @NonNull RecipeMinimal newRecipe) {
                    return oldRecipe.getId().equals(newRecipe.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull RecipeMinimal oldRecipe,
                                                  @NonNull RecipeMinimal newRecipe) {
                    return oldRecipe.equals(newRecipe);
                }
            };


    public interface RecipesAdapterListener {
        void onItemSelected(RecipeMinimal recipe);
        void onImageClicked(RecipeMinimal recipe);
        void onCurrentSizeChanged(int size);
    }
}
