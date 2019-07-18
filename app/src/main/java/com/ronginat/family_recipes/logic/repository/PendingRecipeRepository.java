package com.ronginat.family_recipes.logic.repository;

import com.ronginat.family_recipes.logic.persistence.PendingRecipeDao;
import com.ronginat.family_recipes.model.PendingRecipeEntity;

import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.Completable;

/**
 * Created by ronginat on 26/02/2019.
 */
public class PendingRecipeRepository {
    //private final String TAG = getClass().getSimpleName();
    private final PendingRecipeDao recipeDao;
    private final Executor executor;


    private static PendingRecipeRepository INSTANCE;

    public static PendingRecipeRepository getInstance(PendingRecipeDao pendingRecipeDao, Executor executor) {
        if (INSTANCE == null) {
            INSTANCE = new PendingRecipeRepository(pendingRecipeDao, executor);
        }
        return INSTANCE;
    }

    private PendingRecipeRepository(PendingRecipeDao pendingRecipeDao, Executor executor) {
        this.recipeDao = pendingRecipeDao;
        this.executor = executor;
    }



    public List<PendingRecipeEntity> getAll() {
        return recipeDao.getAll();
    }

    public Completable insertOrUpdatePendingRecipe(PendingRecipeEntity recipeEntity) {
        return recipeDao.insert(recipeEntity);
        /*executor.execute(() ->
                recipeDao.insert(recipeEntity));*/
    }

    public void delete(PendingRecipeEntity recipe) {
        executor.execute(() ->
                recipeDao.delete(recipe));
    }

    /*public void deleteAll() {
        executor.execute(recipeDao::deleteAll);
    }*/
}
