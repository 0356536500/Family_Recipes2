package com.myapps.ron.family_recipes.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.myapps.ron.searchfilter.model.FilterModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Category implements FilterModel, Parcelable {
    @SerializedName("name")
    private String name;
    @SerializedName("categories")
    private List<Category> categories;
    private int color;

    private static final Gson gson = new Gson();

    public Category() {
    }

    public Category(String text, int color) {
        this.name = text;
        this.color = color;
        this.categories = null;
    }

    public Category(String text, int color, List<String> categories) {
        this.name = text;
        this.color = color;
        this.categories = new ArrayList<>();
        for (String str: categories) {
            this.categories.add(new Category(str, this.color));
        }
    }

    public Category(String text, String categories, int color) {
        this.name = text;
        this.color = color;
        setStringCategories(categories);
    }

    private Category(Parcel in) {
        this.name = in.readString();
        this.color = in.readInt();

        categories = new ArrayList<>();

        in.readList(this.categories, String.class.getClassLoader());
    }

    public String getName() {
        return name;
    }

    public void setName(String text) {
        this.name = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getStringCategories() {
        return gson.toJson(getCategories());
    }

    public void setStringCategories(String categories) {
        Type type = new TypeToken<List<String>>() {}.getType();
        List<Category> value = gson.fromJson(categories, type);
        setCategories(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category tag = (Category) o;

        if (getColor() != tag.getColor()) return false;
        return getText().equals(tag.getText());

    }

    @Override
    public int hashCode() {
        int result = getText().hashCode();
        result = 31 * result + getColor();
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

        return new ArrayList<>(categories);
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
        dest.writeInt(this.color);
        dest.writeList(this.categories);
    }
}
