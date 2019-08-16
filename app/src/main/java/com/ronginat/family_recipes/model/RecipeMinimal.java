package com.ronginat.family_recipes.model;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * Created by ronginat on 02/01/2019.
 *
 * data class for recycler view
 */
public class RecipeMinimal {

    private String id;
    private String lastModifiedDate;
    private String name;
    private String description;
    private String author;
    private List<String> categories;
    private String thumbnail;
    private int likes;
    private int meLike;

    public RecipeMinimal() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getMeLike() {
        return meLike;
    }

    public void setMeLike(int meLike) {
        this.meLike = meLike;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeMinimal that = (RecipeMinimal) o;
        return
                Objects.equals(likes, that.likes) &&
                Objects.equals(meLike, that.meLike) &&
                Objects.equals(id, that.id) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(author, that.author) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(thumbnail, that.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, author, categories, thumbnail, likes, meLike);
    }

    @NonNull
    @Override
    public String toString() {
        return "RecipeMinimal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
