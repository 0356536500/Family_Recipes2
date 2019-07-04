package com.myapps.ron.family_recipes.viewmodels;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.background.services.GetUserDetailsService;
import com.myapps.ron.family_recipes.logic.repository.AppRepository;
import com.myapps.ron.family_recipes.logic.repository.CategoryRepository;
import com.myapps.ron.family_recipes.logic.repository.RecipeRepository;
import com.myapps.ron.family_recipes.logic.repository.RepoSearchResults;
import com.myapps.ron.family_recipes.model.AccessEntity;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.model.QueryModel;
import com.myapps.ron.family_recipes.model.RecipeEntity;
import com.myapps.ron.family_recipes.model.RecipeMinimal;
import com.myapps.ron.family_recipes.layout.Constants;
import com.myapps.ron.family_recipes.utils.logic.CrashLogger;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private MutableLiveData<String> infoForUser = new MutableLiveData<>(); // info about new or modified pagedRecipes from last fetch from api
    private CompositeDisposable compositeDisposable;

    //private Observer<List<CategoryEntity>> categoryObserver = categoryList::setValue;

    public DataViewModel(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;

        this.compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(this.recipeRepository.dispatchInfo
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setInfo));
        compositeDisposable.add(this.categoryRepository.dispatchInfo
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setInfo));
        //compositeDisposable.add(this.categoryRepository.dispatchInfo.subscribe(infoForUser::postValue));

        categoryList = categoryRepository.getAllCategoriesLiveData();//.observeForever(categoryObserver);

        // Check if new firebase token is available and needs to be registered
        registerNewFirebaseToken(MyApplication.getContext(),
                SharedPreferencesHandler.getString(MyApplication.getContext(), com.myapps.ron.family_recipes.utils.Constants.NEW_FIREBASE_TOKEN));
    }

    private void registerNewFirebaseToken(Context context, @Nullable String token) {
        if (token != null) {
            compositeDisposable.add(AppRepository.getInstance().registerNewFirebaseToken(context, token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(success -> {
                        if (success)
                            SharedPreferencesHandler.removeString(context, com.myapps.ron.family_recipes.utils.Constants.NEW_FIREBASE_TOKEN);
                    }, throwable -> setInfo(throwable.getMessage())));
        }
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
        compositeDisposable.add(recipeRepository.getSingleRecipe(id)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(recipeEntity -> updateLike(context, recipeEntity, onErrorUpdateUI), throwable -> {
                    CrashLogger.logException(throwable);
                    infoForUser.postValue(context.getString(R.string.load_error_message));
                    onErrorUpdateUI.run();
                })
        );
    }

    /**
     * the actual update
     * Send request to server and then,
     */
    private void updateLike(final Context context, @NonNull RecipeEntity recipe, Runnable onErrorUpdateUI) {
        Log.e(getClass().getSimpleName(), "updateLike");
        Map<String, Object> attrs = new HashMap<>();
        String likeStr = recipe.isUserLiked() ? "unlike" : "like";
        attrs.put(Constants.LIKES, likeStr);
        compositeDisposable.add(recipeRepository.changeLike(context, recipe, attrs)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {}, throwable -> {
                    infoForUser.setValue(throwable.getMessage());
                    onErrorUpdateUI.run();
                })
        );
    }


    public void fetchFromServerJustLoggedIn(Context context) {
        GetUserDetailsService.startActionFetchUserDetails(context);
        //GetUserDetailsService.startActionGetAllRecipes(context);
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

    public LiveData<String> getInfo() {
        return infoForUser;
    }

    private void setInfo(String message) {
        infoForUser.setValue(message);
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
