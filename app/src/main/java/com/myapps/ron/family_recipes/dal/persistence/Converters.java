package com.myapps.ron.family_recipes.dal.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import java.lang.reflect.Type;
import java.util.List;

import androidx.room.TypeConverter;

/**
 * Created by ronginat on 01/01/2019.
 */
@SuppressWarnings("WeakerAccess")
public class Converters {

    private static Gson gson = new Gson();

    @TypeConverter
    public static List<String> fromString(String value) {
        Type type = new TypeToken<List<String>>() { }
                .getType();
        return gson.fromJson(value, type);
    }

    @TypeConverter
    public List<RecipeEntity.Comment> fromStringComments(String comments) {
        Type type = new TypeToken<List<RecipeEntity.Comment>>() {
        }.getType();
        return gson.fromJson(comments, type);
    }

    @TypeConverter
    public static String ListCommentToString(List<RecipeEntity.Comment> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static String ListToString(List<String> list) {
        return gson.toJson(list);
    }
}
