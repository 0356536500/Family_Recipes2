package com.myapps.ron.family_recipes.dal.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "records.db";

    // table names
    static final String TABLE_CATEGORIES = "categories";
    static final String TABLE_RECIPES = "recipes";

    // Recipes Table Columns names
    static final String KEY_ID = "id";
    static final String KEY_NAME = "name";
    static final String KEY_DESC = "description";
    static final String KEY_CREATED = "createdAt";
    static final String KEY_MODIFIED = "lastModifiedAt";
    static final String KEY_FILE = "recipeFile";
    static final String KEY_UPLOADER = "uploader";
    static final String KEY_CATEGORIES = "categories";
    static final String KEY_COMMENTS = "comments";
    static final String KEY_FOOD = "foodFiles";
    static final String KEY_LIKES = "likes";
    static final String KEY_ME_LIKE = "meLike";

    // Categories Table Columns names
    static final String CAT_KEY_ID = "name";
    static final String CAT_KEY_CATS = "categories";
    static final String CAT_KEY_COLOR = "color";

    private static final String CREATE_REC_EXEC = "CREATE TABLE " + TABLE_RECIPES + "("
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
            + KEY_ME_LIKE + " INTEGER " + ")";

    private static final String CREATE_CAT_EXEC = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + CAT_KEY_ID + " TEXT PRIMARY KEY,"
            + CAT_KEY_CATS + " TEXT,"
            + CAT_KEY_COLOR + " INTEGER " + ")";


    MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_REC_EXEC);
        db.execSQL(CREATE_CAT_EXEC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }
}
