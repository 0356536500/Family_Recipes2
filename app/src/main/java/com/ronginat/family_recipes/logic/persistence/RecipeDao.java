package com.ronginat.family_recipes.logic.persistence;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ronginat.family_recipes.model.AccessEntity;
import com.ronginat.family_recipes.model.AccessEntity.RecipeAccess;
import com.ronginat.family_recipes.model.ContentEntity;
import com.ronginat.family_recipes.model.RecipeEntity;
import com.ronginat.family_recipes.model.RecipeMinimal;
import com.ronginat.family_recipes.utils.Constants;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by ronginat on 31/12/2018.
 */
@Dao
public interface RecipeDao {

    // region recipeMinimal
    String recipeMinimalFields = RecipeEntity.KEY_ID + ", " +
            RecipeEntity.KEY_MODIFIED + ", " + RecipeEntity.KEY_NAME + ", " +
            RecipeEntity.KEY_DESCRIPTION + ", " + RecipeEntity.KEY_AUTHOR + ", " +
            RecipeEntity.KEY_CATEGORIES + ", " + RecipeEntity.KEY_THUMBNAIL + ", " +
            RecipeEntity.KEY_LIKES + ", " + RecipeEntity.KEY_FAVORITE;

    // fetch recipes
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


    // fetch favorites
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

    /*@Query("SELECT " + recipeMinimalFields + " FROM " + AppDatabases.TABLE_RECIPES + " WHERE "
            + RecipeEntity.KEY_FAVORITE + " = " + Constants.TRUE)
    List<RecipeMinimal> findAllFavoritesSync(); //testing only*/

    // endregion

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    RecipeEntity findRecipeById(String id);

    // region Get Rx RecipeEntity by id
    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Single<RecipeEntity> getSingleRecipe(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Flowable<RecipeEntity> getObservableRecipe(String id);

    @Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Maybe<RecipeEntity> getMaybeRecipe(String id);

    // endregion

    @Query("SELECT " + RecipeEntity.KEY_IMAGES + " FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :id")
    Maybe<String> getMaybeRecipeImages(String id);

    // region Get Accessed Time

    String recipeAccessedFields =
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_ID + ", " +
            //AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_RECIPE_FILE + ", " +
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_THUMBNAIL + ", " +
            AppDatabases.TABLE_RECIPES + "." + RecipeEntity.KEY_IMAGES + ", " +
            AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ACCESSED_THUMBNAIL + ", " +
            AppDatabases.TABLE_ACCESS + "." + AccessEntity.KEY_ACCESSED_CONTENT + ", " +
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
            " WHERE " + AccessEntity.KEY_ACCESSED_CONTENT + " IS NOT NULL" +
            " ORDER BY access.lastAccessedContent ASC")
    List<RecipeAccess> getAccessTimeOrderByContent();

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

    @Query("DELETE FROM " + AppDatabases.TABLE_ACCESS + " where " + AccessEntity.KEY_ID + " = :id")
    void deleteAccessById(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRecipeAccess(AccessEntity access);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateRecipeAccess(AccessEntity access);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(RecipeEntity recipeEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecipeEntity> list);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateRecipe(RecipeEntity recipeEntity);

    @Query("UPDATE " + AppDatabases.TABLE_RECIPES + " SET meLike = :meLike where " + RecipeEntity.KEY_ID + " = :id")
    void updateLikeRecipe(String id, int meLike);

    /*@Delete
    void deleteRecipe(RecipeEntity recipeEntity);*/

    @Query("DELETE FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_ID + " = :recipeId")
    void deleteRecipeById(String recipeId);

    @Query("DELETE FROM " + AppDatabases.TABLE_RECIPES)
    void deleteAllRecipes();

    // region tests

    /*@Query("SELECT * FROM " + AppDatabases.TABLE_RECIPES + " where " + RecipeEntity.KEY_NAME + " = :name")
    List<RecipeEntity> findRecipesByName(String name);*/

    // endregion

    // region recipe content

    /*String recipeContentFields = ContentEntity.KEY_ID + ", " +
            ContentEntity.KEY_MODIFIED + ", " +
            ContentEntity.KEY_CONTENT;*/

    @Query("DELETE FROM " + AppDatabases.TABLE_CONTENTS + " where " + ContentEntity.KEY_ID + " = :recipeId")
    void deleteContentById(String recipeId);

    @Query("SELECT * FROM " + AppDatabases.TABLE_CONTENTS + " where " + ContentEntity.KEY_ID + " = :recipeId")
    Maybe<ContentEntity> findMaybeContentById(String recipeId);

    @Query("SELECT " + ContentEntity.KEY_CONTENT + " FROM " + AppDatabases.TABLE_CONTENTS + " where " + ContentEntity.KEY_ID + " = :recipeId")
    Flowable<String> findContentById(String recipeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertRecipeContent(ContentEntity contentEntity);

    @Query("SELECT COUNT(" + ContentEntity.KEY_ID + ") FROM " + AppDatabases.TABLE_CONTENTS)
    int getContentDataCount();

    // endregion
}
