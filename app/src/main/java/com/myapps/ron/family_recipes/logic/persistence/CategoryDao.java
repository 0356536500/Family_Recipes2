package com.myapps.ron.family_recipes.logic.persistence;

import com.myapps.ron.family_recipes.model.CategoryEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;

/**
 * Created by ronginat on 02/01/2019.
 */
@Dao
public interface CategoryDao {

    @Query("SELECT * FROM " + AppDatabases.TABLE_CATEGORIES)
    LiveData<List<CategoryEntity>> getAllCategoriesLiveData();

    /*@Query("SELECT * FROM " + AppDatabases.TABLE_CATEGORIES)
    List<CategoryEntity> getAllCategories();*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<CategoryEntity> categoryEntities);

    /*@Query("SELECT * FROM " + AppDatabases.TABLE_CATEGORIES + " WHERE " + CategoryEntity.KEY_NAME + " = :name")
    Maybe<CategoryEntity> getCategory(String name);*/

    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(CategoryEntity... categoryEntities);*/

    /*@Insert
    void insertCategory(CategoryEntity categoryEntity);*/

    /*@Query("DELETE FROM " + AppDatabases.TABLE_CATEGORIES)
    void deleteAllCategories();*/
}
