package com.myapps.ron.family_recipes.model;

import java.util.List;
import java.util.Objects;

/**
 * Created by ronginat on 02/01/2019.
 *
 * data class for recycler view
 */
public class RecipeMinimal {

    private String id;
    private String name;
    private String description;
    private String uploader;
    private List<String> categories;
    private List<String> foodFiles;
    private int likes;

    public RecipeMinimal() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getFoodFiles() {
        return foodFiles;
    }

    public void setFoodFiles(List<String> foodFiles) {
        this.foodFiles = foodFiles;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeMinimal that = (RecipeMinimal) o;
        return likes == that.likes &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(uploader, that.uploader) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(foodFiles, that.foodFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, uploader, categories, foodFiles, likes);
    }

    @Override
    public String toString() {
        return "RecipeMinimal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
