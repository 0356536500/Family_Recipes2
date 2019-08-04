package com.ronginat.family_recipes.recycler.helpers;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.ronginat.family_recipes.model.CategoryEntity;
import com.ronginat.family_recipes.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ronginat on 30/12/2018.
 */
public class RecipesAdapterHelper {
    private List<CategoryEntity> categories;
    // cache layer for categories color, possible because there is a small number of categories
    private Map<String,Integer> colorsMap;

    public RecipesAdapterHelper() {
        super();
        this.colorsMap = new HashMap<>();
    }

    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories;
    }

    public int getCategoryColor(@NonNull String name) {
        // first check the cache layer
        Integer color = colorsMap.get(name);
        if (color != null)
            return color;
        return getCategoryColorByName(name);
    }

    private int getCategoryColorByName(@NonNull String name) {
        if (categories != null) {
            for (CategoryEntity category : categories) {
                if (category.getName().equals(name)) {
                    colorsMap.put(name, category.getIntColor());
                    return category.getIntColor();
                }

                if (category.hasSubCategories()) {
                    int color = getSubCategoryColorByName(category.getCategories(), name);
                    if (color != -1) {
                        colorsMap.put(name, color);
                        return color;
                    }
                }
            }
        }
        return Color.parseColor(Constants.DEFAULT_COLOR);
    }

    private int getSubCategoryColorByName(@NonNull List<CategoryEntity> categoryList, @NonNull String name) {
        int color = - 1;
        for (CategoryEntity category: categoryList) {
            if (category.getName().equals(name)) {
                color = category.getIntColor();
                break;
            } else {
               if (category.hasSubCategories()) {
                   color = getSubCategoryColorByName(category.getCategories(), name);
                   if (color != -1)
                       break;
               }
            }
        }
        return color;
    }
}
