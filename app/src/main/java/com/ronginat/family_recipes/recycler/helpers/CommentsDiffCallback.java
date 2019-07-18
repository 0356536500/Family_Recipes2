package com.ronginat.family_recipes.recycler.helpers;

import androidx.recyclerview.widget.DiffUtil;

import com.ronginat.family_recipes.model.CommentEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 09/12/2018.
 */
public class CommentsDiffCallback extends DiffUtil.Callback {
    private List<CommentEntity> oldComments;
    private List<CommentEntity> newComments;

    public CommentsDiffCallback(List<CommentEntity> oldRecipes, List<CommentEntity> newRecipes) {
        this.oldComments = new ArrayList<>(oldRecipes);
        this.newComments = new ArrayList<>(newRecipes);
    }

    @Override
    public int getOldListSize() {
        return oldComments.size();
    }

    @Override
    public int getNewListSize() {
        return newComments.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldComments.get(oldItemPosition).equals(newComments.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldComments.get(oldItemPosition).equals(newComments.get(newItemPosition));
    }
}
