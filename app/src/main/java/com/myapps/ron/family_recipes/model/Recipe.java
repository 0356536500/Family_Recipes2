package com.myapps.ron.family_recipes.model;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class Recipe {

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("lastModifiedAt")
    private String lastModifiedAt;
    @SerializedName("recipeFile")
    private String recipeFile;
    @SerializedName("uploader")
    private String uploader;
    @SerializedName("categories")
    private List<String> categories;
    @SerializedName("comments")
    private List<String> comments;
    @SerializedName("foodFiles")
    private List<String> foodFiles;
    @SerializedName("likes")
    private int likes;
/*    @SerializedName("sharedKey")
    private String sharedKey;*/

    /*private String id;
    private String name;
    private String description;
    private String createdAt;
    private String lastModifiedAt;
    private String recipeFile;
    private String uploader;
    private Set<String> categories;
    private List<String> comments;
    private List<String> foodFiles;
    private int likes;
    private String sharedKey;*/

    private static final Gson gson = new Gson();

    public Recipe() {
    }

    public Recipe(String id, String name, String description, String createdAt,
                  String lastModifiedAt, String recipeFile, String uploader,
                  @Nullable List<String> categories, @Nullable List<String> comments, @Nullable List<String> foodFiles,
                  int likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.recipeFile = recipeFile;
        this.uploader = uploader;
        this.categories = categories;
        this.comments = comments;
        this.foodFiles = foodFiles;
        this.likes = likes;
    }

    public Recipe(String id, String name, String description, String createdAt,
                  String lastModifiedAt, String recipeFile, String uploader,
                  String categories, String comments, String foodFiles,
                  int likes) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.recipeFile = recipeFile;
        this.uploader = uploader;
        this.likes = likes;

        setStringCategories(categories);
        setStringComments(comments);
        setStringFoodFiles(foodFiles);
    }

    public String getStringCategories() {
        return gson.toJson(getCategories());
    }

    public void setStringCategories(String categories) {
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> value = gson.fromJson(categories, type);
        setCategories(value);
    }

    public String getStringComments() {
        return gson.toJson(getComments());
    }

    public void setStringComments(String comments) {
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> value = gson.fromJson(comments, type);
        setComments(value);
    }

    public String getStringFoodFiles() {
        return gson.toJson(getFoodFiles());
    }

    public void setStringFoodFiles(String foodFiles) {
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> value = gson.fromJson(foodFiles, type);
        setFoodFiles(value);
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(String lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getRecipeFile() {
        return recipeFile;
    }

    public void setRecipeFile(String recipeFile) {
        this.recipeFile = recipeFile;
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

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
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
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", lastModifiedAt='" + lastModifiedAt + '\'' +
                ", recipeFile='" + recipeFile + '\'' +
                ", uploader='" + uploader + '\'' +
                ", categories=" + categories +
                ", comments=" + comments +
                ", foodFiles=" + foodFiles +
                ", likes=" + likes +
                '}';
    }
}
