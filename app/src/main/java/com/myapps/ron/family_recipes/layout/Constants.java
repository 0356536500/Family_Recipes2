package com.myapps.ron.family_recipes.layout;

public class Constants {

    //retrofit headers
    static final String CONTENT_TYPE = "Content-Type";
    static final String AUTHORIZATION = "Authorization";

    //retrofit request queries
    //static final String DATE_QUERY = "lastModified";
    static final String ID_QUERY = "id";
    static final String LAST_MODIFIED_QUERY = "lastModifiedDate";
    static final String EXCLUSIVE_START_KEY_QUERY = "Last-Evaluated-Key";
    static final String LIMIT_QUERY = "limit";
    static final String QUERY_PLATFORM = "platform";
    static final String QUERY_NEW_RECIPE_SUBSCRIPTION = "newRecipes";
    static final String QUERY_COMMENTS_SUBSCRIPTION = "comments";
    static final String QUERY_LIKES_SUBSCRIPTION = "likes";
    //retrofit request path parameters
    static final String PATH_DEVICE_ID = "device";
    static final String PATH_TOKEN = "token";

    //Subscription flags
    public static final String SUBSCRIPTION_SUBSCRIBE = "subscribe";
    public static final String SUBSCRIPTION_UNSUBSCRIBE = "unsubscribe";
    public static final String SUBSCRIPTION_CHANGE_POLICY = "changePolicy";

    //GET userDetails response keys
    public static final String RESPONSE_KEY_SUBSCRIPTIONS = "subscriptions";
    public static final String RESPONSE_KEY_FAVORITES = "favorites";

    //GET detailsForUpdate response keys
    public static final String RESPONSE_KEY_APP_URL = "url";
    public static final String RESPONSE_KEY_APP_NAME = "name";

    //POST pend recipe response keys
    public static final String RESPONSE_KEY_URL = "url";
    public static final String RESPONSE_KEY_RECIPE_ID = "id";
    public static final String RESPONSE_KEY_RECIPE_MODIFIED = "lsatModifiedDate";

    //POST body elements
    public static final int MIN_TAGS = 2;
    static final String POSTED_NAME = "name";
    static final String POSTED_DESCRIPTION = "description";
    static final String POSTED_CONTENT = "html";
    static final String POSTED_CATEGORIES = "categories";
    static final String RECIPE_ITEM = "recipe";

    //GET request food urls queries
    static final String NUM_OF_FILES_QUERY = "numOfFiles";
    static final String EXTENSION_QUERY = "extension";

    static final String NUM_FILES_TO_UPLOAD = "numOfFiles";
    static final String PUT_FOOD_EXTENSION = "extension";

    public static final int MAX_FILES_TO_UPLOAD = 3;
    //PATCH body elements
    //public static final String ATTRIBUTES = "attributes";
    public static final String LIKES = "likes";
    //POST comment body element
    public static final String COMMENT = "comment";
    static final String COMMENTS = "comments";
    /*public static final String COMMENT_MESSAGE = "message";
    public static final String COMMENT_USER = "user";*/

    // response headers
    public static final String HEADER_LAST_EVAL_KEY = "Last-Evaluated-Key";

    //urls
    static final String URL_RECIPES = "recipes";
    static final String URL_CATEGORIES = "recipes/categories";
    static final String URL_CONTENT = "content";
    static final String URL_FOOD = "food";
    static final String URL_USERS = "users";
    static final String URL_SUBSCRIPTIONS = URL_USERS + "/subscriptions";
    static final String URL_TOKENS = URL_USERS + "/tokens";
    static final String BASE_URL = "https://eku11o83ch.execute-api.eu-west-2.amazonaws.com/dev/";
    //android url
    //public static final String ANDROID_URL = "https://familyrecipes.com/recipe/";
    //public static final String ANDROID_URL_INTENT_TYPE = "text/plain";

    public static final String DEFAULT_UPDATED_TIME = "2018-09-09T12:00:00.000Z";
    public static final int CATEGORIES_ELAPSED_TIME_TO_UPDATE = 7; //days

    // S3/Util constants
    public static final String COGNITO_POOL_REGION = "eu-west-2";
    public static final String BUCKET_REGION = "eu-west-2";
    public static final String BUCKET_NAME = "my-family-recipes";
    public static final String THUMB_DIR = "thumbnails";
    public static final String FOOD_DIR = "food-pictures";
    public static final String RECIPES_DIR = "recipes";
    public static final String APK_DIR = "apk";
    public static final String FILE_PREFIX = "file:///";

    //Cognito
    public static final String COGNITO_POOL_ID = "eu-west-2_q4gQJK1TO";
    public static final String COGNITO_CLIENT_ID = "g5a06641tr4f82fr5v366uvku";
    public static final String COGNITO_CLIENT_SECRET = "ufjleb4rj3hofsub84aguoqlnap7p6kg8lr2qet4epipi34375d";
    public static final com.amazonaws.regions.Regions COGNITO_REGION = com.amazonaws.regions.Regions.EU_WEST_2;

    public static final String COGNITO_IDENTITY_POOL_ID = "eu-west-2:8b51a9b1-9fc6-46aa-aa8b-53bee06bfb91";
    public static final String COGNITO_IDENTITY_LOGIN = "cognito-idp." + COGNITO_POOL_REGION + ".amazonaws.com/" + COGNITO_POOL_ID;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

}
