package com.myapps.ron.family_recipes.dal.db;

import android.content.Context;

import com.myapps.ron.family_recipes.model.Recipe;

import java.util.List;

/*
    update recipes db when new data arrives
 */
public class RecipesDBMaintainer {

    //private Context context;
    private RecipesDBHelper dbHelper;

    public RecipesDBMaintainer(Context context) {
        //this.context = context;
        dbHelper = new RecipesDBHelper(context);
    }

    public void updateRecipes(List<Recipe> recipes) {
        for(Recipe item : recipes) {
            if(dbHelper.recipeExists(item.getId()))
                dbHelper.updateRecipeServerChanges(item);
            else
                dbHelper.insertRecipe(item);
        }
    }
}
