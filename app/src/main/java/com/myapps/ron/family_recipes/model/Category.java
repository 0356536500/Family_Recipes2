/*
package com.myapps.ron.family_recipes.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.searchfilter.model.FilterModel;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Category implements FilterModel, Parcelable {
    @SerializedName("name")
    private String name;
    @SerializedName("color")
    private String color;
    @SerializedName("categories")
    private List<Category> categories;


    private static final Gson gson = new Gson();

    public Category() {
    }

    private Category(Parcel in) {
        this.name = in.readString();
        this.color = in.readString();

        categories = new ArrayList<>();

        in.readList(this.categories, String.class.getClassLoader());
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

    */
/*public void setIntColor(int color) {
        this.color = "#" + Integer.toHexString(color & 0x00ffffff);
    }*//*


    public int getIntColor() {
        if (!color.equals(""))
            return Color.parseColor(color);
        return Color.parseColor(Constants.DEFAULT_COLOR);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public boolean hasSubCategories() {
        return categories != null && !categories.isEmpty();
    }

    public String getStringCategories() {
        return gson.toJson(getCategories());
    }

    public void setStringCategories(String categories) {
        //Log.e(getClass().getSimpleName(), "set string categories, " + categories);
        Type type = new TypeToken<List<Category>>() {}.getType();
        List<Category> value = gson.fromJson(categories, type);
        setCategories(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category tag = (Category) o;

        if (!getColor().equals(tag.getColor()))
            return false;
        return getText().equals(tag.getText());
    }

    public boolean fartherEquals(Category tag) {
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

    @NotNull
    @Override
    public String getText() {
        return getName();
    }

    @NotNull
    @Override
    public List<FilterModel> getSubs() {
        if (categories != null)
            return new ArrayList<>(categories);
        return new ArrayList<>();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", categories=" + categories +
                ", color=" + color +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.color);
        dest.writeList(this.categories);
    }

    public static class CategoryBuilder {
        private String builderName;
        private String builderColor;
        private String builderStringCategories = null;
        private List<Category> builderCategories = null;


        public CategoryBuilder() {}

        public CategoryBuilder name (String name) {
            this.builderName = name;
            return this;
        }

        public CategoryBuilder color (String color) {
            this.builderColor = color;
            return this;
        }

        public CategoryBuilder color (int color) {
            this.builderColor = "#" + Integer.toHexString(color & 0x00ffffff);
            return this;
        }

        public CategoryBuilder categories (List<Category> categories) {
            this.builderCategories = categories;
            return this;
        }

        public CategoryBuilder categories (String categories) {
            this.builderStringCategories = categories;
            return this;
        }


        public Category build() {
            Category category = new Category();
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
*/
