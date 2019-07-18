package com.ronginat.family_recipes;

import android.content.Context;
import android.util.Log;

import com.ronginat.family_recipes.logic.persistence.AppDatabases;
import com.ronginat.family_recipes.logic.persistence.PendingRecipeDao;
import com.ronginat.family_recipes.logic.persistence.RecipeDao;
import com.ronginat.family_recipes.model.AccessEntity;
import com.ronginat.family_recipes.model.AccessEntity.RecipeAccess;
import com.ronginat.family_recipes.model.PendingRecipeEntity;
import com.ronginat.family_recipes.model.RecipeEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by ronginat on 05/04/2019.
 */
@RunWith(AndroidJUnit4.class)
public class RoomJoinTest {
    private String TAG = getClass().getSimpleName();
    private RecipeDao recipeDao;
    private AppDatabases db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabases.class).build();
        recipeDao = db.recipeDao();
        //recipeDao.deleteAllRecipes();
    }

    @After
    public void closeDb() {
        recipeDao.deleteAllRecipes();
        db.close();
    }

    @Test
    public void writeRecipesWithAccessAndGetFirstAccess() {
        /*RecipeEntity recipe = new RecipeEntity.RecipeBuilder()
                .id("1")
                .name("test name")
                .description("test description")
                .buildTest();

        recipeDao.insertRecipe(recipe);
        List<RecipeEntity> byName = recipeDao.findRecipesByName("test name");
        assertThat(byName.get(0), equalTo(recipe));*/

        List<RecipeEntity> list = AppDatabases.generateData("test", 3);
        recipeDao.insertAll(list);

        Long firstAccess = null;
        for (RecipeEntity recipe: list) {
            AccessEntity access = new AccessEntity();
            access.setRecipeId(recipe.getId());
            long now = new Date().getTime();
            if (firstAccess == null)
                firstAccess = now;
            access.setLastAccessedThumbnail(now);
            recipeDao.insertRecipeAccess(access);
        }

        AccessEntity accessEntity = recipeDao.getAccessById(list.get(0).getId());
        assertThat(accessEntity.getLastAccessedThumbnail(), equalTo(firstAccess));
        assertThat(accessEntity.getLastAccessedContent(), nullValue());
        assertEquals(recipeDao.getAllRecipeAccess().size(), recipeDao.findAllSync().size());
    }

    @Test
    public void getAccessesByThumbTest() {
        TAG = "getAccessesByThumbTest";
        List<RecipeEntity> list = AppDatabases.generateData("test", 4);
        recipeDao.insertAll(list);

        for (RecipeEntity recipe: list.subList(0, 3)) {
            AccessEntity access = new AccessEntity();
            access.setRecipeId(recipe.getId());
            access.setLastAccessedThumbnail(new Date().getTime());
            recipeDao.insertRecipeAccess(access);
        }

        List<RecipeAccess> recipeAccesses = recipeDao.getAccessTimeOrderByThumb();
        Log.e(TAG, "recipe access in db:");
        for (RecipeAccess access: recipeAccesses) {
            Log.e(TAG, access.toString());
        }
        assertEquals(recipeAccesses.size(), list.size() - 1);
        //assertNull(recipeAccesses.get(0).lastAccessedRecipe);
        assertNotNull(recipeAccesses.get(2).lastAccessedThumbnail);
    }

    @Test
    public void getAccessesByImagesTest() {
        TAG = "getAccessesByImagesTest";
        List<RecipeEntity> list = AppDatabases.generateData("test", 4);
        recipeDao.insertAll(list);

        //Log.e(TAG, "recipes in db:");
        for (RecipeEntity recipe: list.subList(0, 3)) {
            //Log.e(TAG, recipe.toString());
            AccessEntity access = new AccessEntity();
            access.setRecipeId(recipe.getId());
            access.setLastAccessedImages(new Date().getTime());
            recipeDao.insertRecipeAccess(access);
        }

        List<RecipeAccess> recipeAccesses = recipeDao.getAccessTimeOrderByImages();
        Log.e(TAG, "recipe access in db:");
        for (RecipeAccess access: recipeAccesses) {
            Log.e(TAG, access.toString());
        }
        assertEquals(recipeAccesses.size(), list.size() - 1);
        //assertNull(recipeAccesses.get(0).lastAccessedRecipe);
        assertNotNull(recipeAccesses.get(1).lastAccessedImages);
    }

    @Test
    public void writeAndReadRecipeWithStringAsContentTest() {
        TAG = "writeAndReadRecipeWithStringAsContentTest";
        String html = "<div dir=rtl lang=he class=recipeStyle> \n" +
                "    <h2>מרכיבים:</h2>\n" +
                "    <ul>\n" +
                "        <li>טחינה גולמית</li>\n" +
                "        <li>חלב מרוכז</li>\n" +
                "        <li>2 שמנת מתוקה</li>\n" +
                "    </ul> \n" +
                "    <hr>\n" +
                "\n" +
                "    <h2>אופן הכנה:</h2>\n" +
                "\n" +
                "    <ol>\n" +
                "        <li>לשפוך הכל לקערה</li>\n" +
                "        <li>לערבב טוב טוב</li>\n" +
                "        <li>להכניס למקפיא ל4 שעות</li>\n" +
                "    </ol>\n" +
                "    <h5>שיהיה בתיאבון!</h5>\n" +
                "</div>";

        PendingRecipeEntity entity = new PendingRecipeEntity();
        entity.setName("testName");
        entity.setDescription("testDescription");
        entity.setContent(html);
        PendingRecipeDao dao = db.pendingRecipeDao();
        dao.insertAll(entity);

        List<PendingRecipeEntity> entities = dao.getAll();
        assertNotNull(entities);
        assertThat(entities, hasSize(1));
        //assertThat(entity, isIn(entities));
    }
}
