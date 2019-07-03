package com.myapps.ron.family_recipes.layout.modelTO;

import com.google.gson.annotations.SerializedName;
import com.myapps.ron.family_recipes.model.PendingRecipeEntity;
import com.myapps.ron.family_recipes.layout.PostRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 20/02/2019.
 */
public class PendingRecipeTO implements PostRecipe {

    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("html")
    private String content;
    @SerializedName("categories")
    private List<String> categories;
    /*@SerializedName("foodFiles")
    private List<String> foodFiles;*/

    public PendingRecipeTO(PendingRecipeEntity entity) {
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.content = entity.getRecipeContent();
        this.categories = new ArrayList<>(entity.getCategories());
        //this.foodFiles = new ArrayList<>(entity.getFoodFiles());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLastModifiedDate() {
        return null;
    }

    @Override
    public String getRecipeContent() {
        return content;
    }

    /*@Override
    public List<String> getFoodFiles() {
        return foodFiles;
    }*/

    @Override
    public List<String> getCategories() {
        return categories;
    }
}
