package com.myapps.ron.family_recipes.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.recycler.RecipesAdapterHelper;
import com.myapps.ron.family_recipes.recycler.RecipesDiffCallback;
import com.myapps.ron.family_recipes.utils.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.MyViewHolder>
        implements Filterable {
    private int[] colors;
    private Random random;
    private Context context;
    private List<RecipeEntity> recipeList;
    private List<RecipeEntity> recipeListFiltered;
    private List<String> tags; // filters the user chose

    private List<Category> categoryList;

    private String mLastQuery = "";
    private RecipesAdapterListener listener;
    //private StorageWrapper storageWrapper;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, uploader, numberOfLikes;
        AppCompatImageView thumbnail;
        HorizontalScrollView horizontalScrollView;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            description = view.findViewById(R.id.description);
            thumbnail = view.findViewById(R.id.thumbnail);
            uploader = view.findViewById(R.id.uploader);
            numberOfLikes = view.findViewById(R.id.number_of_likes);
            horizontalScrollView = view.findViewById(R.id.categories_scroll_container);

            itemView.setTag(this);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onItemSelected(recipeListFiltered.get(getAdapterPosition()));
                }
            });

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onImageClicked(recipeListFiltered.get(getAdapterPosition()));
                }
            });

        }
    }


    public RecipesAdapter(Context context, List<RecipeEntity> recipeList, List<Category> categoryList, RecipesAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.categoryList = categoryList;
        this.recipeList = recipeList;
        this.recipeListFiltered = recipeList;
        this.tags = new ArrayList<>();
        //this.storageWrapper = StorageWrapper.getInstance(context);
        this.colors = context.getResources().getIntArray(R.array.colors);
        this.random = new Random();
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
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
        final RecipeEntity recipe = recipeListFiltered.get(position);

        if (recipe.getName() != null)
            holder.name.setText(recipe.getName());
        else
            holder.name.setText(com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_NAME);

        if (recipe.getDescription() != null)
            holder.description.setText(recipe.getDescription());
        else
            holder.description.setText(com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_DESC);

        if (recipe.getUploader() != null)
            holder.uploader.setText(recipe.getUploader());
        else
            holder.uploader.setText(com.myapps.ron.family_recipes.utils.Constants.DEFAULT_RECIPE_UPLOADER);

        holder.numberOfLikes.setText(String.valueOf(recipe.getLikes()));

        //inflate categories
        inflateCategories(holder, recipe);

        //load image of the food or default if not exists
        loadImage(holder, recipe);


        /*final RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(circularProgressDrawable);
        //requestOptions.placeholder(android.R.drawable.progress_indeterminate_horizontal);// R.drawable.ic_placeholder);
        requestOptions.error(android.R.drawable.stat_notify_error);// ic_error);*/
    }

    private void inflateCategories(MyViewHolder holder, RecipeEntity recipe) {
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

    private void loadImage(final MyViewHolder holder, final RecipeEntity recipe) {
        if(recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > 0) {
            StorageWrapper.getThumbFile(context, recipe.getFoodFiles().get(0), new MyCallback<String>() {
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
                                .into(holder.thumbnail);
                    }
                    else
                        loadDefaultImage(holder);
                }
                //.apply(RequestOptions.circleCropTransform())
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

    @Override
    public int getItemCount() {
        return recipeListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<RecipeEntity> filteredList;
                String charString = mLastQuery;
                if(charSequence != null){
                    charString = charSequence.toString();
                    mLastQuery = charString;
                }
                if (charString.isEmpty() && (tags == null || tags.isEmpty())) {
                    filteredList = new ArrayList<>(recipeList);
                } else {
                    filteredList = new ArrayList<>();
                    for (RecipeEntity row : recipeList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or description number match
                        if ((row.getName().toLowerCase().contains(charString.toLowerCase())
                                || row.getDescription().contains(charString))
                                && row.hasTags(tags)) {
                            filteredList.add(row);
                        }
                    }

                    //recipeListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //Log.e("adapter", "update from filter");
                if(filterResults.values != null)
                    updateRecipes((ArrayList<RecipeEntity>) filterResults.values, false);
                //notifyDataSetChanged();
            }
        };
    }

/*
    public List<Recipe> getCurrentList() {
        return recipeListFiltered;
    }
*/

    public void updateTags(List<String> newTags) {
        //tags.clear();
        if (newTags != null)
            tags = new ArrayList<>(newTags);
        else
            tags = null;
            //tags.addAll(newTags);
        getFilter().filter(null);
        /*List<Recipe> filteredList = new ArrayList<>();
        for (Recipe row : recipeListFiltered) {
            if (row.hasTags(tags)) {
                filteredList.add(row);
            }
        }
        updateRecipes(filteredList);*/
    }

    public void updateRecipes(List<RecipeEntity> list, boolean addedRecipes) {
        if (list == null)
            return;
        Log.e(getClass().getSimpleName(), "update recipes, added = " + addedRecipes +"\n " + list.toString());
        if (this.recipeListFiltered == null || this.recipeListFiltered.isEmpty()){
            this.recipeListFiltered = new ArrayList<>(list);
            notifyDataSetChanged();

        } else {
            List<RecipeEntity> oldTemp;
            if (addedRecipes) {
                oldTemp = recipeList;
                recipeList = list;
                recipeListFiltered = recipeList;

            } else {
                oldTemp = recipeListFiltered;
                recipeListFiltered = list;
                listener.onCurrentSizeChanged(recipeListFiltered.size());
            }

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RecipesDiffCallback(oldTemp, list));
            diffResult.dispatchUpdatesTo(this);

            if (addedRecipes)
                getFilter().filter(null);
        }
    }

    public void updateRecipesOrder(List<RecipeEntity> list) {
        if (list == null)
            return;
        if (this.recipeListFiltered == null || this.recipeListFiltered.isEmpty()){
            this.recipeListFiltered = new ArrayList<>(list);
            notifyDataSetChanged();
        }
        else {
            List<RecipeEntity> oldTemp = recipeList;
            recipeList = list;
            recipeListFiltered = recipeList;

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RecipesDiffCallback(oldTemp, list));
            diffResult.dispatchUpdatesTo(this);

            getFilter().filter(null);
        }
    }

    public void updateOneRecipe(RecipeEntity recipe) {
        //List<Recipe> newList = new ArrayList<>(recipeList);

        int index = recipeList.indexOf(recipe);
        if(index >= 0) {
            recipeList.set(index, recipe);

            notifyItemChanged(index);
        }
        //updateRecipes(newList, true);
    }

    public interface RecipesAdapterListener {
        void onItemSelected(RecipeEntity recipe);
        void onImageClicked(RecipeEntity recipe);
        void onCurrentSizeChanged(int size);
    }
}
