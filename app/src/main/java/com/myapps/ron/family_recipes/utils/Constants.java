package com.myapps.ron.family_recipes.utils;

import com.myapps.ron.family_recipes.model.RecipeEntity;

public class Constants {
    public static final String FIRST_APP_LAUNCH = "first_app_launch";
    public static final String NEW_FIREBASE_TOKEN = "new_firebase_token";

    // region Notifications
    public static final String BODY = "message";
    public static final String TITLE = "title";
    public static final String CHANNEL = "channel";
    public static final String ID = "id";
    public static final String NOTIFICATION = "notification";

    // endregion

    // region Splash Activity
    public static final String SPLASH_ACTIVITY_CODE = "splash_code";
    public static final String SPLASH_ACTIVITY_CODE_MAIN = "splash_code_main";
    public static final String SPLASH_ACTIVITY_CODE_POST = "splash_code_post";
    public static final String SPLASH_ACTIVITY_CODE_RECIPE = "splash_code_recipe";

    // endregion

    // region Main Activity

    public static final String MAIN_ACTIVITY_FIRST_FRAGMENT = "first_fragment";
    public static final String MAIN_ACTIVITY_FRAGMENT_ALL = "fragment_all";
    public static final String MAIN_ACTIVITY_FRAGMENT_FAVORITES = "fragment_favorites";

    public static final String FIRST_LOAD_FRAGMENT = "first_load";

    // endregion

    public static final String RECIPE_ID = "recipe";
    public static final String CONTENT_MODIFIED = "lastModified";
    public static final String RECIPE_PATH = "recipe_file";
    public static final String DEFAULT_RECIPE_PATH = "/recipe/default.html";

    public static final String ACTION_UPDATE_FROM_SERVICE = "update_from_service";
    public static final String ACTION_UPLOAD_IMAGES_SERVICE = "upload_images_service";

    //public static final int RECIPE_ACTIVITY_CODE = 0;
    public static final int POST_RECIPE_ACTIVITY_CODE = 1;

    public static final long FADE_ANIMATION_DURATION = 200;
    public static final long SCALE_ANIMATION_DURATION = 500L;

    public static final long REFRESH_DELAY = 60000; // 60 seconds

    public static final String DEFAULT_RECIPE_NAME = "anonymous";
    public static final String DEFAULT_RECIPE_DESC = "A sample app to showcase Cognito Identity and the SDK for Android.";
    public static final String DEFAULT_RECIPE_UPLOADER = "unknown";

    public static final int FALSE = 0;
    public static final int TRUE = 1;

    //RecipeActivity
    public static final int MAX_FILES_TO_UPLOAD = com.myapps.ron.family_recipes.layout.Constants.MAX_FILES_TO_UPLOAD;
    public static final String PAGER_FOOD_IMAGES = RecipeEntity.KEY_FOOD_FILES;
    public static final String ASSET_FILE_BASE_URL = "file:///android_asset/";

    //PostRecipeActivity
    public static final int MIN_NUMBER_OF_HTML_ELEMENTS = 2;

    public static final String DEFAULT_COLOR = "#827f93";

    //Dark Theme Preferences
    //public static final String[] DARK_THEME = {"always", "auto", "battery", "never"};
    public static final String DARK_THEME_ALWAYS = "always";
    public static final String DARK_THEME_NIGHT_BATTERY_SAVER = "auto";
    public static final String DARK_THEME_BATTERY_SAVER = "battery";
    public static final String DARK_THEME_NEVER = "never";


    // region Sample Recipe
    private final String HTML_SAMPLE_RECIPE_URL = "https://www.sugat.com/recipes/fast-easy-brownies/";
    public enum HTML_SAMPLE_SPINNER { HEADER, SUB_HEADER, PARAGRAPH, UNORDERED_LIST, ORDERED_LIST }
    public static final String HTML_SAMPLE_TEXT_CHECK_THE_PREVIEW = "כדאי לבדוק את התוצאה הסופית בלחיצה על כפתור התצוגה המקדימה";
    public static final String HTML_SAMPLE_TEXT_INGREDIENTS = "מרכיבים";
    public static final String HTML_SAMPLE_TEXT_INFO = "חומרים לתבנית בגודל 20X30 ס\"מ";
    public static final String HTML_SAMPLE_TEXT_HOW_TO_MAKE = "אופן הכנה";
    public static final String HTML_SAMPLE_INGREDIENT_LIST = "3 ביצים\n200 גרם חמאה\n 100 גרם אגוזי מלך";
    public static final String HTML_SAMPLE_HOW_TO_MAKE_STEPS_LIST = "מחממים תנור ל-170 מעלות ומשמנים את התבנית \nטורפים את הביצים בקערה במשך כחצי דקה \nמוסיפים חמאה ומאחדים לקרם חלק \nהבנתם את הרעיון";

    // endregion

}
