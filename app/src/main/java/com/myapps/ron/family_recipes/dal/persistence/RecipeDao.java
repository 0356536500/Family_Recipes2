package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.utils.Constants;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

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

    // region recipeMinimal
    String recipeMinimalFields = RecipeEntity.KEY_ID + ", " + RecipeEntity.KEY_NAME + ", " +
            RecipeEntity.KEY_DESCRIPTION + ", " + RecipeEntity.KEY_UPLOADER + ", " +
            RecipeEntity.KEY_CATEGORIES + ", " + RecipeEntity.KEY_FOOD_FILES + ", " +
            RecipeEntity.KEY_LIKES;

    /*@Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES)
    Flowable<List<RecipeMinimal>> getAllRecipesMinimal();

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " order by :order DESC")
    Flowable<List<RecipeMinimal>> getAllRecipesMinimalOrdered(String order);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " where :query")
    Flowable<List<RecipeMinimal>> getAllRecipesMinimalByQuery(String query);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " order by :order DESC")
    LiveData<List<RecipeMinimal>> getRecipesLiveMinimalOrdered(String order); */

   /* @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " ORDER BY " + RecipeEntity.KEY_CREATED + " DESC")
    DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrderByCreation();

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " order by " + RecipeEntity.KEY_MODIFIED + " DESC")
    DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrderByModified();

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " order by " + RecipeEntity.KEY_LIKES + " DESC")
    DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrderByLikes();*/

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " ORDER BY :orderBy DESC")
    DataSource.Factory<Integer, RecipeMinimal> getAllRecipesOrdered(String orderBy);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (" + RecipeEntity.KEY_NAME + " LIKE :search) OR (" +
            RecipeEntity.KEY_DESCRIPTION + " LIKE :search)")
    DataSource.Factory<Integer, RecipeMinimal> getAllRecipesSearched(String search);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (" + RecipeEntity.KEY_NAME + " LIKE :search) OR (" +
            RecipeEntity.KEY_DESCRIPTION + " LIKE :search) ORDER BY :order DESC")
    DataSource.Factory<Integer, RecipeMinimal> getAllRecipesSearchedOrdered(String search, String order);


    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (" + RecipeEntity.KEY_NAME + " LIKE :search) OR (" +
            RecipeEntity.KEY_DESCRIPTION + " LIKE :search) AND " + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE + " ORDER BY :order DESC")
    DataSource.Factory<Integer, RecipeMinimal> getAllFavoriteRecipesSearchedOrdered(String search, String order);



    /*@Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE " + RecipeEntity.KEY_NAME + " LIKE :search OR " +
            RecipeEntity.KEY_DESCRIPTION + " LIKE :search ORDER BY :order DESC")
    Flowable<List<RecipeMinimal>> getAllRecipesMinimalSearchedOrdered(String search, String order);*/
    // endregion


    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Single<RecipeEntity> getRecipe(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Maybe<RecipeEntity> isRecipeExists(String id);

    /*@Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where :query")
    Flowable<List<RecipeEntity>> getAllRecipesByQuery(String query);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " order by :order DESC")
    Flowable<List<RecipeEntity>> getRecipesOrdered(String order);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_NAME + " like :search OR " +
            RecipeEntity.KEY_DESCRIPTION + " like :search order by :order DESC")
    Flowable<List<RecipeEntity>> getRecipeSearchedOrdered(String search, String order);*/

    // Emits the number of users added to the database.
    //@Insert
    //Maybe<Integer> insertList(List<RecipeEntity> recipeEntities);

    // Makes sure that the operation finishes successfully.
    @Insert
    Completable insertRecipes(RecipeEntity... recipeEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(RecipeEntity recipeEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecipeEntity> list);

    @Update
    void updateRecipes(RecipeEntity... recipeEntities);

    @Update
    void updateRecipe(RecipeEntity recipeEntity);

    @Query("UPDATE " + AppDatabases.TABLE_RECIPES + " SET meLike = :meLike where " + RecipeEntity.KEY_ID + " = :id")
    void updateLikeRecipe(String id, int meLike);

    @Delete
    void deleteRecipe(RecipeEntity recipeEntity);

    @Query("DELETE FROM " + AppDatabases.TABLE_RECIPES)
    void deleteAllRecipes();
}
