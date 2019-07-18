package com.ronginat.family_recipes.recycler.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.chip.Chip;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.model.CategoryEntity;
import com.ronginat.family_recipes.model.RecipeEntity;
import com.ronginat.family_recipes.model.RecipeMinimal;
import com.ronginat.family_recipes.recycler.helpers.RecipesAdapterHelper;
import com.ronginat.family_recipes.utils.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


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
                circularProgressDrawable.setColorFilter(new PorterDuffColorFilter(circularColor, PorterDuff.Mode.SRC_ATOP));
                circularProgressDrawable.setStrokeWidth(5f);
                circularProgressDrawable.setCenterRadius(25f);
                circularProgressDrawable.start();
                compoundButton.setBackground(circularProgressDrawable);
                compoundButton.setEnabled(false);

                listener.onFavoriteClicked(getItem(getAdapterPosition()), () ->
                        new Handler().postDelayed(() -> {
                            compoundButton.setChecked(!b);
                            compoundButton.setBackgroundResource(R.drawable.favorite_selector);
                            compoundButton.startAnimation(scaleAnimation);
                            compoundButton.setEnabled(true);
                        }, Constants.SCALE_ANIMATION_DURATION));
            }
        }

        void bindTo(RecipeMinimal recipe) {
            this.name.setText(recipe.getName() != null ?
                    recipe.getName() :
                    com.ronginat.family_recipes.utils.Constants.DEFAULT_RECIPE_NAME);

            this.description.setText(recipe.getDescription() != null ?
                    recipe.getDescription() :
                    com.ronginat.family_recipes.utils.Constants.DEFAULT_RECIPE_DESC);

            /*this.uploader.setText(recipe.getUploader() != null ?
                    recipe.getUploader() :
                    com.myapps.family_recipes.utils.Constants.DEFAULT_RECIPE_UPLOADER);*/

            if (recipe.getUploader() != null) {
                listener.getDisplayedName(recipe.getUploader())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<String>() {
                            @Override
                            public void onSuccess(String name) {
                                uploader.setText(name);
                                dispose();
                            }

                            @Override
                            public void onError(Throwable t) {
                                setDefaultUploader(uploader);
                            }
                        });
            } else
                setDefaultUploader(this.uploader);

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

    private void setDefaultUploader(TextView uploader) {
        uploader.setText(com.ronginat.family_recipes.utils.Constants.DEFAULT_RECIPE_UPLOADER);
    }


    public RecipesAdapter(Context context, RecipesAdapterListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;

        this.scaleAnimation = new ScaleAnimation(0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(Constants.SCALE_ANIMATION_DURATION);

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
            //Log.e(getClass().getSimpleName(), "bind with null object");
            Spannable spannable = Spannable.Factory.getInstance().newSpannable("Not exists!");
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, spannable.length(), 0);
            holder.name.setText(spannable , TextView.BufferType.SPANNABLE);
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
                Chip chip = view.findViewById(R.id.category_text);
                chip.setText(category);
                chip.setChipBackgroundColor(ColorStateList.valueOf(RecipesAdapterHelper.getCategoryColorByName(categoryList, category)));
                /*((TextView) view.findViewById(R.id.category_text)).setText(category);
                view.findViewById(R.id.category_text).getBackground().setTint(RecipesAdapterHelper.getCategoryColorByName(categoryList, category));*/

                //view.findViewById(R.id.category_text).getBackground().setColorFilter(RecipesAdapterHelper.getCategoryColorByName(categoryList, category), PorterDuff.Mode.SRC_ATOP);

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
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(35f);
        circularProgressDrawable.start();
        holder.thumbnail.setImageDrawable(circularProgressDrawable);

        if(recipe.getThumbnail() != null) {
            //.apply(RequestOptions.circleCropTransform())
            StorageWrapper.getThumbFile(context, recipe.getThumbnail())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<Uri>() {
                        @Override
                        public void onSuccess(Uri path) {
                            listener.onThumbnailAccessed(recipe.getId());
                            if(path != null) {
                                Glide.with(context)
                                        .load(path)
                                        .placeholder(circularProgressDrawable)
                                        .transform(new RoundedCorners(50))// TODO: change to constant
                                        //.optionalCircleCrop()
                                        .into(holder.thumbnail);
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(getClass().getSimpleName(), "error from storage, ", throwable);
                            loadDefaultImage(holder, circularProgressDrawable);
                            dispose();
                        }
                    });
        } else
            loadDefaultImage(holder, circularProgressDrawable);
    }

    private void loadDefaultImage(@NonNull final MyViewHolder holder, @NonNull Drawable placeholder) {
        Glide.with(context)
                .load(RecipeEntity.image)
                .placeholder(placeholder)
                .optionalCircleCrop()
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
        void onFavoriteClicked(RecipeMinimal recipe, Runnable onError);
        void onItemSelected(RecipeMinimal recipe);
        void onImageClicked(RecipeMinimal recipe);
        void onThumbnailAccessed(String id);
        void onCurrentSizeChanged(int size);
        Single<String> getDisplayedName(String username);
    }
}
