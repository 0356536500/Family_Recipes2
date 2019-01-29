package com.myapps.ron.family_recipes.dal.repository;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.persistence.AppDatabases;
import com.myapps.ron.family_recipes.dal.persistence.RecipeDao;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.List;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Response;

import static com.myapps.ron.family_recipes.utils.Constants.FALSE;
import static com.myapps.ron.family_recipes.utils.Constants.TRUE;

/**
 * Created by ronginat on 02/01/2019.
 */
public class RecipeRepository {
    private final String TAG = getClass().getSimpleName();
    private final long DELAYED_DISPATCH = 2000;
    private final int LIMIT = 100;

    private final RecipeDao recipeDao;
    private final Executor executor;

    private PagedList.Config pagedConfig;

    public PublishSubject<String> dispatchInfo;

    private static RecipeRepository INSTANCE;

    public static RecipeRepository getInstance(RecipeDao recipeDao, Executor executor) {
        if (INSTANCE == null) {
            INSTANCE = new RecipeRepository(recipeDao, executor);
        }
        return INSTANCE;
    }

    private RecipeRepository(RecipeDao recipeDao, Executor executor) {
        this.recipeDao = recipeDao;
        this.executor = executor;
        this.pagedConfig = new PagedList.Config.Builder()
                .setPageSize(15)
                .setPrefetchDistance(50)
                .setEnablePlaceholders(true)
                .build();
        this.dispatchInfo = PublishSubject.create();
    }

    public Single<RecipeEntity> getRecipe(String id) {
        return recipeDao.getRecipe(id);
    }

