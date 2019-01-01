package com.myapps.ron.family_recipes.dal.persistence;

import android.content.Context;

import com.myapps.ron.family_recipes.dal.persistence.RecipeDao;
import com.myapps.ron.family_recipes.model.RecipeEntity;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Created by ronginat on 31/12/2018.
 */
@Database(entities = {RecipeEntity.class/*, Category.class*/}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabases extends RoomDatabase {

    private static volatile AppDatabases INSTANCE;

    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_CATEGORIES = "categories";

    public abstract RecipeDao recipeDao();
    //public abstract CategoriesDao categoriesDao();

    public static AppDatabases getInstance(Context context)  {
        if (INSTANCE == null) {
            synchronized (AppDatabases.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabases.class, "Data.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
