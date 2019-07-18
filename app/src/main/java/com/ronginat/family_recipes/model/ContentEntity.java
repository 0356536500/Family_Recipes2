package com.ronginat.family_recipes.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.ronginat.family_recipes.logic.persistence.AppDatabases;

import java.util.Objects;

/**
 * Created by ronginat on 20/06/2019
 */

@Entity(tableName = AppDatabases.TABLE_CONTENTS, foreignKeys =
@ForeignKey(entity = RecipeEntity.class, parentColumns = RecipeEntity.KEY_ID, childColumns = ContentEntity.KEY_ID, onDelete = ForeignKey.CASCADE))
public class ContentEntity {
    public static final String KEY_ID = "id";
    public static final String KEY_MODIFIED = "lastModifiedDate";
    public static final String KEY_CONTENT = "html";

    @SuppressWarnings("NullableProblems")
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = KEY_ID)
    private String recipeId;
    @ColumnInfo(name = KEY_MODIFIED)
    private String lastModifiedDate;
    @ColumnInfo(name = KEY_CONTENT)
    private String content;

    public ContentEntity() {
        super();
    }

    @NonNull
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(@NonNull String recipeId) {
        this.recipeId = recipeId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentEntity that = (ContentEntity) o;
        return recipeId.equals(that.recipeId) &&
                lastModifiedDate.equals(that.lastModifiedDate) &&
                content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeId, lastModifiedDate, content);
    }

    @NonNull
    @Override
    public String toString() {
        return "ContentEntity{" +
                "recipeId='" + recipeId + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
