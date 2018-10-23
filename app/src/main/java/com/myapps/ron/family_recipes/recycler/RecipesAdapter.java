package com.myapps.ron.family_recipes.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
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
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MyCallback;
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
    private List<Recipe> recipeList;
    private List<Recipe> recipeListFiltered;
    private List<String> tags; // filters the user chose

    private String mLastQuery = "";
    private RecipesAdapterListener listener;
    private StorageWrapper storageWrapper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description, uploader, numberOfLikes;
        public AppCompatImageView thumbnail;
        public HorizontalScrollView horizontalScrollView;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            description = view.findViewById(R.id.description);
            thumbnail = view.findViewById(R.id.thumbnail);
            uploader = view.findViewById(R.id.uploader);
            numberOfLikes = view.findViewById(R.id.number_of_likes);
            horizontalScrollView = view.findViewById(R.id.categories_scroll_container);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onItemSelected(recipeListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }


    public RecipesAdapter(Context context, List<Recipe> recipeList, RecipesAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.recipeList = recipeList;
        this.recipeListFiltered = recipeList;
        this.tags = new ArrayList<>();
        this.storageWrapper = StorageWrapper.getInstance(context);
        this.colors = context.getResources().getIntArray(R.array.colors);
        this.random = new Random();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Recipe recipe = recipeListFiltered.get(position);

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
        if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
            /*LinearLayout internalWrapper = new LinearLayout(context);
            internalWrapper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            internalWrapper.setOrientation(LinearLayout.HORIZONTAL);
            internalWrapper.setGravity(Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                internalWrapper.setForegroundGravity(Gravity.CENTER);
            }*/

            //only child of the scroll view is a linear layout containing all the views
            LinearLayout internalWrapper = holder.horizontalScrollView.findViewById(R.id.categories_layout_container);

            //margins of every view in linear layout
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginLayoutParams.setMarginStart(6);
            marginLayoutParams.setMarginEnd(6);

            for (String category: recipe.getCategories()) {
                View view = LayoutInflater.from(context).inflate(R.layout.category_item_layout, internalWrapper, false);
                //view.setLayoutParams(marginLayoutParams);
                ((TextView) view.findViewById(R.id.category_text)).setText(category);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.setForegroundGravity(Gravity.CENTER);
                }
                int color = pickColor();
                view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                ((GradientDrawable)view.getBackground()).setStroke(5, Color.BLACK);
                //((GradientDrawable)view.getBackground()).setStroke(5, color);
                //((TextView) view.findViewById(R.id.category_text)).setTextColor(color);
                internalWrapper.addView(view, marginLayoutParams);
            }

            holder.horizontalScrollView.removeAllViews();
            holder.horizontalScrollView.addView(internalWrapper);
        }

        /*final RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(circularProgressDrawable);
        //requestOptions.placeholder(android.R.drawable.progress_indeterminate_horizontal);// R.drawable.ic_placeholder);
        requestOptions.error(android.R.drawable.stat_notify_error);// ic_error);*/


        if(recipe.getFoodFiles() != null && recipe.getFoodFiles().size() > 0) {
            storageWrapper.getFoodFile(context, recipe, Constants.FOOD_DIR, new MyCallback<String>() {
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
                    loadDefaultImage(recipe, holder);
            }
            //.apply(RequestOptions.circleCropTransform())
        });
        }
        else {
            loadDefaultImage(recipe, holder);
        }
        //storageWrapper.getFoodFile(context, recipeListFiltered.get(position), );

    }

    private int pickColor() {
        return colors[random.nextInt(colors.length)];
    }

    private void loadDefaultImage(Recipe recipe, final MyViewHolder holder) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(35f);
        circularProgressDrawable.start();

        GlideApp.with(context)
                .load(recipe.image)
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
                List<Recipe> filteredList;
                String charString = mLastQuery;
                if(charSequence != null){
                    charString = charSequence.toString();
                    mLastQuery = charString;
                }
                if (charString.isEmpty() && (tags == null || tags.isEmpty())) {
                    filteredList = new ArrayList<>(recipeList);
                } else {
                    filteredList = new ArrayList<>();
                    for (Recipe row : recipeList) {

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
                Log.e("adapter", "update from filter");
                if(filterResults.values != null)
                    updateRecipes((ArrayList<Recipe>) filterResults.values);
                //notifyDataSetChanged();
            }
        };
    }

    public List<Recipe> getCurrentList() {
        return recipeListFiltered;
    }
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

    public void updateRecipes(List<Recipe> list) {
        if(this.recipeListFiltered == null || this.recipeListFiltered.isEmpty()){
            this.recipeListFiltered = new ArrayList<>(list);
            notifyDataSetChanged();
        }
        else {
            List<Recipe> oldTemp = recipeListFiltered;
            recipeListFiltered = list;
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(oldTemp, list));
            diffResult.dispatchUpdatesTo(this);
        }
        //boolean changed = false;
        /*for(Recipe item : list) {
            int index = recipeList.indexOf(item);
            if(index >= 0)// && item.hashCode() == recipeList.get(index).hashCode() && item.identical(recipeList.get(index)))
                recipeList.set(index, item);
            else
                recipeList.add(recipeList.size(), item);
        }*/

        //notifyDataSetChanged();
    }

    public void updateOneRecipe(Recipe recipe) {
        int index1 = recipeList.indexOf(recipe);
        if(index1 >= 0)
            recipeList.set(index1, recipe);

        int index2 = recipeListFiltered.indexOf(recipe);
        if(index2 >= 0)
            recipeListFiltered.set(index2, recipe);
        notifyDataSetChanged();

        //return index1 >= 0 && index2 >= 0;
    }

    public interface RecipesAdapterListener {
        void onItemSelected(Recipe recipe);
    }
}
