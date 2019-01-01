package com.myapps.ron.family_recipes.dal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.myapps.ron.family_recipes.model.RecipeEntity;

import java.util.ArrayList;
import java.util.List;

import static com.myapps.ron.family_recipes.utils.Constants.TRUE;


public class RecipesDBHelper extends MyDBHelper{
    // All Static variables
    public static final String SORT_POPULAR = KEY_LIKES;
    public static final String SORT_RECENT = KEY_CREATED;
    public static final String SORT_MODIFIED = KEY_MODIFIED;

    public RecipesDBHelper(Context context) {
        super(context);
    }


    //region CRUD Methods
    /**
     * CRUD(Create, Read, Update, Delete) Operations
     */

    public void insertRecipe(RecipeEntity recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID, recipe.getId());
        values.put(KEY_NAME, recipe.getName());
        values.put(KEY_DESC, recipe.getDescription());
        values.put(KEY_CREATED, recipe.getCreationDate());
        values.put(KEY_MODIFIED, recipe.getLastModifiedAt());
        values.put(KEY_FILE, recipe.getRecipeFile());
        values.put(KEY_UPLOADER, recipe.getUploader());
        values.put(KEY_CATEGORIES, recipe.getCategoriesToString());
        values.put(KEY_COMMENTS, recipe.getCommentsToString());
        values.put(KEY_FOOD, recipe.getFoodFilesToString());
        values.put(KEY_LIKES, recipe.getLikes());
        values.put(KEY_ME_LIKE, recipe.getMeLike());

        // Inserting Row
        db.insert(TABLE_RECIPES, null, values);
        db.close(); // Closing database connection
    }

    public RecipeEntity getRecipe(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        //                                  new String[]{CAT_KEY_ID,KEY_NAME}
        Cursor cursor = db.query(TABLE_RECIPES, null,KEY_ID+"=?",
                new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor != null) {
            cursor.moveToFirst();
            RecipeEntity recipe = new RecipeEntity.RecipeBuilder()
                    .id(cursor.getString(cursor.getColumnIndex(KEY_ID)))
                    .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                    .description(cursor.getString(cursor.getColumnIndex(KEY_DESC)))
                    .createdAt(cursor.getString(cursor.getColumnIndex(KEY_CREATED)))
                    .lastModifiedAt(cursor.getString(cursor.getColumnIndex(KEY_MODIFIED)))
                    .recipeFile(cursor.getString(cursor.getColumnIndex(KEY_FILE)))
                    .uploader(cursor.getString(cursor.getColumnIndex(KEY_UPLOADER)))
                    .categoriesJson(cursor.getString(cursor.getColumnIndex(KEY_CATEGORIES)))
                    .commentsJson(cursor.getString(cursor.getColumnIndex(KEY_COMMENTS)))
                    .foodFilesJson(cursor.getString(cursor.getColumnIndex(KEY_FOOD)))
                    .likes(cursor.getInt(cursor.getColumnIndex(KEY_LIKES)))
                    .meLike(cursor.getInt(cursor.getColumnIndex(KEY_ME_LIKE)) == TRUE)
                    .build();
            cursor.close();
            return recipe;
            /*return new Recipe(cursor.getString(cursor.getColumnIndex(KEY_ID)), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9),
                    cursor.getInt(10), cursor.getInt(11) == TRUE);*/

        }
        return null;
    }

    // Getting All Records
    private List<RecipeEntity> getAllRecipesFromQuery(String selectQuery) {
        List<RecipeEntity> recipeList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RecipeEntity recipe = new RecipeEntity.RecipeBuilder()
                        .id(cursor.getString(0))
                        .name(cursor.getString(1))
                        .description(cursor.getString(2))
                        .createdAt(cursor.getString(3))
                        .lastModifiedAt(cursor.getString(4))
                        .recipeFile(cursor.getString(5))
                        .uploader(cursor.getString(6))
                        .categoriesJson(cursor.getString(7))
                        .commentsJson(cursor.getString(8))
                        .foodFilesJson(cursor.getString(9))
                        .likes(cursor.getInt(10))
                        .meLike(cursor.getInt(11) == TRUE)
                        .build();
                /*Recipe recipe = new Recipe();
                recipe.setId(cursor.getString(0));
                recipe.setName(cursor.getString(1));
                recipe.setDescription(cursor.getString(2));
                recipe.setCreationDate(cursor.getString(3));
                recipe.setLastModifiedAt(cursor.getString(4));
                recipe.setRecipeFile(cursor.getString(5));
                recipe.setUploader(cursor.getString(6));
                recipe.setStringCategories(cursor.getString(7));
                recipe.setStringComments(cursor.getString(8));
                recipe.setStringFoodFiles(cursor.getString(9));
                recipe.setLikes(cursor.getInt(10));
                recipe.setMeLike(cursor.getInt(11));*/

                // Adding contact to list
                recipeList.add(recipe);

            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        // return contact list
        return recipeList;
    }

    // Getting All Records
    public List<RecipeEntity> getAllRecipes(String orderedBy) {
        // Default is by creation date
        if(orderedBy == null)
            orderedBy = KEY_CREATED;
        // Check if valid parameter
        if(!orderedBy.equals(KEY_CREATED) && !orderedBy.equals(KEY_LIKES) && !orderedBy.equals(KEY_MODIFIED))
            return null;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RECIPES + " ORDER BY " + orderedBy + " DESC";

        return getAllRecipesFromQuery(selectQuery);
    }

    // Getting All Records
    public List<RecipeEntity> getFavoriteRecipes(String orderedBy) {
        // Default is by creation date
        if(orderedBy == null)
            orderedBy = KEY_CREATED;
        // Check if valid parameter
        if(!orderedBy.equals(KEY_CREATED) && !orderedBy.equals(KEY_LIKES))
            return null;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RECIPES + " WHERE " + KEY_ME_LIKE + "=" + TRUE
                + " ORDER BY " + orderedBy + " DESC";

        return getAllRecipesFromQuery(selectQuery);
    }

    public int updateRecipeServerChanges(RecipeEntity recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DESC, recipe.getDescription());
        values.put(KEY_MODIFIED, recipe.getLastModifiedAt());
        values.put(KEY_FILE, recipe.getRecipeFile());
        values.put(KEY_CATEGORIES, recipe.getCategoriesToString());
        values.put(KEY_COMMENTS, recipe.getCommentsToString());
        values.put(KEY_FOOD, recipe.getFoodFilesToString());
        values.put(KEY_LIKES, recipe.getLikes());

        // updating row
        return db.update(TABLE_RECIPES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())});
    }

    public int updateRecipeUserChanges(RecipeEntity recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_DESC, recipe.getDescription());
        values.put(KEY_MODIFIED, recipe.getLastModifiedAt());
        //values.put(KEY_CATEGORIES, recipe.getCategoriesToString());
        values.put(KEY_COMMENTS, recipe.getCommentsToString());
        //values.put(KEY_FOOD, recipe.getFoodFilesToString());
        values.put(KEY_LIKES, recipe.getLikes());
        values.put(KEY_ME_LIKE, recipe.getMeLike());

        // updating row
        return db.update(TABLE_RECIPES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())});
    }

    public void deleteRecipe(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECIPES, KEY_ID + "=?", new String[]{ id });
        //db.delete(TABLE_RECIPES, KEY_ID + "=" + id, null);
        db.close();
    }

    //endregion


    public int getRecipesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_RECIPES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public boolean recipeExists(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_RECIPES + " WHERE " + KEY_ID + "='" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }


}
