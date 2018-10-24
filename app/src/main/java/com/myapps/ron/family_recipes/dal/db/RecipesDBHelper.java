package com.myapps.ron.family_recipes.dal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.myapps.ron.family_recipes.model.Recipe;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;


public class RecipesDBHelper extends MyDBHelper{
    private static final int FALSE = 0;
    private static final int TRUE = 1;
    // All Static variables
    // Database Version
   /* private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "records.db";

    // Recipes table name
    private static final String TABLE_NAME = "recipes";

    // Recipes Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "description";
    private static final String KEY_CREATED = "createdAt";
    private static final String KEY_MODIFIED = "lastModifiedAt";
    private static final String KEY_FILE = "recipeFile";
    private static final String KEY_UPLOADER = "uploader";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_COMMENTS = "comments";
    private static final String KEY_FOOD = "foodFiles";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_ME_LIKE = "meLike";
*/
    public static final String SORT_POPULAR = KEY_LIKES;
    public static final String SORT_RECENT = KEY_CREATED;
    public static final String SORT_MODIFIED = KEY_MODIFIED;

    /*private static final String CREATE_EXEC = "CREATE TABLE " + TABLE_NAME + "("
            + KEY_ID + " TEXT PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_DESC + " TEXT,"
            + KEY_CREATED + " TEXT,"
            + KEY_MODIFIED + " TEXT,"
            + KEY_FILE + " TEXT,"
            + KEY_UPLOADER + " TEXT,"
            + KEY_CATEGORIES + " TEXT,"
            + KEY_COMMENTS + " TEXT,"
            + KEY_FOOD + " TEXT,"
            + KEY_LIKES + " INTEGER,"
            + KEY_ME_LIKE + " INTEGER " + ")";*/

    public RecipesDBHelper(Context context) {
        super(context);
    }

    //region Override From SQLite
    //creating Tables
    /*@Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXEC);
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);

        // Create tables again
        onCreate(db);
    }
    //endregion

    //region CRUD Methods
    /**
     * CRUD(Create, Read, Update, Delete) Operations
     */

    public void insertRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID, recipe.getId());
        values.put(KEY_NAME, recipe.getName());
        values.put(KEY_DESC, recipe.getDescription());
        values.put(KEY_CREATED, recipe.getCreatedAt());
        values.put(KEY_MODIFIED, recipe.getLastModifiedAt());
        values.put(KEY_FILE, recipe.getRecipeFile());
        values.put(KEY_UPLOADER, recipe.getUploader());
        values.put(KEY_CATEGORIES, recipe.getStringCategories());
        values.put(KEY_COMMENTS, recipe.getStringComments());
        values.put(KEY_FOOD, recipe.getStringFoodFiles());
        values.put(KEY_LIKES, recipe.getLikes());
        values.put(KEY_ME_LIKE, recipe.getMeLike() ? TRUE : FALSE);

        // Inserting Row
        db.insert(TABLE_RECIPES, null, values);
        db.close(); // Closing database connection
    }

    public Recipe getRecipe(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        //                                  new String[]{CAT_KEY_ID,KEY_NAME}
        Cursor cursor = db.query(TABLE_RECIPES, null,KEY_ID+"=?",
                new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor != null) {
            cursor.moveToFirst();
            return new Recipe(cursor.getString(cursor.getColumnIndex(KEY_ID)), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9),
                    cursor.getInt(10), cursor.getInt(11) == TRUE);
        }
        return null;
    }

    // Getting All Records
    private List<Recipe> getAllRecipesFromQuery(String selectQuery) {
        List<Recipe> recipeList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Recipe recipe = new Recipe();
                recipe.setId(cursor.getString(0));
                recipe.setName(cursor.getString(1));
                recipe.setDescription(cursor.getString(2));
                recipe.setCreatedAt(cursor.getString(3));
                recipe.setLastModifiedAt(cursor.getString(4));
                recipe.setRecipeFile(cursor.getString(5));
                recipe.setUploader(cursor.getString(6));
                recipe.setStringCategories(cursor.getString(7));
                recipe.setStringComments(cursor.getString(8));
                recipe.setStringFoodFiles(cursor.getString(9));
                recipe.setLikes(cursor.getInt(10));
                recipe.setMeLike(cursor.getInt(11) == TRUE);

                // Adding contact to list
                recipeList.add(recipe);

            } while (cursor.moveToNext());
        }

        db.close();
        // return contact list
        return recipeList;
    }

    // Getting All Records
    public List<Recipe> getAllRecipes(String orderedBy) {
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
    public List<Recipe> getFavoriteRecipes(String orderedBy) {
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

    public int updateRecipeServerChanges(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DESC, recipe.getDescription());
        values.put(KEY_MODIFIED, recipe.getLastModifiedAt());
        values.put(KEY_CATEGORIES, recipe.getStringCategories());
        values.put(KEY_COMMENTS, recipe.getStringComments());
        values.put(KEY_FOOD, recipe.getStringFoodFiles());
        values.put(KEY_LIKES, recipe.getLikes());

        // updating row
        return db.update(TABLE_RECIPES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())});
    }

    public int updateRecipeUserChanges(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_DESC, recipe.getDescription());
        values.put(KEY_MODIFIED, recipe.getLastModifiedAt());
        //values.put(KEY_CATEGORIES, recipe.getStringCategories());
        values.put(KEY_COMMENTS, recipe.getStringComments());
        //values.put(KEY_FOOD, recipe.getStringFoodFiles());
        values.put(KEY_LIKES, recipe.getLikes());
        values.put(KEY_ME_LIKE, recipe.getMeLike() ? TRUE : FALSE);

        // updating row
        return db.update(TABLE_RECIPES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())});
    }

    public void deleteRecipe(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECIPES, KEY_ID + "=" + id, null);
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
        String query = "SELECT id FROM "+ TABLE_RECIPES + " WHERE id=" + id;
        Cursor cursor = db.rawQuery(query, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }


}
