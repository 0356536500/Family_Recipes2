package com.myapps.ron.family_recipes.network.modelTO;

import com.myapps.ron.family_recipes.network.PostRecipe;

import java.util.Date;
import java.util.List;

/**
 * Created by ronginat on 20/02/2019.
 */
public class PendingRecipeTO implements PostRecipe {

    private Date creationDate;
    private String name;
    private String description;
    private String recipeFile;
    private List<String> categories;
    private List<String> foodFiles;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getLastModifiedDate() {
        return null;
    }

    @Override
    public String getRecipeFile() {
        return null;
    }

    @Override
    public List<String> getFoodFiles() {
        return null;
    }

    @Override
    public List<String> getCategories() {
        return null;
    }
}
