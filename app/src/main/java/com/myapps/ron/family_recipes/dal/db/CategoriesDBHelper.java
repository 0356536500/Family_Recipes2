package com.myapps.ron.family_recipes.dal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.myapps.ron.family_recipes.model.Category;

import java.util.ArrayList;
import java.util.List;


public class CategoriesDBHelper extends MyDBHelper{
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "records.db";

    // Contacts table name
    /*private static final String TABLE_NAME = "categories";

    // Categories Table Columns names
    private static final String CAT_KEY_ID = "name";
    private static final String CAT_KEY_CATS = "categories";
    private static final String CAT_KEY_COLOR = "color";


    private static final String CREATE_EXEC = "CREATE TABLE " + TABLE_NAME + "("
            + CAT_KEY_ID + " TEXT PRIMARY KEY,"
            + CAT_KEY_CATS + " TEXT,"
            + CAT_KEY_COLOR + " INTEGER " + ")";*/

    public CategoriesDBHelper(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);

        // Create tables again
        onCreate(db);
    }
    //endregion

    //region CRUD Methods
    /**
     * CRUD(Create, Read, Update, Delete) Operations
     */

    public void insertCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CAT_KEY_ID, category.getName());
        values.put(CAT_KEY_COLOR, category.getColor());
        values.put(CAT_KEY_CATS, category.getStringCategories());


        // Inserting Row
        db.insert(TABLE_CATEGORIES, null, values);
        db.close(); // Closing database connection
    }

    public Category getCategory(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        //                                  new String[]{CAT_KEY_ID,CAT_KEY_ID}
        Cursor cursor = db.query(TABLE_CATEGORIES, null, CAT_KEY_ID +"=?",
                new String[]{String.valueOf(id)},null,null,null,null);
        Category temp = null;
        if(cursor != null) {
            cursor.moveToFirst();
            temp = new Category.CategoryBuilder()
                    .name(cursor.getString(cursor.getColumnIndex(CAT_KEY_ID)))
                    .color(cursor.getString(1))
                    .categories(cursor.getString(2))
                    .build();

            cursor.close();
        }
        return temp;
    }

    // Getting All Records
    public List<Category> getAllCategories() {
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
        List<Category> categoryList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor != null) {
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Category category = new Category.CategoryBuilder()
                            .name(cursor.getString(cursor.getColumnIndex(CAT_KEY_ID)))
                            .color(cursor.getString(1))
                            .categories(cursor.getString(2))
                            .build();

                    // Adding contact to list
                    categoryList.add(category);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        // return contact list
        return categoryList;
    }


    public int updateCategoryServerChanges(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CAT_KEY_CATS, category.getStringCategories());
        values.put(CAT_KEY_COLOR, category.getColor());

        // updating row
        return db.update(TABLE_CATEGORIES, values, CAT_KEY_ID + " = ?",
                new String[]{String.valueOf(category.getName())});
    }

    /*public int updateCategoryUserChanges(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CAT_KEY_CATS, category.getStringCategories());
        values.put(CAT_KEY_COLOR, category.getColor());

        // updating row
        return db.update(TABLE_CATEGORIES, values, CAT_KEY_ID + " = ?",
                new String[]{String.valueOf(category.getName())});
    }

    public void deleteCategory(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, CAT_KEY_ID + "=" + id, null);
        db.close();
    }*/

    //endregion


    public int getCategoriesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public boolean categoryExists(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_CATEGORIES + " WHERE " + CAT_KEY_ID + "='" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }


}
