package com.ronginat.family_recipes.layout.modelTO;

import com.google.gson.annotations.SerializedName;
import com.ronginat.family_recipes.model.RecipeEntity;
import com.ronginat.family_recipes.utils.Constants;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;


/**
 * Created by ronginat on 31/12/2018.
 */
public class RecipeTO {

    //public static String image = "https://api.androidhive.info/json/images/keanu.jpg";

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("creationDate")
    private String creationDate;
    @SerializedName("lastModifiedDate")
    private String lastModifiedDate;
    @SerializedName("author")
    private String author;
    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("categories")
    private List<String> categories;
    @SerializedName("images")
    private List<String> images;
    @SerializedName("likes")
    private int likes;


    public RecipeTO() {
        super();
    }

    /*public RecipeTO (RecipeEntity recipe) {
        this();
        if (recipe != null) {
            this.id = recipe.getId();
            this.name = recipe.getName();
            this.description = recipe.getDescription();
            this.creationDate = recipe.getCreationDate();
            this.lastModifiedDate = recipe.getLastModifiedDate();
            this.uploader = recipe.getAuthor();
            this.thumbnail = recipe.getThumbnail();
            this.categories = recipe.getCategories();
            this.images = recipe.getFoodFiles();
            this.likes = recipe.getLikes();
        }
    }*/

    public RecipeEntity toEntity() {
        RecipeEntity rv = new RecipeEntity();
        rv.setId(this.id);
        rv.setName(this.name);
        rv.setDescription(this.description);
        rv.setCreationDate(this.creationDate);
        rv.setLastModifiedDate(this.lastModifiedDate);
        rv.setAuthor(this.author);
        rv.setThumbnail(this.thumbnail);
        rv.setCategories(this.categories);
        rv.setImages(this.images);
        rv.setLikes(this.likes);

        return rv;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeTO) {
            return getId().equals(((RecipeTO)obj).getId());
        }
        return false;
        //return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, creationDate, lastModifiedDate, author, thumbnail, categories, images, likes);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if(name != null)
            return name;
        return Constants.DEFAULT_RECIPE_NAME;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        if(description != null)
            return description;
        return Constants.DEFAULT_RECIPE_DESC;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreationDate() {
        if(creationDate != null)
            return creationDate;
        return com.ronginat.family_recipes.layout.Constants.DEFAULT_UPDATED_TIME;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getAuthor() {
        if(author != null)
            return author;
        return Constants.DEFAULT_RECIPE_AUTHOR;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }


    @NonNull
    @Override
    public String toString() {
        return "RecipeTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                /*", description='" + description + '\'' +*/
                ", creationDate='" + creationDate + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                /*", author='" + author + '\'' +*/
                ", thumbnail='" + thumbnail + '\'' +
                ", categories=" + categories +
                ", images=" + images +
                /*", likes=" + likes +*/
                '}';
    }

}
