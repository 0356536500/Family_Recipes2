package com.myapps.ron.family_recipes.model;

import com.myapps.ron.family_recipes.logic.persistence.AppDatabases;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by ronginat on 04/04/2019.
 *
 * helper class containing attributes for deleting files linked to non-relevant recipes
 */
@Entity(tableName = AppDatabases.TABLE_ACCESS)
public class AccessEntity {
    public static final String KEY_ID = "id";
    public static final String KEY_ACCESSED_THUMBNAIL = "lastAccessedThumbnail";
    public static final String KEY_ACCESSED_RECIPE = "lastAccessedRecipe";
    public static final String KEY_ACCESSED_IMAGES = "lastAccessedImages";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = KEY_ID)
    private String recipeId;
    // timestamp of last time accessed to thumbnail
    @ColumnInfo(name = KEY_ACCESSED_THUMBNAIL)
    private Long lastAccessedThumbnail;

    // timestamp of last time accessed to recipe file
    @ColumnInfo(name = KEY_ACCESSED_RECIPE)
    private Long lastAccessedRecipe;

    // timestamp of last time accessed to food images
    @ColumnInfo(name = KEY_ACCESSED_IMAGES)
    private Long lastAccessedImages;

    public AccessEntity() {
        super();
    }

    @NonNull
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(@NonNull String recipeId) {
        this.recipeId = recipeId;
    }

    public Long getLastAccessedThumbnail() {
        return lastAccessedThumbnail;
    }

    public void setLastAccessedThumbnail(Long lastAccessedThumbnail) {
        this.lastAccessedThumbnail = lastAccessedThumbnail;
    }

    public Long getLastAccessedRecipe() {
        return lastAccessedRecipe;
    }

    public void setLastAccessedRecipe(Long lastAccessedRecipe) {
        this.lastAccessedRecipe = lastAccessedRecipe;
    }

    public Long getLastAccessedImages() {
        return lastAccessedImages;
    }

    public void setLastAccessedImages(Long lastAccessedImages) {
        this.lastAccessedImages = lastAccessedImages;
    }

    /**
     * POJO of {@link RecipeEntity} and {@link AccessEntity} combined with SQLite 'Left Outer JOIN'
     */
    public static class RecipeAccess {
        public String id;
        public String recipeFile;
        public String thumbnail;
        public List<String> foodFiles;

        public Long lastAccessedThumbnail;
        public Long lastAccessedRecipe;
        public Long lastAccessedImages;

        @Ignore
        public Object getFileNameByAccessKey(String accessKey) {
            switch (accessKey) {
                case KEY_ACCESSED_THUMBNAIL:
                    return thumbnail;
                case KEY_ACCESSED_RECIPE:
                    return recipeFile;
                case KEY_ACCESSED_IMAGES:
                    return foodFiles;
                default:
                    return null;
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "RecipeAccess{" +
                    "id='" + id + '\'' +
                    ", thumbnail='" + thumbnail + '\'' +
                    ", lastAccessedThumbnail=" + lastAccessedThumbnail +
                    ", lastAccessedRecipe=" + lastAccessedRecipe +
                    ", lastAccessedImages=" + lastAccessedImages +
                    '}';
        }
    }
}
