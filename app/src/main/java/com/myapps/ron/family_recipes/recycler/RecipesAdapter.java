package com.myapps.ron.family_recipes.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
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


public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Recipe> recipeList;
    private List<Recipe> recipeListFiltered;
    private List<String> tags; // filters the user chose

    private String mLastQuery = "";
    private RecipesAdapterListener listener;
    private StorageWrapper storageWrapper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description;
        public ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            description = view.findViewById(R.id.description);
            thumbnail = view.findViewById(R.id.thumbnail);

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
        holder.name.setText(recipe.getName());
        holder.description.setText(recipe.getDescription());

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
                String charString = mLastQuery;
                if(charSequence != null){
                    charString = charSequence.toString();
                    mLastQuery = charString;
                }
                if (charString.isEmpty() && tags.isEmpty()) {
                    recipeListFiltered = recipeList;
                } else {
                    List<Recipe> filteredList = new ArrayList<>();
                    for (Recipe row : recipeList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or description number match
                        if ((row.getName().toLowerCase().contains(charString.toLowerCase())
                                || row.getDescription().contains(charString))
                                && row.hasTags(tags)) {
                            filteredList.add(row);
                        }
                    }

                    recipeListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = recipeListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                recipeListFiltered = (ArrayList<Recipe>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public List<Recipe> getCurrentList() {
        return recipeListFiltered;
    }
    public void updateTags(List<String> newTags) {
        tags.clear();
        if (newTags != null)
            tags.addAll(newTags);
        getFilter().filter(null);
    }

    public void updateRecipes(List<Recipe> list) {
        //boolean changed = false;
        for(Recipe item : list) {
            int index = recipeList.indexOf(item);
            if(index >= 0)// && item.hashCode() != recipeList.get(index).hashCode()) {
                recipeList.set(index, item);
            else
                recipeList.add(recipeList.size(), item);
        }

        notifyDataSetChanged();
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
