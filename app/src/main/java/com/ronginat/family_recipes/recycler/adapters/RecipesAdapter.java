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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import com.ronginat.family_recipes.model.RecipeMinimal;
import com.ronginat.family_recipes.recycler.helpers.RecipesAdapterHelper;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

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
    private int thumbnailSize, thumbnailDefaultSize;
    private final int thumbCorners;

    private List<CategoryEntity> categoryList;

    private RecipesAdapterHelper categoriesHelper;
    private RecipesAdapterListener listener;
    private Animation scaleAnimation;
    @ColorInt
    private int circularColor;

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.author)
        TextView author;
        @BindView(R.id.number_of_likes)
        TextView numberOfLikes;
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.categories_layout_container)
        ViewGroup categoriesLayout; // flexbox
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

            /*this.uploader.setText(recipe.getAuthor() != null ?
                    recipe.getAuthor() :
                    com.myapps.family_recipes.utils.Constants.DEFAULT_RECIPE_AUTHOR);*/

            if (recipe.getAuthor() != null) {
                listener.getDisplayedName(recipe.getAuthor())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<String>() {
                            @Override
                            public void onSuccess(String name) {
                                author.setText(name);
                                dispose();
                            }

                            @Override
                            public void onError(Throwable t) {
                                setDefaultAuthor(author);
                            }
                        });
            } else
                setDefaultAuthor(this.author);

            this.numberOfLikes.setText(String.valueOf(recipe.getLikes()));
            this.favoriteToggleButton.setBackgroundResource(R.drawable.favorite_selector);
            this.favoriteToggleButton.setChecked(recipe.getMeLike() == Constants.TRUE);
            this.favoriteToggleButton.setEnabled(true);

            //inflate categories
            if (categoryList != null)
                inflateCategories(this, recipe);
            // hide previous categories
            else
                prepareCategoriesLayout(this, recipe);

            //load image of the food or default if not exists
            loadImage(this, recipe);
        }
    }

    private void setDefaultAuthor(TextView uploader) {
        uploader.setText(com.ronginat.family_recipes.utils.Constants.DEFAULT_RECIPE_AUTHOR);
    }


    public RecipesAdapter(Context context, RecipesAdapterListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
        this.categoriesHelper = new RecipesAdapterHelper();

        this.scaleAnimation = new ScaleAnimation(0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(Constants.SCALE_ANIMATION_DURATION);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.textColorMain, typedValue, true);
        circularColor = typedValue.data;
        this.thumbnailSize = (int) this.context.getResources().getDimension(R.dimen.thumbnail);
        this.thumbnailDefaultSize = (int) this.context.getResources().getDimension(R.dimen.thumbnail_default);
        this.thumbCorners = (int) (this.context.getResources().getDimension(R.dimen.thumbnail_corner_radius) /
                this.context.getResources().getDisplayMetrics().density);
    }

    public void setCategoryList(List<CategoryEntity> categoryList) {
        this.categoryList = categoryList;
        this.categoriesHelper.setCategories(categoryList);
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
            Spannable spannable = Spannable.Factory.getInstance().newSpannable("Not exists!");
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, spannable.length(), 0);
            holder.name.setText(spannable , TextView.BufferType.SPANNABLE);
            //holder.clear();
        }
    }

    private void inflateCategories(MyViewHolder holder, RecipeMinimal recipe) {
        if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
            prepareCategoriesLayout(holder, recipe);

            int i = 0;
            for (String category: recipe.getCategories()) {
                View view = holder.categoriesLayout.getChildAt(i++);
                // show view
                view.setVisibility(View.VISIBLE);
                Chip chip = view.findViewById(R.id.category_text);
                chip.setText(category);
                chip.setChipBackgroundColor(ColorStateList.valueOf(categoriesHelper.getCategoryColor(category)));
            }
        }
    }

    /**
     * Do as little changes as possible to the categories layout, to avoid redundant inflation
     */
    private void prepareCategoriesLayout(MyViewHolder holder, RecipeMinimal recipe) {
        int numberOfCategories = recipe.getCategories() != null ? recipe.getCategories().size() : 0;
        int currentNumOfViews = holder.categoriesLayout.getChildCount();
        // hide redundant views
        if (currentNumOfViews > numberOfCategories) {
            //holder.categoriesLayout.removeViewsInLayout(numberOfCategories, currentNumOfViews - numberOfCategories);
            for (int i = numberOfCategories; i < currentNumOfViews; i++)
                holder.categoriesLayout.getChildAt(i).setVisibility(View.GONE);
        }
        // inflate missing views
        else if (numberOfCategories > currentNumOfViews) {
            //margins of every view in the flexbox
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginLayoutParams.setMarginStart(6);
            marginLayoutParams.setMarginEnd(6);

            for (int i = 0; i < numberOfCategories - currentNumOfViews; i++) {
                View view = LayoutInflater.from(context).inflate(R.layout.category_item_layout, holder.categoriesLayout, false);
                holder.categoriesLayout.addView(view, marginLayoutParams);
            }
        }

    }

    private void loadImage(final MyViewHolder holder, final RecipeMinimal recipe) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(35f);
        circularProgressDrawable.start();
        holder.thumbnail.setImageDrawable(circularProgressDrawable);

        if(recipe.getThumbnail() != null) {
            setThumbnailSize(holder, thumbnailSize);
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
                                        //.apply(new RequestOptions().override((int) context.getResources().getDimension(R.dimen.thumbnail)))
                                        .transform(new RoundedCorners(thumbCorners))
                                        //.optionalCircleCrop()
                                        .error(R.drawable.food_default_small)
                                        .into(holder.thumbnail);
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            CrashLogger.e(getClass().getSimpleName(), "error from storage, ", throwable);
                            loadDefaultImage(holder, circularProgressDrawable);
                            dispose();
                        }
                    });
        } else
            loadDefaultImage(holder, circularProgressDrawable);
    }

    private void loadDefaultImage(@NonNull final MyViewHolder holder, @NonNull Drawable placeholder) {
        setThumbnailSize(holder, thumbnailDefaultSize);
        Glide.with(context)
                .load(R.drawable.food_default_small)
                .placeholder(placeholder)
                //.apply(new RequestOptions().override((int) context.getResources().getDimension(R.dimen.thumbnail_default)))
                //.optionalCircleCrop()
                .into(holder.thumbnail);
    }

    private void setThumbnailSize(MyViewHolder holder, int size) {
        ViewGroup.LayoutParams lp = holder.thumbnail.getLayoutParams();
        if (lp.width != size) {
            lp.width = size;
            lp.height = size;
            //holder.thumbnail.requestLayout();
        }
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
