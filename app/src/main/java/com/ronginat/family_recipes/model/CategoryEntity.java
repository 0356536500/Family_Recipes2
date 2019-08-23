package com.ronginat.family_recipes.model;

import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ronginat.family_recipes.logic.persistence.AppDatabases;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.searchfilter.model.FilterModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by ronginat on 01/01/2019.
 */
@Entity(tableName = AppDatabases.TABLE_CATEGORIES)
@SuppressWarnings("WeakerAccess")
public class CategoryEntity implements FilterModel {
    public static final String KEY_NAME = "name";
    public static final String KEY_COLOR = "color";
    public static final String KEY_CATEGORIES = "categories";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = KEY_NAME)
    private String name;
    @ColumnInfo(name = KEY_COLOR)
    private String color;
    @ColumnInfo(name = KEY_CATEGORIES)
    private List<CategoryEntity> categories;

    private static final Gson gson = new Gson();

    public CategoryEntity() {
        super();
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String text) {
        this.name = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /*public void setIntColor(int color) {
        this.color = "#" + Integer.toHexString(color & 0x00ffffff);
    }*/

    public int getIntColor() {
        if (color != null && !color.equals(""))
            return Color.parseColor(color);
        return Color.parseColor(Constants.DEFAULT_COLOR);
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories;
    }

    public boolean hasSubCategories() {
        return categories != null && !categories.isEmpty();
    }

    public String getStringCategories() {
        return gson.toJson(getCategories());
    }

    private void setStringCategories(String categories) {
        //Log.e(getClass().getSimpleName(), "set string categories, " + categories);
        Type type = new TypeToken<List<CategoryEntity>>() {}.getType();
        List<CategoryEntity> value = gson.fromJson(categories, type);
        setCategories(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryEntity)) return false;

        CategoryEntity tag = (CategoryEntity) o;

        if (!Objects.equals(getColor(), tag.getColor()))
        //if (!getColor().equals(tag.getColor()))
            return false;
        return getText().equals(tag.getText());
    }

    public boolean fartherEquals(CategoryEntity tag) {
        if (!getColor().equals(tag.getColor()))
            return false;
        if (!getText().equals(tag.getText()))
            return false;

        if (categories != null && tag.categories != null) {
            if (categories.size() != tag.categories.size())
                return false;
            for (int i = 0; i < categories.size(); i++) {
                if (!categories.get(i).equals(tag.categories.get(i)))
                    return false;
            }
            return true;
        }
        // if both null return 'true'
        return categories == tag.categories;
    }

    @Override
    public int hashCode() {
        int result = getText().hashCode();
        result = 31 * result + getIntColor();
        return result;
    }

    @NonNull
    @Override
    public String getText() {
        return getName();
    }

    @NonNull
    @Override
    public List<FilterModel> getSubs() {
        if (categories != null)
            return new ArrayList<>(categories);
        return new ArrayList<>();
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryEntity{" +
                "name='" + name + '\'' +
                ", categories=" + categories +
                ", color=" + color +
                '}';
    }


    public static class Builder {
        private String builderName;
        private String builderColor;
        private String builderStringCategories = null;
        private List<CategoryEntity> builderCategories = null;


        public Builder() {}

        public Builder name (@NonNull String name) {
            this.builderName = name;
            return this;
        }

        public Builder color (String color) {
            this.builderColor = color;
            return this;
        }

        public Builder color (int color) {
            this.builderColor = "#" + Integer.toHexString(color & 0x00ffffff);
            return this;
        }

        public Builder categories (List<CategoryEntity> categories) {
            this.builderCategories = categories;
            return this;
        }

        public Builder categories (String categories) {
            this.builderStringCategories = categories;
            return this;
        }


        public CategoryEntity build() {
            CategoryEntity category = new CategoryEntity();
            category.setName(builderName);
            category.setColor(builderColor);
            if (builderCategories != null)
                category.setCategories(builderCategories);
            else if (builderStringCategories != null)
                category.setStringCategories(builderStringCategories);

            return category;
        }

    }
}
