package com.ronginat.family_recipes.logic.persistence;

import com.ronginat.family_recipes.model.CategoryEntity;

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<CategoryEntity> categoryEntities);

    /*@Insert
    void insertCategory(CategoryEntity categoryEntity);*/
}
