package com.myapps.ron.family_recipes.model;

import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * Created by ronginat on 14/01/2019.
 */
public class CommentEntity {
    private String message;
    private String user;
    private String date;

    public CommentEntity(){
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        if (message == null || user == null || date == null)
            return "null";
        return "CommentEntity{" +
                "message='" + message + '\'' +
                ", user='" + user + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentEntity comment = (CommentEntity) o;
        return this.message.equals(comment.message) &&
                this.user.equals(comment.user) &&
                this.date.equals(comment.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, user, date);
    }

}
