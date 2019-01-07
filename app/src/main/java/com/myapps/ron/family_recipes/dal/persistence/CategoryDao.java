package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.model.CategoryEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Completable;

/**
 * Created by ronginat on 02/01/2019.
 */
@Dao
public interface CategoryDao {

    @Query("SELECT * FROM " + AppDatabases.TABLE_CATEGORIES)
    List<CategoryEntity> getAllCategories();

    @Insert
    Completable insertCategories(List<CategoryEntity> categoryEntities);

    @Insert
    Completable insertCategories(CategoryEntity... categoryEntities);

    @Insert
    void insertCategory(CategoryEntity categoryEntity);

    @Query("DELETE FROM " + AppDatabases.TABLE_CATEGORIES)
    void deleteAllCategories();
}
