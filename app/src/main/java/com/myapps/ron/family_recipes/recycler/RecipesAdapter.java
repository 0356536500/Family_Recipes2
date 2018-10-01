package com.myapps.ron.family_recipes.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.StorageWrapper;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.MyCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ravi on 16/11/17.
 */

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Contact> recipeList;
    private List<Contact> recipeListFiltered;
    private RecipesAdapterListener listener;
    private StorageWrapper storageWrapper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;
        public ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            phone = view.findViewById(R.id.phone);
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


    public RecipesAdapter(Context context, List<Contact> recipeList, RecipesAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.recipeList = recipeList;
        this.recipeListFiltered = recipeList;
        this.storageWrapper = StorageWrapper.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Contact contact = recipeListFiltered.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());

        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(android.R.drawable.progress_indeterminate_horizontal);// R.drawable.ic_placeholder);
        requestOptions.error(android.R.drawable.stat_notify_error);// ic_error);

        //storageWrapper.getFoodFile(context, recipeListFiltered.get(position), );
        storageWrapper.getFoodFile(context, null, new MyCallback<String>() {
            @Override
            public void onFinished(String path) {
                Glide.with(context)
                        .load(Uri.fromFile(new File(path)))
                        .apply(requestOptions)
                        .into(holder.thumbnail);
            }
            //.apply(RequestOptions.circleCropTransform())
        });
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
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    recipeListFiltered = recipeList;
                } else {
                    List<Contact> filteredList = new ArrayList<>();
                    for (Contact row : recipeList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhone().contains(charSequence)) {
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
                recipeListFiltered = (ArrayList<Contact>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateRecipes(List<Recipe> list1) {
        List<Contact> list = new ArrayList<>();
        boolean changed = false;
        for(Contact item : list) {
            int index = recipeList.indexOf(item);
            if(index >= 0 && item.hashCode() != recipeList.get(index).hashCode()) {
                recipeList.set(index, item);
                changed = true;
            }
            else {
                recipeList.add(recipeList.size(), item);
                changed = true;
            }
        }

        if (changed)
            notifyDataSetChanged();
    }

    public interface RecipesAdapterListener {
        void onItemSelected(Contact contact);
    }
}
