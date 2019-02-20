package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.model.PendingRecipe;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * Created by ronginat on 20/02/2019.
 */
@Dao
public interface PendingRecipeDao {
    @Query("SELECT * FROM " + AppDatabases.TABLE_PENDING_RECIPES)// + " ORDER BY " + PendingRecipe.KEY_CREATED + " ASC")
    List<PendingRecipe> getAll();

    @Insert
    void insert(PendingRecipe recipe);

    @Insert
    void insertAll(PendingRecipe... recipes);

    @Delete
    void delete(PendingRecipe recipe);
}
