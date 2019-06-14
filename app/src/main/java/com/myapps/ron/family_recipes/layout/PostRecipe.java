package com.myapps.ron.family_recipes.layout;

import java.util.List;

/**
 * Created by ronginat on 20/02/2019.
 */
public interface PostRecipe {
    String getName();
    String getDescription();
    String getLastModifiedDate();
    String getRecipeContent();
    List<String> getFoodFiles();
    List<String> getCategories();
}
