package com.ronginat.family_recipes.recycler.helpers;

import androidx.recyclerview.widget.DiffUtil;

import com.ronginat.family_recipes.model.RecipeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 23/10/2018.
 */
public class RecipesDiffCallback extends DiffUtil.Callback {

    //private static final String TAG = RecipesDiffCallback.class.getSimpleName();
    private List<RecipeEntity> oldRecipes;
    private List<RecipeEntity> newRecipes;

    public RecipesDiffCallback(List<RecipeEntity> oldRecipes, List<RecipeEntity> newRecipes) {
        this.oldRecipes = new ArrayList<>(oldRecipes);
        this.newRecipes = new ArrayList<>(newRecipes);
    }

    @Override
    public int getOldListSize() {
        //Log.e(TAG, "old size, " + oldRecipes.size());
        return oldRecipes.size();
    }

    @Override
    public int getNewListSize() {
        //Log.e(TAG, "new size, " + newRecipes.size());
        return newRecipes.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        //Log.e(TAG, "items the same, " + oldRecipes.get(oldItemPosition).equals(newRecipes.get(newItemPosition)));
        return oldRecipes.get(oldItemPosition).equals(newRecipes.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //Log.e(TAG, "contents the same, " + oldRecipes.get(oldItemPosition).identical(newRecipes.get(newItemPosition)));
        return oldRecipes.get(oldItemPosition).identical(newRecipes.get(newItemPosition));
    }
}
