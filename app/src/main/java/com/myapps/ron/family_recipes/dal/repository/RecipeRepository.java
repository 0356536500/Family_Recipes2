package com.myapps.ron.family_recipes.dal.repository;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.background.workers.GetOneRecipeWorker;
import com.myapps.ron.family_recipes.dal.persistence.AppDatabases;
import com.myapps.ron.family_recipes.dal.persistence.Converters;
import com.myapps.ron.family_recipes.dal.persistence.RecipeDao;
import com.myapps.ron.family_recipes.dal.storage.ExternalStorageHelper;
import com.myapps.ron.family_recipes.model.AccessEntity;
import com.myapps.ron.family_recipes.model.CommentEntity;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.network.modelTO.CommentTO;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.utils.logic.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Response;

import static com.myapps.ron.family_recipes.network.APICallsHandler.STATUS_OK;
import static com.myapps.ron.family_recipes.utils.Constants.FALSE;
import static com.myapps.ron.family_recipes.utils.Constants.TRUE;

/**
 * Created by ronginat on 02/01/2019.
 */
public class RecipeRepository {
    private final String TAG = getClass().getSimpleName();
    @SuppressWarnings("FieldCanBeLocal")
    private final long DELAYED_DISPATCH = 2000;
    private final int LIMIT = 100;
    private AtomicBoolean mayRefresh;

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
        this.mayRefresh = new AtomicBoolean(true);
    }

    /*public Single<RecipeEntity> getSingleRecipe(String id) {
        return recipeDao.getSingleRecipe(id);
    }*/

    // region Local

    /**
     * @param id recipe id
     * @return {@link Single} list of images file names to
     * {@link com.myapps.ron.family_recipes.viewmodels.DataViewModel} used by {@link com.myapps.ron.family_recipes.ui.fragments.RecyclerWithFiltersAbstractFragment}
     */
    public Maybe<List<String>> getRecipeImages(String id) {
        return Maybe.create(emitter ->
                recipeDao.getMaybeRecipeImages(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.from(executor))
                        .subscribe(new DisposableMaybeObserver<String>() {
                                       @Override
                                       public void onSuccess(String s) {
                                           Log.e(TAG, "onSuccess, " + s);
                                           if (s != null && s.length() > 0 && !s.equals("null")) // !s.startsWith("[") && s.endsWith("]"))
                                               emitter.onSuccess(Converters.fromString(s));
                                           else
                                               emitter.onComplete();
                                           dispose();
                                       }

                                       @Override
                                       public void onError(Throwable t) {
                                           Log.e(TAG, "onError, " + t.getMessage());
                                           emitter.onComplete();
                                           dispose();
                                       }

                                       @Override
                                       public void onComplete() {
                                           Log.e(TAG, "onComplete");
                                           emitter.onComplete();
                                           dispose();
                                       }
                                   })
                );
    }

    public Single<RecipeEntity> getSingleRecipe(@NonNull String id) {
        return recipeDao.getSingleRecipe(id);
    }

    /**
     * @param id recipe id
     * @return {@link Flowable} {@link RecipeEntity} to
     * {@link com.myapps.ron.family_recipes.viewmodels.RecipeViewModel} used by {@link com.myapps.ron.family_recipes.ui.activities.RecipeActivity}
     */
    public Flowable<RecipeEntity> getObservableRecipe(String id) {
        // check whether the recipe is available locally with getMaybeRecipe
        // return the Flowable anyways, it will do onNext when the recipe will be available
        recipeDao.getMaybeRecipe(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(executor))
                .subscribe(new DisposableMaybeObserver<RecipeEntity>() {
                    @Override
                    public void onSuccess(RecipeEntity entity) {
                        // will be available in Flowable#onNext
                        Log.e(TAG, "getMaybeRecipe, onSuccess");
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "getMaybeRecipe, onComplete, enqueue worker");
                        // no local recipe, fetch from server
                        WorkManager.getInstance().enqueueUniqueWork(GetOneRecipeWorker.class.getSimpleName(), ExistingWorkPolicy.REPLACE, GetOneRecipeWorker.getOneRecipeWorker(id));
                    }
                });
        return recipeDao.getObservableRecipe(id);
    }

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

    /**
     * Called from {@link GetOneRecipeWorker} background worker
     * @param recipeEntity to insert
     * @return {@link Completable} - deferred computation without any value but
     * only indication for completion or exception.
     */
    public Completable insertRecipeCompletable(RecipeEntity recipeEntity) {
        return recipeDao.insertRecipeCompletable(recipeEntity);
        /*return Completable.create(emitter ->
                executor.execute(() ->
                        recipeDao.insertRecipeCompletable(recipeEntity)
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        emitter.onComplete();
                                        dispose();
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        emitter.onError(t);
                                        dispose();
                                    }
                                })
                        )
        );*/
    }

    private void updateRecipe(RecipeEntity recipeEntity) {
        executor.execute(() ->
                recipeDao.updateRecipe(recipeEntity));
    }

    public void insertQuery(String name, int size) {
        executor.execute(() -> recipeDao.insertAll(AppDatabases.generateData(name, size)));
    }

    // endregion

    // region Helpers

    //@SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteOldRecipeContent(Context context, String path) {
        Uri uri = ExternalStorageHelper.getFileAbsolutePath(context,
                Constants.RECIPES_DIR, path);
        if (uri != null) {
            // local file exists
            Log.e(TAG, "deleting " + path + ", " + new File(uri.getPath()).delete());
        }
    }

    private void delayedDispatch(final Context context, final AddedModifiedSize addedModifiedSize) {
        executor.execute(() -> {
            try {
                Thread.sleep(DELAYED_DISPATCH);
                dispatchInfo.onNext(
                        context.getString(
                                R.string.message_from_fetch_recipes,
                                addedModifiedSize.added,
                                addedModifiedSize.modified));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        /*new Handler().postDelayed(() ->
                        dispatchInfo.onNext(
                                context.getString(
                                        R.string.message_from_fetch_recipes,
                                        addedModifiedSize.added,
                                        addedModifiedSize.modified)),
                DELAYED_DISPATCH);*/
    }

    // endregion

    // region Server

    public void updateFromServer(Context context, List<RecipeTO> list, AddedModifiedSize addedModifiedSize) {
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
                //Log.e(TAG, "updateFromServer, id = " + fromServer.getId());
                recipeDao.getMaybeRecipe(fromServer.getId()).subscribe(new DisposableMaybeObserver<RecipeEntity>() {
                    RecipeEntity update = fromServer.toEntity();
                    @Override
                    public void onSuccess(RecipeEntity recipeEntity) {
                        // found a recipe
                        // update it and save the current 'like' of the user
                        Log.e(TAG, "updateFromServer, found, id " + fromServer.getId());
                        if (!update.identical(recipeEntity)) {
                            update.setMeLike(recipeEntity.getMeLike());
                            // compare existing and new recipe html files
                            if (recipeEntity.getRecipeFile() != null &&
                                    !recipeEntity.getRecipeFile().equals(update.getRecipeFile())) {
                                // delete old recipe html file
                                Log.e(TAG, "different html");
                                deleteOldRecipeContent(context, recipeEntity.getRecipeFile());

                            }
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
        if (!MiddleWareForNetwork.checkInternetConnection(context)) {
            dispatchInfo.onNext(context.getString(R.string.no_internet_message));
            return;
        }
        if (AppHelper.getAccessToken() == null) {
            dispatchInfo.onNext(context.getString(R.string.invalid_access_token));
            return;
        }
        if (!mayRefresh.get()){
            dispatchInfo.onNext(context.getString(R.string.refresh_error_message));
            return;
        }
        mayRefresh.getAndSet(false);
        new Handler().postDelayed(() -> mayRefresh.getAndSet(true), com.myapps.ron.family_recipes.utils.Constants.REFRESH_DELAY);
        //if(MiddleWareForNetwork.checkInternetConnection(context)) {
        final String time = DateUtil.getUTCTime();

        String lastUpdate = DateUtil.getLastUpdateTime(context);

        Observable<Response<List<RecipeTO>>> recipeObservable = APICallsHandler
                .getAllRecipesObservable(lastUpdate, LIMIT, null, AppHelper.getAccessToken());
        Disposable disposable = recipeObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(executor))
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    if (next.code() == 200) {
                        Log.e(TAG, "fetch recipes, " + next.body());
                        final AddedModifiedSize addedModifiedSize = new AddedModifiedSize();
                        updateFromServer(context, next.body(), addedModifiedSize);
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
        //}
    }

    // make more api calls with pagination if necessary, using lastEvaluatedKey header from api response
    private void fetchMoreRecipesReactive(final Context context, @NonNull String lastKey, String lastUpdate, final AddedModifiedSize addedModifiedSize, final String currentTimeStamp) {
        if(MiddleWareForNetwork.checkInternetConnection(context)) {
            Observable<Response<List<RecipeTO>>> recipeObservable = APICallsHandler
                    .getAllRecipesObservable(lastUpdate, LIMIT, lastKey, AppHelper.getAccessToken());
            Disposable disposable = recipeObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.from(executor))
                    //.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(next -> {
                        if (next.code() == 200) {
                            Log.e(TAG, "more recipes, status 200");
                            updateFromServer(context, next.body(), addedModifiedSize);
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

    //for case of server not sending the updated recipe in the response,
    //need to fetch it to update lastModifiedDate attribute
    public void changeLike(String id, boolean like) {
        executor.execute(() -> {
            recipeDao.updateLikeRecipe(id, like ? TRUE : FALSE);
            compositeDisposable.add(APICallsHandler.getRecipeObservable(id, AppHelper.getAccessToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.from(executor))
                    .subscribe(response -> {
                        Log.e(TAG, "get one recipe code = " + response.code());
                        if (response.code() == STATUS_OK)
                            updateFromServerAfterLike(response.body());
                    }, throwable -> Log.e(TAG, "error when getting updated recipe after like\n" + throwable.getMessage())));
        });
    }

    private void updateFromServerAfterLike(RecipeTO updatedFromServer) {
        if (updatedFromServer == null) {
            Log.e(TAG, "recipe from server, null");
            return;
        }
        executor.execute(() ->
                recipeDao.getSingleRecipe(updatedFromServer.getId())
                        .subscribe(new DisposableSingleObserver<RecipeEntity>() {
                            RecipeEntity update = updatedFromServer.toEntity();
                            @Override
                            public void onSuccess(RecipeEntity recipeEntity) {
                                // found a recipe
                                // update it and save the current 'like' of the user
                                //Log.e(TAG, "updateFromServer, found, id " + updatedFromServer.getId());
                                if (!update.identical(recipeEntity)) {
                                    update.setMeLike(recipeEntity.getMeLike());
                                    recipeDao.updateRecipe(update);
                                }
                                dispose();
                            }

                            @Override
                            public void onError(Throwable e) {
                                dispatchInfo.onNext(e.getMessage());
                                Log.e("updateFromServer", e.getMessage(), e);
                                dispose();
                            }
                        })
        );
    }

    public Single<List<CommentEntity>> fetchRecipeComments(Context context, String recipeId) {
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.error(new Throwable(context.getString(R.string.no_internet_message)));
        if (AppHelper.getAccessToken() == null)
            return Single.error(new Throwable(context.getString(R.string.invalid_access_token)));
        return Single.create(emitter -> APICallsHandler.getRecipeCommentsObervable(recipeId, AppHelper.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(executor))
                .subscribe(new DisposableObserver<Response<List<CommentTO>>>() {
                    @Override
                    public void onNext(Response<List<CommentTO>> response) {
                        if (response.code() == APICallsHandler.STATUS_OK) {
                            // status 200 OK
                         if (response.body() != null) {
                             // there are comments
                             List<CommentEntity> rv = new ArrayList<>();
                             for (CommentTO to : response.body()) {
                                 rv.add(to.toEntity());
                             }
                             emitter.onSuccess(rv);
                         } else {
                             // body is null somehow
                             emitter.onError(new Throwable(context.getString(R.string.load_error_message)));
                         }
                        } else {
                            String message = response.message();
                            try {
                                if (response.errorBody() != null)
                                    message = response.errorBody().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                                emitter.onError(e);
                            }
                            emitter.onError(new Throwable(String.format("status %d, " + message, response.code())));
                        }
                        dispose();
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.onError(t);
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (!isDisposed())
                            dispose();
                    }
                }));
    }

    // endregion

    public void updateFavoritesFromUserRecord(List<String> favorites) {
        if (favorites != null) {
            executor.execute(() -> {
                for (String id : favorites)
                    recipeDao.updateLikeRecipe(id, TRUE);
                //recipeDao.updateLikesFromUserRecord(favorites, TRUE);
            });
        }
    }

    public Single<Boolean> changeLike(final Context context, @NonNull RecipeEntity recipe, Map<String, Object> attrs) {
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.error(new Throwable(context.getString(R.string.no_internet_message)));
        if (AppHelper.getAccessToken() == null)
            return Single.error(new Throwable(context.getString(R.string.invalid_access_token)));
        return Single.create(emitter ->
                APICallsHandler.patchRecipe(attrs, recipe.getId(), recipe.getLastModifiedDate(), AppHelper.getAccessToken(), result -> {
                    if (result != null && recipe.getId().equals(result.getId())) { // status 200
                        RecipeEntity update = result.toEntity();
                        update.setMeLike(!recipe.isUserLiked() ? TRUE : FALSE);
                        updateRecipe(update);
                        emitter.onSuccess(true);
                    } else // status <> 200
                        emitter.onError(new Throwable(context.getString(R.string.load_error_message)));
                })
        );
    }

    public Single<Boolean> postComment(final Context context, Map<String, Object> patchAttrs, String recipeId, String lastModifiedDate) {
        if (!MiddleWareForNetwork.checkInternetConnection(context))
            return Single.error(new Throwable(context.getString(R.string.no_internet_message)));
        if (AppHelper.getAccessToken() == null)
            return Single.error(new Throwable(context.getString(R.string.invalid_access_token)));
        return Single.create(emitter -> {
            APICallsHandler.postCommentObservable(patchAttrs, recipeId, lastModifiedDate, AppHelper.getAccessToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.from(executor))
                    .subscribe(new DisposableObserver<Response<Void>>() {
                        @Override
                        public void onNext(Response<Void> response) {
                            if (response.code() == STATUS_OK) {
                                emitter.onSuccess(true);
                            } else {
                                String message = response.message();
                                try {
                                    if (response.errorBody() != null)
                                        message = response.errorBody().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    emitter.onError(e);
                                }
                                emitter.onError(new Throwable(String.format("status %d, " + message, response.code())));
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            emitter.onError(t);
                            dispose();
                        }

                        @Override
                        public void onComplete() {
                            if (!isDisposed())
                                dispose();
                        }
                    });
        });
    }


    public void deleteAllRecipes() {
        executor.execute(recipeDao::deleteAllRecipes);
    }

    // region Recipe Access

      // region Update/Insert Access

    /**
     *
     * @param id specified recipe
     * @param accessKey One String of {@link AccessEntity#KEY_ACCESSED_THUMBNAIL},
     * {@link AccessEntity#KEY_ACCESSED_RECIPE} or {@link AccessEntity#KEY_ACCESSED_IMAGES}
     * @param value new Date().getTime() or null
     */
    public void upsertRecipeAccess(@NonNull String id, @NonNull String accessKey, @Nullable Long value) {
        executor.execute(() -> recipeDao.getMaybeAccessById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(executor))
                .subscribe(new DisposableMaybeObserver<AccessEntity>() {
                    @Override
                    public void onSuccess(AccessEntity accessEntity) {
                        recipeDao.updateRecipeAccess(
                                updatePOJOAccessEntityByKey(accessEntity, accessKey, value));
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        dispatchInfo.onNext(e.toString());
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        AccessEntity accessEntity = new AccessEntity();
                        accessEntity.setRecipeId(id);
                        recipeDao.insertRecipeAccess(
                                updatePOJOAccessEntityByKey(accessEntity, accessKey, value));
                        dispose();
                    }
                }));
    }

    private AccessEntity updatePOJOAccessEntityByKey(@NonNull AccessEntity access, @NonNull String accessKey, @Nullable Long value) {
        switch (accessKey) {
            case AccessEntity.KEY_ACCESSED_THUMBNAIL:
                access.setLastAccessedThumbnail(value);
                break;
            case AccessEntity.KEY_ACCESSED_RECIPE:
                access.setLastAccessedRecipe(value);
                break;
            case AccessEntity.KEY_ACCESSED_IMAGES:
                access.setLastAccessedImages(value);
                break;
        }
        return access;
    }

      // endregion

      // region Fetch Access

    @Nullable
    public List<AccessEntity.RecipeAccess> getRecipesAccessesOrderBy(String accessKey) {
        switch (accessKey) {
            case AccessEntity.KEY_ACCESSED_THUMBNAIL:
                return recipeDao.getAccessTimeOrderByThumb();
            case AccessEntity.KEY_ACCESSED_RECIPE:
                return recipeDao.getAccessTimeOrderByRecipe();
            case AccessEntity.KEY_ACCESSED_IMAGES:
                return recipeDao.getAccessTimeOrderByImages();
            default:
                return null;
        }
    }

      // endregion

    // endregion

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
