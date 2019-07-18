package com.ronginat.family_recipes.logic.persistence;

import android.content.Context;

import com.ronginat.family_recipes.model.AccessEntity;
import com.ronginat.family_recipes.model.CategoryEntity;
import com.ronginat.family_recipes.model.ContentEntity;
import com.ronginat.family_recipes.model.PendingRecipeEntity;
import com.ronginat.family_recipes.model.RecipeEntity;
import com.ronginat.family_recipes.utils.logic.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Created by ronginat on 31/12/2018.
 */
@Database(entities = {RecipeEntity.class, CategoryEntity.class, ContentEntity.class, PendingRecipeEntity.class, AccessEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabases extends RoomDatabase {

    private static volatile AppDatabases INSTANCE;

    private static final String DATABASE_NAME = "family_recipes.db";

    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_CONTENTS = "contents";
    public static final String TABLE_PENDING_RECIPES = "pendingRecipes";
    public static final String TABLE_ACCESS = "access";

    public abstract RecipeDao recipeDao();
    public abstract CategoryDao categoriesDao();
    public abstract PendingRecipeDao pendingRecipeDao();

    public static AppDatabases getInstance(Context context)  {
        if (INSTANCE == null) {
            synchronized (AppDatabases.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabases.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            //.allowMainThreadQueries()
                            /*.addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Executors.newSingleThreadScheduledExecutor().execute(() ->
                                            getInstance(context)
                                                    .categoriesDao()
                                                    .insertCategory(
                                                            new CategoryEntity.CategoryBuilder()
                                                                    .name(context.getString(R.string.str_all_selected))
                                                                    .color(ContextCompat.getColor(context, R.color.search_filter_text_light))
                                                                    .build()
                                                ));
                                }
                            })*/
                            /*.addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    Executors.newSingleThreadScheduledExecutor().execute(() -> {
                                        RecipeDao recipeDao = getInstance(context).recipeDao();
                                        recipeDao.deleteAllRecipes();
                                        //recipeDao.insertAll(generate5Recipes());
                                        //recipeDao.insertAll(generateData("recipe", 15));
                                        //recipeDao.insertAll(generateData("tirass", 15));
                                    });
                                }
                            })*/
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /*private static List<RecipeEntity> generate5Recipes() {
        List<RecipeEntity> recipeEntities = new ArrayList<>();
        recipeEntities.add(new RecipeEntity.RecipeBuilder()
                .id("1")
                .name("מרק עוף")
                .meLike(false)
                .build());
        recipeEntities.add(new RecipeEntity.RecipeBuilder()
                .id("2")
                .name("alef")
                .meLike(false)
                .build());
        recipeEntities.add(new RecipeEntity.RecipeBuilder()
                .id("3")
                .name("bet")
                .meLike(false)
                .build());
        recipeEntities.add(new RecipeEntity.RecipeBuilder()
                .id("4")
                .name("פסטה פרמזן")
                .meLike(true)
                .build());
        recipeEntities.add(new RecipeEntity.RecipeBuilder()
                .id("5")
                .name("עוף בגריל")
                .meLike(true)
                .build());

        return recipeEntities;
    }*/

    public static List<RecipeEntity> generateData(String name, int size) {
        List<RecipeEntity> data = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                List<String> categories = new ArrayList<>();
                int pos = random.nextInt(cats.length - 1) + 1;
                categories.add(cats[pos]);
                categories.add(cats[pos - 1]);

                data.add(new RecipeEntity.RecipeBuilder()
                        .id(name + i)
                        .name(name + i)
                        .description("desc " + name + i)
                        .creationDate(DateUtil.getUTCTime())
                        .categories(categories)
                        .buildTest());

                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*for (RecipeEntity recipe: data) {
            Log.e(AppDatabases.class.getSimpleName(), recipe.toString());
        }*/
        return data;
    }

    private static Random random = new Random(System.currentTimeMillis());
    private static String[] cats = {"a", "b", "c", "d", "e", "f"};
}
