package com.ronginat.family_recipes.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ronginat.family_recipes.logic.persistence.AppDatabases;
import com.ronginat.family_recipes.utils.Constants;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import static com.ronginat.family_recipes.utils.Constants.FALSE;
import static com.ronginat.family_recipes.utils.Constants.TRUE;

/**
 * Created by ronginat on 31/12/2018.
 */
//@Fts4
@Entity(tableName = AppDatabases.TABLE_RECIPES/*, indices = { @Index(value = {"name", "description"}), @Index("categories") }*/)
public class RecipeEntity implements Serializable {

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CATEGORIES = "categories";
    public static final String KEY_CREATED = "creationDate";
    public static final String KEY_MODIFIED = "lastModifiedDate";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_THUMBNAIL = "thumbnail";
    public static final String KEY_IMAGES = "images";
    public static final String KEY_LIKES = "likes";
    public static final String KEY_FAVORITE = "meLike";

    //@Ignore
    //public static String image = "https://api.androidhive.info/json/images/keanu.jpg";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = KEY_ID)
    private String id;
    @ColumnInfo(name = KEY_NAME)
    private String name;
    @ColumnInfo(name = KEY_DESCRIPTION)
    private String description;
    @ColumnInfo(name = KEY_CREATED)
    private String creationDate;
    @ColumnInfo(name = KEY_MODIFIED)
    private String lastModifiedDate;
    @ColumnInfo(name = KEY_AUTHOR)
    private String author;
    @ColumnInfo(name = KEY_THUMBNAIL)
    private String thumbnail;
    @ColumnInfo(name = KEY_CATEGORIES)
    private List<String> categories;
    @ColumnInfo(name = KEY_IMAGES)
    private List<String> images;
    @ColumnInfo(name = KEY_LIKES)
    private int likes;
    @ColumnInfo(name = KEY_FAVORITE)
    private int meLike;


    private static final Gson gson = new Gson();

    public RecipeEntity() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeEntity) {
            return getId().equals(((RecipeEntity)obj).getId());
        }
        return false;
        //return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, creationDate, lastModifiedDate, author, thumbnail, categories, images, likes, meLike);
    }

    /**
     * check all attributes
     * @param other - other recipe
     * @return whether or not current and second recipes are exactly the same
     */
    public boolean identical(RecipeEntity other) {
        boolean ids = Objects.equals(getId(), other.getId());
        boolean names = Objects.equals(getName(), other.getName());
        boolean descriptions = Objects.equals(getDescription(), other.getDescription());
        boolean uploaders = Objects.equals(getAuthor(), other.getAuthor());
        boolean thumbnails = Objects.equals(getThumbnail(), other.getThumbnail());
        boolean created = Objects.equals(getCreationDate(), other.getCreationDate());
        boolean modified = Objects.equals(getCreationDate(), other.getCreationDate());
        boolean likes = getLikes() == other.getLikes();

        boolean cats = getCategoriesToString().equals(other.getCategoriesToString());

        boolean images = getImagesToString().equals(other.getImagesToString());
        //boolean meLikes = getMeLike() == other.getMeLike();

        return ids && names && descriptions && uploaders && thumbnails && cats && created && modified
                && images && likes/* && meLikes*/;
    }

    private String getCategoriesToString() {
        if (getCategories() != null)
            return gson.toJson(getCategories());
        return "";
    }

    private void setStringCategories(String categories) {
        if (categories == null) {
            setCategories(null);
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> value = gson.fromJson(categories, type);
            setCategories(value);
        }
    }

    private String getImagesToString() {
        if (getImages() != null)
            return gson.toJson(getImages());
        return "";
    }

    private void setStringImages(String foodFiles) {
        if (foodFiles == null) {
            setImages(null);
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> value = gson.fromJson(foodFiles, type);
            setImages(value);
        }
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public int getMeLike() {
        return meLike;
    }

    public void setMeLike(int meLike) {
        this.meLike = meLike;
    }

    public boolean isUserLiked() {
        return this.meLike == TRUE;
    }

    public boolean hasTags(List<String> tags) {
        if (tags != null && !tags.isEmpty())
            return categories.containsAll(tags);
        else
            return true;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recipe{" +
                //"image='" + image + '\'' +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", author='" + author + '\'' +
                ", categories=" + categories +
                ", images=" + images +
                ", likes=" + likes +
                ", meLike=" + meLike +
                '}';
    }

    public static class Builder {
        private String builderId;
        private String builderName;
        private String builderDescription;
        private String builderCreatedAt;
        private String builderLastModifiedAt;
        private String builderAuthor;
        private String builderCategories;
        private String builderThumbnail;
        private String builderImages;
        private int builderLikes;

        private List<String> builderListCategories;

        private boolean builderMeLike;

        public Builder() {}

        public Builder id (String id) {
            this.builderId = id;
            return this;
        }

        public Builder name (String name) {
            this.builderName = name;
            return this;
        }

        public Builder description (String description) {
            this.builderDescription = description;
            return this;
        }

        public Builder creationDate(String createdAt) {
            this.builderCreatedAt = createdAt;
            return this;
        }

        public Builder lastModifiedAt (String lastModifiedAt) {
            this.builderLastModifiedAt = lastModifiedAt;
            return this;
        }

        public Builder author (String author) {
            this.builderAuthor = author;
            return this;
        }

        public Builder thumbnail (String thumbnail) {
            this.builderThumbnail = thumbnail;
            return this;
        }

        public Builder categoriesJson (String categoriesJson) {
            this.builderCategories = categoriesJson;
            return this;
        }

        public Builder categories (List<String> categories) {
            this.builderListCategories = categories;
            return this;
        }

        public Builder imagesJson (String foodFilesJson) {
            this.builderImages = foodFilesJson;
            return this;
        }

        public Builder likes (int likes) {
            this.builderLikes = likes;
            return this;
        }

        public Builder meLike (boolean meLike) {
            this.builderMeLike = meLike;
            return this;
        }

        public RecipeEntity build() {
            RecipeEntity recipe = new RecipeEntity();
            recipe.setId(builderId);
            recipe.setName(builderName);
            recipe.setDescription(builderDescription);
            recipe.setCreationDate(builderCreatedAt);
            recipe.setLastModifiedDate(builderLastModifiedAt);
            recipe.setAuthor(builderAuthor);
            recipe.setThumbnail(builderThumbnail);
            recipe.setStringCategories(builderCategories);
            recipe.setStringImages(builderImages);
            recipe.setLikes(builderLikes);
            recipe.setMeLike(builderMeLike ? TRUE : FALSE);

            return recipe;
        }


        public RecipeEntity buildTest() {
            RecipeEntity recipe = new RecipeEntity();
            recipe.setId(builderId);
            recipe.setName(builderName);
            recipe.setDescription(builderDescription);
            recipe.setCreationDate(builderCreatedAt);
            recipe.setLastModifiedDate(builderLastModifiedAt);
            recipe.setCategories(builderListCategories);
            recipe.setLikes(builderLikes);
            recipe.setMeLike(builderMeLike ? TRUE : FALSE);

            return recipe;
        }

    }
}
