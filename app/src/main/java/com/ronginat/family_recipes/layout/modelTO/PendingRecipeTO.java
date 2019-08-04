package com.ronginat.family_recipes.layout.modelTO;

import com.google.gson.annotations.SerializedName;
import com.ronginat.family_recipes.model.PendingRecipeEntity;
import com.ronginat.family_recipes.layout.PostRecipe;

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
    /*@SerializedName("images")
    private List<String> images;*/

    public PendingRecipeTO(PendingRecipeEntity entity) {
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.content = entity.getContent();
        this.categories = new ArrayList<>(entity.getCategories());
        //this.images = new ArrayList<>(entity.getFoodFiles());
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
        return images;
    }*/

    @Override
    public List<String> getCategories() {
        return categories;
    }
}
