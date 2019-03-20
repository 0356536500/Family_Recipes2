package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.model.PendingRecipeEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Completable;

/**
 * Created by ronginat on 20/02/2019.
 */
@Dao
public interface PendingRecipeDao {
    @Query("SELECT * FROM " + AppDatabases.TABLE_PENDING_RECIPES)// + " ORDER BY " + PendingRecipeEntity.KEY_CREATED + " ASC")
    List<PendingRecipeEntity> getAll();

    @Insert
    Completable insert(PendingRecipeEntity recipe);

    @Insert
    void insertAll(PendingRecipeEntity... recipes);

    @Delete
    void delete(PendingRecipeEntity recipe);

    @Query("DELETE FROM " + AppDatabases.TABLE_PENDING_RECIPES)
    void deleteAll();
}
