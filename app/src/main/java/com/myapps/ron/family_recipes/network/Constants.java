package com.myapps.ron.family_recipes.network;

public class Constants {

    //retrofit headers
    public static final String DATE_QUERY = "lastModified";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";

    //POST body elements
    public static final String NUM_FILES_TO_UPLOAD = "numOfFiles";
    public static final String RECIPE_ITEM = "recipe";

    //PATCH body elements
    public static final String ATTRIBUTES = "attributes";

    //urls
    public static final String URL_RECIPES = "recipes";
    public static final String URL_CATEGORIES = "recipes/categories";
    public static final String URL_FOOD = "food";
    public static final String BASE_URL = "https://eku11o83ch.execute-api.eu-west-2.amazonaws.com/dev/";

    public static final String DEFAULT_UPDATED_TIME = "2018-09-09 12:00:00";
}
