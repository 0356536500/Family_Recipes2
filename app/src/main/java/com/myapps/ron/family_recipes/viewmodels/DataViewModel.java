package com.myapps.ron.family_recipes.viewmodels;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.background.services.GetUserDetailsService;
import com.myapps.ron.family_recipes.dal.repository.AppRepository;
import com.myapps.ron.family_recipes.dal.repository.CategoryRepository;
import com.myapps.ron.family_recipes.dal.repository.RecipeRepository;
import com.myapps.ron.family_recipes.dal.repository.RepoSearchResults;
import com.myapps.ron.family_recipes.model.AccessEntity;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.network.Constants;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * class for use by MainActivity.
 * loads all recipes, updates local db and server time.
 */
public class DataViewModel extends ViewModel {
    private RecipeRepository recipeRepository;
    private CategoryRepository categoryRepository;

    private MutableLiveData<QueryModel> queryLiveData = new MutableLiveData<>();
    //Applying transformation to get RepoSearchResults for the given Search Query
    private LiveData<RepoSearchResults> repoResults = Transformations.map(queryLiveData,
            input -> recipeRepository.query(input));

    //Applying transformation to get Live PagedList<Repo> from the RepoSearchResult
    private LiveData<PagedList<RecipeMinimal>> pagedRecipes = Transformations.switchMap(repoResults,
            RepoSearchResults::getData
    );

    private LiveData<List<CategoryEntity>> categoryList;// = new MutableLiveData<>(); // list of newCategories from api

    private MutableLiveData<String> infoFromLastFetch = new MutableLiveData<>(); // info about new or modified pagedRecipes from last fetch from api
    private CompositeDisposable compositeDisposable;

    //private Observer<List<CategoryEntity>> categoryObserver = categoryList::setValue;

    public DataViewModel(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;

        this.compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(this.recipeRepository.dispatchInfo.subscribe(infoFromLastFetch::postValue));
        compositeDisposable.add(this.categoryRepository.dispatchInfo.subscribe(infoFromLastFetch::postValue));

        categoryList = categoryRepository.getAllCategoriesLiveData();//.observeForever(categoryObserver);
    }


    public LiveData<PagedList<RecipeMinimal>> getPagedRecipes() {
        return pagedRecipes;
    }


    public void applyQuery(@NonNull QueryModel queryModel) {
        QueryModel newModel = new QueryModel.Builder().build(queryModel);
        if (!newModel.equals(queryLiveData.getValue())) {
            queryLiveData.setValue(newModel);
        }
    }

    /**
     * Retrieve the recipe before updating it
     * @param context context
     * @param id recipe identifier
     */
    public void changeLike(final Context context, @NonNull String id, Runnable onErrorUpdateUI) {
        recipeRepository.getSingleRecipe(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new DisposableSingleObserver<RecipeEntity>() {
                    @Override
                    public void onSuccess(RecipeEntity recipeEntity) {
                        updateLike(context, recipeEntity, onErrorUpdateUI);
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        infoFromLastFetch.postValue(e.getMessage());
                        onErrorUpdateUI.run();
                        dispose();
                    }
                });

    }

    /**
     * the actual update
     */
    private void updateLike(final Context context, @NonNull RecipeEntity recipe, Runnable onErrorUpdateUI) {
        Log.e(getClass().getSimpleName(), "updateLike");
        Map<String, Object> attrs = new HashMap<>();
        String likeStr = recipe.isUserLiked() ? "unlike" : "like";
        attrs.put(Constants.LIKES, likeStr);
        recipeRepository.changeLike(context, recipe, attrs)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer status) {
                        if (status != -1) {
                            infoFromLastFetch.setValue(context.getString(status));
                            onErrorUpdateUI.run();
                        }
                        dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        infoFromLastFetch.setValue(e.getMessage());
                        onErrorUpdateUI.run();
                        dispose();
                    }
                });
    }


    public void fetchFromServerJustLoggedIn(Context context) {
        GetUserDetailsService.startActionFetchUserDetails(context);
        //GetUserDetailsService.startActionGetAllRecipes(context);
        //categoryRepository.fetchCategoriesReactive(context);
    }

    public void fetchFromServer(Context context) {
        recipeRepository.fetchRecipesReactive(context);
        categoryRepository.fetchCategoriesReactive(context);
    }

    public Maybe<List<String>> getRecipeImages(String id) {
        return recipeRepository.getRecipeImages(id);
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categoryList;
    }

    public LiveData<String> getInfoFromLastFetch() {
        return infoFromLastFetch;
    }

    // region Recipe Access

    private void updateAccessToRecipe(String id, String accessKey) {
        recipeRepository.upsertRecipeAccess(id, accessKey, new Date().getTime());
    }

    public void updateAccessToRecipeThumbnail(String id) {
        updateAccessToRecipe(id, AccessEntity.KEY_ACCESSED_THUMBNAIL);
    }

    public void updateAccessToRecipeImages(String id) {
        updateAccessToRecipe(id, AccessEntity.KEY_ACCESSED_IMAGES);
    }

    // endregion

    // App update

    public Single<Map<String, String>> getDataToDownloadUpdate(ContextWrapper context) {
        return Single.create(emitter ->
                AppRepository.getInstance().getDataToDownloadUpdate(context)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableMaybeObserver<Map<String, String>>() {
                            @Override
                            public void onSuccess(Map<String, String> map) {
                                if (map != null) // always true
                                    emitter.onSuccess(map);
                                dispose();
                            }

                            @Override
                            public void onError(Throwable t) {
                                emitter.onError(t);
                                dispose();
                            }

                            @Override
                            public void onComplete() {
                                // notify up to-date
                                emitter.onError(new Throwable(context.getString(R.string.main_activity_app_up_to_date)));
                                dispose();
                            }
                        })
        );
    }

    public void downloadNewAppVersion(ContextWrapper context, BroadcastReceiver onComplete, Uri uri, File appUpdateFile) {
        context.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        ((DownloadManager)context.getSystemService(DOWNLOAD_SERVICE)).enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(appUpdateFile.getName())
                .setDescription("Downloading app update")
                .setDestinationUri(Uri.fromFile(appUpdateFile))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        );
    }

    public void installApp(Context context, File appUpdateFile) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addCategory("android.intent.category.DEFAULT");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName(), appUpdateFile), "application/vnd.android.package-archive");
        //installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(installIntent);
    }

    // endregion

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        //categoryRepository.getAllCategoriesLiveData().removeObserver(categoryObserver);
    }
}
