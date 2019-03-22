package com.myapps.ron.family_recipes.network.modelTO;

import com.google.gson.annotations.SerializedName;
import com.myapps.ron.family_recipes.model.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by ronginat on 01/01/2019.
 */
public class CategoryTO {
    @SerializedName("name")
    private String name;
    @SerializedName("color")
    private String color;
    @SerializedName("categories")
    private List<CategoryTO> categories;


    public CategoryTO() {
        super();
    }

    /*public CategoryTO(CategoryEntity categoryEntity) {
        if (categoryEntity != null) {
            setName(categoryEntity.getName());
            setColor(categoryEntity.getColor());
            if (categoryEntity.getCategories() != null) {
                this.categories = new ArrayList<>();
                for (CategoryEntity category: categoryEntity.getCategories()) {
                    this.categories.add(new CategoryTO(category));
                }
            }
        }
    }*/

    public CategoryEntity toEntity() {
        CategoryEntity rv = new CategoryEntity();
        rv.setName(this.name);
        rv.setColor(this.color);
        List<CategoryEntity > categoriesRv = null;
        if (this.categories != null) {
            categoriesRv = new ArrayList<>();
            for (CategoryTO category: this.categories) {
                categoriesRv.add(category.toEntity());
            }
        }
        rv.setCategories(categoriesRv);

        return rv;
    }

    public String getName() {
        return name;
    }

    public void setName(String text) {
        this.name = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<CategoryTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryTO> categories) {
        this.categories = categories;
    }

    /*public boolean hasSubCategories() {
        return categories != null && !categories.isEmpty();
    }*/


    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getColor().hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryTO{" +
                "name='" + name + '\'' +
                ", categories=" + categories +
                ", color=" + color +
                '}';
    }


}
