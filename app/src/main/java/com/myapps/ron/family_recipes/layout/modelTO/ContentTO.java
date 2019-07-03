package com.myapps.ron.family_recipes.layout.modelTO;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.myapps.ron.family_recipes.model.ContentEntity;

/**
 * Created by ronginat on 20/06/2019
 */
public class ContentTO {

    @SerializedName(value = "id")
    private String id;
    @SerializedName(value = "name")
    private String name;
    @SerializedName(value = "lastModifiedDate")
    private String lastModifiedDate;
    @SerializedName(value = "html")
    private String content;

    public ContentTO() {
        super();
    }

    public ContentEntity toEntity() {
        ContentEntity rv = new ContentEntity();
        rv.setRecipeId(this.id);
        rv.setLastModifiedDate(this.lastModifiedDate);
        rv.setContent(this.content);
        return rv;
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

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public String toString() {
        return "ContentTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
