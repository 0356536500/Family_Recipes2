package com.myapps.ron.family_recipes.model;

import com.myapps.ron.family_recipes.logic.persistence.AppDatabases;
import com.myapps.ron.family_recipes.utils.logic.DateUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by ronginat on 20/02/2019.
 */
@Entity(tableName = AppDatabases.TABLE_PENDING_RECIPES)
@SuppressWarnings("WeakerAccess")
public class PendingRecipeEntity {

    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CATEGORIES = "categories";
    public static final String KEY_CREATED = "creationDate";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_FOOD_FILES = "foodFiles";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = KEY_CREATED)
    private String creationDate;
    @ColumnInfo(name = KEY_NAME)
    private String name;
    @ColumnInfo(name = KEY_DESCRIPTION)
    private String description;
    @ColumnInfo(name = KEY_CONTENT)
    private String content;
    @ColumnInfo(name = KEY_CATEGORIES)
    private List<String> categories;
    @ColumnInfo(name = KEY_FOOD_FILES)
    private List<String> foodFiles;

    public PendingRecipeEntity() {
        this.creationDate = DateUtil.getUTCTime();
        categories = new ArrayList<>();
        foodFiles = new ArrayList<>();
    }

    /*public PendingRecipeEntity(RecipeEntity entity) {
        this.creationDate = entity.getCreationDate();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.recipeContent = entity.getRecipeFile();
        this.categories = entity.getCategories();
        this.foodFiles = entity.getFoodFiles();
    }*/

    @NonNull
    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(@NonNull String creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getFoodFiles() {
        return foodFiles;
    }

    public void setFoodFiles(List<String> foodFiles) {
        this.foodFiles = foodFiles;
    }
}
