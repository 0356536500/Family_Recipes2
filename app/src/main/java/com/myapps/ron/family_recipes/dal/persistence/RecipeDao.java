package com.myapps.ron.family_recipes.dal.persistence;

import com.myapps.ron.family_recipes.model.AccessEntity;
import com.myapps.ron.family_recipes.model.AccessEntity.RecipeAccess;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.utils.Constants;

import java.util.List;

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

    // region recipeMinimal
    String recipeMinimalFields = RecipeEntity.KEY_ID + ", " + RecipeEntity.KEY_NAME + ", " +
            RecipeEntity.KEY_DESCRIPTION + ", " + RecipeEntity.KEY_UPLOADER + ", " +
            RecipeEntity.KEY_CATEGORIES + ", " + RecipeEntity.KEY_THUMBNAIL + ", " +
            RecipeEntity.KEY_LIKES + ", " + RecipeEntity.KEY_FAVORITE;

   /* @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " ORDER BY " + RecipeEntity.KEY_CREATED + " DESC")
    DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrderByCreation();

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " order by " + RecipeEntity.KEY_MODIFIED + " DESC")
    DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrderByModified();

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " order by " + RecipeEntity.KEY_LIKES + " DESC")
    DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrderByLikes();*/

    /*@Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES
            + " ORDER BY :orderBy DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllOrderBy(String orderBy);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES +
            " WHERE " + RecipeEntity.KEY_CATEGORIES + " LIKE :filters ORDER BY :order DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllByCategoriesLikeOrderBy(String filters, String order);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE ("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search) ORDER BY :order DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllByNameLikeOrDescriptionLikeOrderBy(String search, String order);*/

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search)) AND ("
            + RecipeEntity.KEY_CATEGORIES + " LIKE :filters) ORDER BY creationDate DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByCreationDate(
            String search, String filters);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search)) AND "
            + RecipeEntity.KEY_CATEGORIES + " LIKE :filters ORDER BY lastModifiedDate DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByLastModified(
            String search, String filters);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search)) AND "
            + RecipeEntity.KEY_CATEGORIES + " LIKE :filters ORDER BY likes DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByPopularity(
            String search, String filters);


    /*@Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE
            + " ORDER BY :order DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllFavoritesOrderBy(String order);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE ("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search) AND "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE
            + " ORDER BY :order DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllFavoritesByNameLikeOrDescriptionLikeOrderBy(
            String search, String order);*/

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search)) AND "
            + RecipeEntity.KEY_CATEGORIES + " LIKE :filters AND "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE
            + " ORDER BY creationDate DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllFavoritesByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByCreationDate(
            String search, String filters);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search)) AND "
            + RecipeEntity.KEY_CATEGORIES + " LIKE :filters AND "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE
            + " ORDER BY lastModifiedDate DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllFavoritesByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByLastModified(
            String search, String filters);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE (("
            + RecipeEntity.KEY_NAME + " LIKE :search) OR ("
            + RecipeEntity.KEY_DESCRIPTION + " LIKE :search)) AND "
            + RecipeEntity.KEY_CATEGORIES + " LIKE :filters AND "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE
            + " ORDER BY likes DESC")
    DataSource.Factory<Integer, RecipeMinimal> findAllFavoritesByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByPopularity(
            String search, String filters);

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES)
    List<RecipeMinimal> findAllSync(); //testing only

    @Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE)
    List<RecipeMinimal> findAllFavoritesSync(); //testing only

    // endregion

    // region Get Rx RecipeEntity by id
    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Single<RecipeEntity> getRecipe(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Flowable<RecipeEntity> getObservableRecipe(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Maybe<RecipeEntity> getMaybeRecipe(String id);

    // endregion

    @Query("SELECT " + RecipeEntity.KEY_FOOD_FILES + " FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Single<String> getRecipeImages(String id);

    // region Get Accessed Time

    String recipeAccessedFields =
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_ID + ", " +
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_RECIPE_FILE + ", " +
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_THUMBNAIL + ", " +
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_FOOD_FILES + ", " +
            AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ACCESSED_THUMBNAIL + ", " +
            AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ACCESSED_RECIPE + ", " +
            AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ACCESSED_IMAGES;

    @Query("SELECT " + recipeAccessedFields +
            " FROM " + AppDatabases.TABLE_RECIPES + " INNER JOIN " + AppDatabases.TABLE_ACCESS +
            " ON " + AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_ID + " = " + AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ID +
            " WHERE " + AccessEntity.KEY_ACCESSED_THUMBNAIL + " IS NOT NULL" +
            " ORDER BY access.lastAccessedThumbnail ASC")
    List<RecipeAccess> getAccessTimeOrderByThumb();

    @Query("SELECT " + recipeAccessedFields +
            " FROM " + AppDatabases.TABLE_RECIPES + " INNER JOIN " + AppDatabases.TABLE_ACCESS +
            " ON " + AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_ID + " = " + AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ID +
            " WHERE " + AccessEntity.KEY_ACCESSED_RECIPE + " IS NOT NULL" +
            " ORDER BY access.lastAccessedRecipe ASC")
    List<RecipeAccess> getAccessTimeOrderByRecipe();

    @Query("SELECT " + recipeAccessedFields +
            " FROM " + AppDatabases.TABLE_RECIPES + " INNER JOIN " + AppDatabases.TABLE_ACCESS +
            " ON " + AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_ID + " = " + AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ID +
            " WHERE " + AccessEntity.KEY_ACCESSED_IMAGES + " IS NOT NULL" +
            " ORDER BY access.lastAccessedImages ASC")
    List<RecipeAccess> getAccessTimeOrderByImages();

    @Query("SELECT * FROM " + AppDatabases.TABLE_ACCESS + " where " + AccessEntity.KEY_ID + " = :id")
    AccessEntity getAccessById(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_ACCESS + " where " + AccessEntity.KEY_ID + " = :id")
    Maybe<AccessEntity> getMaybeAccessById(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_ACCESS)
    List<AccessEntity> getAllRecipeAccess();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRecipeAccess(AccessEntity access);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateRecipeAccess(AccessEntity access);

    /*@Query("UPDATE " + AppDatabases.TABLE_ACCESS + " SET lastAccessedThumbnail = :timestamp where " + AccessEntity.KEY_ID + " = :id")
    void updateRecipeAccessThumbnailbyId(String id, long timestamp);

    @Query("UPDATE " + AppDatabases.TABLE_ACCESS + " SET lastAccessedRecipe = :timestamp where " + AccessEntity.KEY_ID + " = :id")
    void updateRecipeAccessFilesById(String id, long timestamp);*/

    // endregion

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

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateRecipe(RecipeEntity recipeEntity);

    @Query("UPDATE " + AppDatabases.TABLE_RECIPES + " SET meLike = :meLike where " + RecipeEntity.KEY_ID + " = :id")
    void updateLikeRecipe(String id, int meLike);

    /*@Query("UPDATE " + AppDatabases.TABLE_RECIPES + " SET meLike = :meLike where " + RecipeEntity.KEY_ID + " IN (:ids)")
    void updateLikesFromUserRecord(List<String> ids, int meLike); // not working */

    @Delete
    void deleteRecipe(RecipeEntity recipeEntity);

    @Query("DELETE FROM " + AppDatabases.TABLE_RECIPES)
    void deleteAllRecipes();

    // region tests

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_NAME + " = :name")
    List<RecipeEntity> findRecipesByName(String name);

    // endregion
}
