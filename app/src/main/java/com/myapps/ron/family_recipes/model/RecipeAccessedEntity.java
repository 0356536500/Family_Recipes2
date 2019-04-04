package com.myapps.ron.family_recipes.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by ronginat on 04/04/2019.
 *
 * helper class containing attributes for deleting files linked to non-relevant recipes
 */
@Entity(tableName = "recipesAccess")
public class RecipeAccessedEntity {
    public static final String KEY_ACCESSED_THUMBNAIL = "lastAccessedThumbnail";
    public static final String KEY_ACCESSED_FILES = "lastAccessedFiles";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = RecipeEntity.KEY_ID)
    private String recipeId;
    // timestamp of last time accessed to thumbnail
    @ColumnInfo(name = KEY_ACCESSED_THUMBNAIL)
    private long lastAccessedThumbnail;

    // timestamp of last time accessed to food images or recipe file
    @ColumnInfo(name = KEY_ACCESSED_FILES)
    private long lastAccessedFiles;

    public RecipeAccessedEntity() {
        super();
    }

    @NonNull
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(@NonNull String recipeId) {
        this.recipeId = recipeId;
    }

    public long getLastAccessedThumbnail() {
        return lastAccessedThumbnail;
    }

    public void setLastAccessedThumbnail(long lastAccessedThumbnail) {
        this.lastAccessedThumbnail = lastAccessedThumbnail;
    }

    public long getLastAccessedFiles() {
        return lastAccessedFiles;
    }

    public void setLastAccessedFiles(long lastAccessedFiles) {
        this.lastAccessedFiles = lastAccessedFiles;
    }
}