    public Flowable<RecipeEntity> getObservableRecipe(String id) {
        return recipeDao.getObservableRecipe(id);
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

    public void updateFromServer(List<RecipeTO> list, AddedModifiedSize addedModifiedSize) {
        if (list != null) {
            if (list.isEmpty()) {
                return;
            }
            Log.e(TAG, "recipes from server, " + list.toString());
        } else {
            Log.e(TAG, "recipes from server, null");
            return;
        }
        executor.execute(() -> {
            // first cell is for added and second cell is for modified recipes
            for (RecipeTO fromServer: list) {
                Log.e(TAG, "updateFromServer, id = " + fromServer.getId());
                recipeDao.isRecipeExists(fromServer.getId()).subscribe(new DisposableMaybeObserver<RecipeEntity>() {
                    RecipeEntity update = fromServer.toEntity();
                    @Override
                    public void onSuccess(RecipeEntity recipeEntity) {
                        // found a recipe
                        // update it and save the current 'like' of the user
                        Log.e(TAG, "updateFromServer, found, id " + fromServer.getId());
                        if (!update.identical(recipeEntity)) {
                            update.setMeLike(recipeEntity.getMeLike());
                            recipeDao.updateRecipe(update);
                            addedModifiedSize.incrementModified();
                            dispose();
                        }

                        addedModifiedSize.incrementSize();

                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dispatchInfo.onNext(e.getMessage());
                        Log.e("updateFromServer", e.getMessage(), e);
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        // no recipe found
                        // insert a new recipe
                        Log.e(TAG, "updateFromServer, recipe not found, id " + fromServer.getId());
                        recipeDao.insertRecipe(fromServer.toEntity());
                        addedModifiedSize.incrementAdded();
                        addedModifiedSize.incrementSize();

                        dispose();
                    }
                });
            }
        });
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void fetchRecipesReactive(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            final String time = DateUtil.getUTCTime();

            String lastUpdate = DateUtil.getLastUpdateTime(context);

            Observable<Response<List<RecipeTO>>> recipeObservable = APICallsHandler
                    .getAllRecipesObservable(lastUpdate, LIMIT, null, AppHelper.getAccessToken());
            Disposable disposable = recipeObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(next -> {
                        if (next.code() == 200) {
                            Log.e(TAG, "fetch recipes, " + next.body());
                            final AddedModifiedSize addedModifiedSize = new AddedModifiedSize();
                            updateFromServer(next.body(), addedModifiedSize);
                            String lastKey = next.headers().get(Constants.HEADER_LAST_EVAL_KEY);
                            if (lastKey != null && !lastKey.isEmpty())
                                // there are more updated recipes
                                fetchMoreRecipesReactive(context, lastKey, lastUpdate, addedModifiedSize, time);
                            else {
                                //there are no more recipes
                                delayedDispatch(context, addedModifiedSize);
                                DateUtil.updateServerTime(context, time);
                                compositeDisposable.clear();
                            }
                        } else if (next.code() == 304) {
                            DateUtil.updateServerTime(context, time);
                            dispatchInfo.onNext(context.getString(R.string.message_from_fetch_recipes_not_modified));
                            compositeDisposable.clear();
                        } else {
                            dispatchInfo.onNext(context.getString(R.string.load_error_message) + next.message());
                            compositeDisposable.clear();
                        }
                        Log.e(TAG, "response code, " + next.code());

                        //DateUtil.updateServerTime(context, time);
                    }, error -> Toast.makeText(context, context.getString(R.string.load_error_message) + "\n" + error.getMessage(), Toast.LENGTH_SHORT).show());

            compositeDisposable.add(disposable);
        }
    }

    // make more api calls with pagination if necessary, using lastEvaluatedKey header from api response
    private void fetchMoreRecipesReactive(final Context context, @NonNull String lastKey, String lastUpdate, final AddedModifiedSize addedModifiedSize, final String currentTimeStamp) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            Observable<Response<List<RecipeTO>>> recipeObservable = APICallsHandler
                    .getAllRecipesObservable(lastUpdate, LIMIT, lastKey, AppHelper.getAccessToken());
            Disposable disposable = recipeObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(next -> {
                        if (next.code() == 200) {
                            Log.e(TAG, "more recipes, status 200");
                            updateFromServer(next.body(), addedModifiedSize);
                            String lastEvalKey = next.headers().get(Constants.HEADER_LAST_EVAL_KEY);
                            if (lastEvalKey != null && !lastEvalKey.isEmpty())
                                // there are more updated recipes
                                fetchMoreRecipesReactive(context, lastEvalKey, lastUpdate, addedModifiedSize, currentTimeStamp);
                            else {
                                //there are no more recipes
                                delayedDispatch(context, addedModifiedSize);
                                DateUtil.updateServerTime(context, currentTimeStamp);
                                compositeDisposable.clear();
                            }
                        }
                        Log.e(TAG, "response code, " + next.code());

                    }, error -> Toast.makeText(context, context.getString(R.string.load_error_message) + "\n" + error.getMessage(), Toast.LENGTH_SHORT).show());

            compositeDisposable.add(disposable);
        }
    }

    private void delayedDispatch(final Context context, final AddedModifiedSize addedModifiedSize) {
        new Handler().postDelayed(() ->
                        dispatchInfo.onNext(
                                context.getString(
                                        R.string.message_from_fetch_categories,
                                        addedModifiedSize.added,
                                        addedModifiedSize.modified)),
                DELAYED_DISPATCH);
    }

    /*public void fetchRecipesReactive1(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            final String time = DateUtil.getUTCTime();

            Observable<Response<List<RecipeTO>>> recipeObservable = APICallsHandler
                    .getAllRecipesObservable(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken());
            Disposable disposable = recipeObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(next -> {
                        if (next.code() == 200) {
                            //updateFromServer(context, next.body());
                        } else if (next.code() == 304) {
                            dispatchInfo.onNext(context.getString(R.string.message_from_fetch_recipes_not_modified));
                        }
                        Log.e(TAG, "response code, " + next.code());

                        DateUtil.updateServerTime(context, time);
                    }, error -> Toast.makeText(context, R.string.load_error_message, Toast.LENGTH_SHORT).show());

            compositeDisposable.add(disposable);
        }
    }*/

    public void changeLike(String id, boolean like) {
        executor.execute(() ->
                recipeDao.updateLikeRecipe(id, like ? TRUE : FALSE));
    }

    public void deleteAllRecipes() {
        executor.execute(recipeDao::deleteAllRecipes);
    }


    class AddedModifiedSize {
        private int added, modified, size;

        synchronized void incrementAdded() {
            added++;
        }

        synchronized void incrementModified() {
            modified++;
        }

        synchronized void incrementSize() {
            size++;
        }
    }
}
