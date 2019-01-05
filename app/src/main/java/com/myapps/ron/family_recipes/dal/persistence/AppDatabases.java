package com.myapps.ron.family_recipes.dal.persistence;

import android.content.Context;

import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Created by ronginat on 31/12/2018.
 */
@Database(entities = {RecipeEntity.class, CategoryEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabases extends RoomDatabase {

    private static volatile AppDatabases INSTANCE;

    private static final String DATABASE_NAME = "family_recipes.db";

    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_CATEGORIES = "categories";

    public abstract RecipeDao recipeDao();
    public abstract CategoryDao categoriesDao();

    public static AppDatabases getInstance(Context context)  {
        if (INSTANCE == null) {
            synchronized (AppDatabases.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabases.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .addCallback(new Callback() {
                                /*@Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Executors.newSingleThreadScheduledExecutor().execute(() -> {
                                        getInstance(context).recipeDao().insertAll(generateData("recipe", 150));
                                    });
                                }*/

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    Executors.newSingleThreadScheduledExecutor().execute(() -> {
                                        RecipeDao recipeDao = getInstance(context).recipeDao();
                                        recipeDao.deleteAllRecipes();
                                        recipeDao.insertAll(generateData("recipe", 15));
                                        recipeDao.insertAll(generateData("uirass", 15));
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static List<RecipeEntity> generateData(String name, int size) {
        List<RecipeEntity> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(new RecipeEntity.RecipeBuilder()
            .id(name + i)
            .name(name + i)
            .description("desc " + name + i)
            .creationDate(DateUtil.getUTCTime())
            .build());
        }
        return data;
    }
}
