package com.myapps.ron.family_recipes.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by ronginat on 20/02/2019.
 */
@Entity(tableName = "pendingRecipes")
public class PendingRecipeEntity {

    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CATEGORIES = "categories";
    public static final String KEY_CREATED = "creationDate";
    public static final String KEY_FOOD_FILES = "foodFiles";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = KEY_CREATED)
    private Date creationDate;
    @ColumnInfo(name = KEY_NAME)
    private String name;
    @ColumnInfo(name = KEY_DESCRIPTION)
    private String description;
    @ColumnInfo(name = "recipeFile")
    private String recipeFile;
    @ColumnInfo(name = KEY_CATEGORIES)
    private List<String> categories;
    @ColumnInfo(name = KEY_FOOD_FILES)
    private List<String> foodFiles;

    public PendingRecipeEntity() {
        this.creationDate = new Date();
        categories = new ArrayList<>();
        foodFiles = new ArrayList<>();
    }

    @NonNull
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(@NonNull Date creationDate) {
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

    public String getRecipeFile() {
        return recipeFile;
    }

    public void setRecipeFile(String recipeFile) {
        this.recipeFile = recipeFile;
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
