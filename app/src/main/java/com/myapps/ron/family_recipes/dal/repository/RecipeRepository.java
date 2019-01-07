package com.myapps.ron.family_recipes.dal.repository;

import android.util.Log;

import com.myapps.ron.family_recipes.dal.persistence.AppDatabases;
import com.myapps.ron.family_recipes.dal.persistence.RecipeDao;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;

import java.util.List;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import io.reactivex.Single;
import io.reactivex.observers.DisposableMaybeObserver;

/**
 * Created by ronginat on 02/01/2019.
 */
public class RecipeRepository {

    private final RecipeDao recipeDao;
    private final Executor executor;

    private PagedList.Config pagedConfig;

    public RecipeRepository(RecipeDao recipeDao, Executor executor) {
        this.recipeDao = recipeDao;
        this.executor = executor;
        this.pagedConfig = new PagedList.Config.Builder()
                .setPageSize(15)
                .setPrefetchDistance(50)
                .setEnablePlaceholders(true)
                .build();
    }

    public Single<RecipeEntity> getRecipe(String id) {
        return recipeDao.getRecipe(id);
    }


    /*public DataSource.Factory<Integer, RecipeMinimal> getRecipesDataMinimalOrdered(String order) {
        switch (order) {
            case RecipeEntity.KEY_CREATED:
                return recipeDao.getRecipesDataMinimalOrderByCreation();
            case  RecipeEntity.KEY_MODIFIED:
                return recipeDao.getRecipesDataMinimalOrderByModified();
            case RecipeEntity.KEY_LIKES:
                return recipeDao.getRecipesDataMinimalOrderByLikes();
            default:
                return recipeDao.getRecipesDataMinimalOrderByCreation();
        }
    }*/

    /**
     * Query the repository
     *
     * @param query - order of the items to be fetched,
     * search a base LIKE search with string,
     * filters a complex LIKE search that searching for array containing this array
     */
    public RepoSearchResults query(@NonNull QueryModel query) {
        DataSource.Factory<Integer, RecipeMinimal> dataSource;

        String search = query.getSQLSearch();
        String order = query.getOrderBy();
        String filters = query.getSQLFilters();

        Log.e(getClass().getSimpleName(), order);
        Log.e(getClass().getSimpleName(), search);
        Log.e(getClass().getSimpleName(), filters);

        if (query.isFavorites()) {
            dataSource = switchCaseOrderFavorites(search, filters, order);
        } else
            dataSource = switchCaseOrder(search, filters, order);

         LiveData<PagedList<RecipeMinimal>> data = new LivePagedListBuilder<>(dataSource, pagedConfig)
                 .build();

         return new RepoSearchResults(data);
    }

    private DataSource.Factory<Integer, RecipeMinimal> switchCaseOrder(String search, String filters, String order) {
        switch (order) {
            case RecipeEntity.KEY_MODIFIED:
                return recipeDao.findAllByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByLastModified(search, filters);
            case RecipeEntity.KEY_LIKES:
                return recipeDao.findAllByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByPopularity(search, filters);
            default:
                return recipeDao.findAllByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByCreationDate(search, filters);
        }
    }

    private DataSource.Factory<Integer, RecipeMinimal> switchCaseOrderFavorites(String search, String filters, String order) {
        switch (order) {
            case RecipeEntity.KEY_MODIFIED:
                return recipeDao.findAllFavoritesByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByLastModified(search, filters);
            case RecipeEntity.KEY_LIKES:
                return recipeDao.findAllFavoritesByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByPopularity(search, filters);
            default:
                return recipeDao.findAllFavoritesByNameLikeOrDescriptionLikeAndCategoriesLikeOrderByCreationDate(search, filters);
        }
    }


    /**
     * update from local user
     * @param recipeEntity to insert
     */
    public void insertOrUpdateRecipe(RecipeEntity recipeEntity) {
        executor.execute(() ->
                recipeDao.insertRecipe(recipeEntity));
    }

    public void insertQuery(String name, int size) {
        executor.execute(() -> recipeDao.insertAll(AppDatabases.generateData(name, size)));
    }

    public void updateFromServer(List<RecipeTO> list) {
        executor.execute(() -> {
            for (RecipeTO fromServer: list) {
                recipeDao.isRecipeExists(fromServer.getId()).subscribe(new DisposableMaybeObserver<RecipeEntity>() {
                    @Override
                    public void onSuccess(RecipeEntity recipeEntity) {
                        // found a recipe
                        // update it and save the current 'like' of the user
                        RecipeEntity update = fromServer.toEntity();
                        update.setMeLike(recipeEntity.getMeLike());
                        recipeDao.updateRecipe(update);
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("updateFromServer", e.getMessage(), e);
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        // no recipe found
                        // insert a new recipe
                        recipeDao.insertRecipe(fromServer.toEntity());
                        dispose();
                    }
                });
            }
        });
    }

    public void deleteAllRecipes() {
        executor.execute(recipeDao::deleteAllRecipes);
    }
}
