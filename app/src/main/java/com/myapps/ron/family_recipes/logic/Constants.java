package com.myapps.ron.family_recipes.logic;


import com.myapps.ron.family_recipes.model.RecipeEntity;

public class Constants {
    // Sort options from SQLite DB
    public static final String SORT_POPULAR = RecipeEntity.KEY_LIKES;
    public static final String SORT_RECENT = RecipeEntity.KEY_CREATED;
    public static final String SORT_MODIFIED = RecipeEntity.KEY_MODIFIED;

    public static final long MIN_FOOD_FOLDER_SIZE_TO_START_DELETING_CONTENT = 104857600L; //100MB in bytes, 73400320L; //70MB in bytes
    public static final long MIN_RECIPE_FOLDER_SIZE_TO_START_DELETING_CONTENT = 20971520L; //20MB in bytes
    public static final long MIN_THUMB_FOLDER_SIZE_TO_START_DELETING_CONTENT = 31457280L; //30MB in bytes

    public static final long TARGET_FOOD_FOLDER_SIZE_AFTER_DELETING_CONTENT = 52428800L; //50MB in bytes
    public static final long TARGET_RECIPE_FOLDER_SIZE_AFTER_DELETING_CONTENT = 10485760L; //10MB in bytes
    public static final long TARGET_THUMB_FOLDER_SIZE_AFTER_DELETING_CONTENT = 10485760L; //10MB in bytes
}
