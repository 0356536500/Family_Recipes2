package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.model.RecipeEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * Created by ronginat on 31/12/2018.
 */
@Dao
public interface RecipeDao {
    String ORDER_BY = "order by";
    String DESC = " DESC";
    String CREATION_DATE = "creationDate" + DESC;
    String MODIFIED_DATE = "lastModifiedAt"  + DESC;
    String POPULARTY = "likes" + DESC;

    String SEARCH_HEAD = "name like %{}% or description like %{}%";
    //String SEARCH_CATEGORIES = "name like %{}% or description like %{}%";

    @Query("SELECT * FROM recipes where :query")
    Flowable<List<RecipeEntity>> getAllRecipesByQuery(String query);

    @Query("SELECT * FROM recipes where id = :id")
    Flowable<RecipeEntity> getRecipe(String id);

    @Query("SELECT * FROM recipes order by :order DESC")
    Flowable<List<RecipeEntity>> getRecipeOrdered(String order);

    @Query("SELECT * FROM recipes where name like :search OR description like :search " +
            "order by :order DESC")
    Flowable<List<RecipeEntity>> getRecipeSearchedOrdered(String search, String order);

    // Emits the number of users added to the database.
    //@Insert
    //Maybe<Integer> insertList(List<RecipeEntity> recipeEntities);

    // Makes sure that the operation finishes successfully.
    @Insert
    Completable insertRecipes(RecipeEntity... recipeEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(RecipeEntity recipeEntity);

    @Update
    void updateRecipes(RecipeEntity... recipeEntities);

    @Update
    void updateRecipe(RecipeEntity recipeEntity);

    @Query("UPDATE recipes SET meLike = :meLike where id = :id")
    void updateLikeRecipe(String id, int meLike);

    @Delete
    void deleteRecipe(RecipeEntity recipeEntity);

    @Query("DELETE FROM recipes")
    void deleteAllRecipes();
}
