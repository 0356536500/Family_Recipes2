package com.myapps.ron.family_recipes.network.modelTO;

import com.google.gson.annotations.SerializedName;
import com.myapps.ron.family_recipes.model.CommentEntity;

import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * Created by ronginat on 14/01/2019.
 */
public class CommentTO {
    @SerializedName("message")
    private String message;
    @SerializedName("user")
    private String user;
    @SerializedName("creationDate")
    private String creationDate;

    public CommentTO() {
        super();
    }

    /*public CommentTO(CommentEntity comment) {
        this();
        if (comment != null) {
            this.message = comment.getMessage();
            this.user = comment.getUser();
            this.creationDate = comment.getDate();
        }
    }*/

    public CommentEntity toEntity() {
        CommentEntity entity = new CommentEntity();
        entity.setMessage(this.message);
        entity.setUser(this.user);
        entity.setDate(this.creationDate);
        return entity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @NonNull
    @Override
    public String toString() {
        if (message == null || user == null || creationDate == null)
            return "null";
        return "CommentTO{" +
                "message='" + message + '\'' +
                ", user='" + user + '\'' +
                ", creationDate='" + creationDate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentTO comment = (CommentTO) o;
        return this.message.equals(comment.message) &&
                this.user.equals(comment.user) &&
                this.creationDate.equals(comment.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, user, creationDate);
    }
}
