package com.myapps.ron.family_recipes.recycler.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.recycler.helpers.RecipesAdapterHelper;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class RecipesAdapter extends PagedListAdapter<RecipeMinimal, RecipesAdapter.MyViewHolder> {
    private Context context;

    private List<CategoryEntity> categoryList;

    private RecipesAdapterListener listener;
    private Animation scaleAnimation;
    @ColorInt
    private int circularColor;

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.uploader)
        TextView uploader;
        @BindView(R.id.number_of_likes)
        TextView numberOfLikes;
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.categories_scroll_container)
        HorizontalScrollView horizontalScrollView;
        @BindView(R.id.favorite_button)
        ToggleButton favoriteToggleButton;

        MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(view ->
                    listener.onItemSelected(getItem(getAdapterPosition())));

            ButterKnife.bind(this, itemView);
            /*name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            uploader = itemView.findViewById(R.id.uploader);
            numberOfLikes = itemView.findViewById(R.id.number_of_likes);
            horizontalScrollView = itemView.findViewById(R.id.categories_scroll_container);
            favoriteToggleButton = itemView.findViewById(R.id.favorite_toggle_button);


            thumbnail.setOnClickListener(view ->
                    listener.onImageClicked(getItem((getAdapterPosition()))));

            favoriteToggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if (compoundButton.isPressed()) {
                    // not when programmatically changing checked value
                    //listener.onFavoriteCheckedChanged(getItem(getAdapterPosition()));
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                    circularProgressDrawable.setStrokeWidth(4f);
                    circularProgressDrawable.setCenterRadius(25f);
                    circularProgressDrawable.start();
                    compoundButton.setBackground(circularProgressDrawable);
                    new Handler().postDelayed(() -> {
                        compoundButton.setBackgroundResource(R.drawable.favorite_selector);
                        compoundButton.startAnimation(scaleAnimation);
                        }, 2000);
                    Log.e(getClass().getSimpleName(), "checked changed, " + Boolean.toString(b));
                }
            });*/
        }

        @SuppressWarnings("UnusedParameters")
        @OnClick(R.id.thumbnail)
        void onClickThumbnailListener(View view) {
            listener.onImageClicked(getItem(getAdapterPosition()));
        }

        @OnCheckedChanged(R.id.favorite_button)
        void onCheckedChangedFavoriteListener(CompoundButton compoundButton, boolean b) {
            if (compoundButton.isPressed()) {
                // not when programmatically changing checked value
                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                circularProgressDrawable.setColorFilter(circularColor, PorterDuff.Mode.SRC_ATOP);
                circularProgressDrawable.setStrokeWidth(5f);
                circularProgressDrawable.setCenterRadius(25f);
                circularProgressDrawable.start();
                compoundButton.setBackground(circularProgressDrawable);
                compoundButton.setEnabled(false);

                listener.onFavoriteClicked(getItem(getAdapterPosition()));
                new Handler().postDelayed(() -> {
                    compoundButton.setBackgroundResource(R.drawable.favorite_selector);
                    compoundButton.startAnimation(scaleAnimation);
                    compoundButton.setEnabled(true);

                }, 1500);

                Log.e(getClass().getSimpleName(), "checked changed, " + Boolean.toString(b));
            }
        }

        void bindTo(RecipeMinimal recipe) {
            this.name.setText(recipe.getName() != null ?
                    recipe.getName() :
                    com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_NAME);

            this.description.setText(recipe.getDescription() != null ?
                    recipe.getDescription() :
                    com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_DESC);

            this.uploader.setText(recipe.getUploader() != null ?
                    recipe.getUploader() :
                    com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_UPLOADER);

            this.numberOfLikes.setText(String.valueOf(recipe.getLikes()));
            this.favoriteToggleButton.setBackgroundResource(R.drawable.favorite_selector);
            this.favoriteToggleButton.setChecked(recipe.getMeLike() == Constants.TRUE);
            this.favoriteToggleButton.setEnabled(true);

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

        this.scaleAnimation = new ScaleAnimation(0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.textColorMain, typedValue, true);
        circularColor = typedValue.data;
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
                            .load(path)
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
        void onFavoriteClicked(RecipeMinimal recipe);
        void onItemSelected(RecipeMinimal recipe);
        void onImageClicked(RecipeMinimal recipe);
        void onCurrentSizeChanged(int size);
    }
}
