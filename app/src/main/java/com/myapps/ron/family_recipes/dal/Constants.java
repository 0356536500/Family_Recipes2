package com.myapps.ron.family_recipes.dal;


import com.myapps.ron.family_recipes.model.RecipeEntity;

public class Constants {
    // Sort options from SQLite DB
    public static final String SORT_POPULAR = RecipeEntity.KEY_LIKES;
    public static final String SORT_RECENT = RecipeEntity.KEY_CREATED;
    public static final String SORT_MODIFIED = RecipeEntity.KEY_MODIFIED;
}
