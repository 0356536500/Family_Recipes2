package com.myapps.ron.family_recipes.dal.repository;

import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.persistence.CategoryDao;
import com.myapps.ron.family_recipes.dal.persistence.Converters;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.network.modelTO.CategoryTO;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.List;
import java.util.concurrent.Executor;

import androidx.lifecycle.LiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Created by ronginat on 14/01/2019.
 */
public class CategoryRepository {
    private final String TAG = getClass().getSimpleName();
    private final CategoryDao categoryDao;
    private final Executor executor;

    public PublishSubject<String> dispatchInfo;

    private static CategoryRepository INSTANCE;

    public static CategoryRepository getInstance(CategoryDao categoryDao, Executor executor) {
        if (INSTANCE == null) {
            INSTANCE = new CategoryRepository(categoryDao, executor);
        }
        return INSTANCE;
    }

    private CategoryRepository(CategoryDao categoryDao, Executor executor) {
        this.categoryDao = categoryDao;
        this.executor = executor;

        this.dispatchInfo = PublishSubject.create();
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    /**
     * update from local user
     * @param categoryEntity to insert
     */
    public void insertOrUpdateCategory(CategoryEntity categoryEntity) {
        executor.execute(() ->
                categoryDao.insertCategory(categoryEntity));
    }

    /*public void insertQuery(String name, int size) {
        executor.execute(() -> categoryDao.insertAll(AppDatabases.generateData(name, size)));
    }*/

    public void deleteAllRecipes() {
        executor.execute(categoryDao::deleteAllCategories);
    }

    // region Remote Server

    public void fetchCategories(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)
                && DateUtil.shouldUpdateCategories(context)) {
            final String time = DateUtil.getUTCTime();
            APICallsHandler.getAllCategories(DateUtil.getLastUpdateTime(context), AppHelper.getAccessToken(), categories -> {
                if(categories != null) {
                    DateUtil.updateServerTime(context, time);
                    updateFromServer(categories);
                } else {
                    dispatchInfo.onNext(context.getString(R.string.error_message_from_fetch_categories));
                }
            });
        }
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void fetchCategoriesReactive(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)
                && DateUtil.shouldUpdateCategories(context)) {
            final String time = DateUtil.getUTCTime();

            Observable<Response<List<CategoryTO>>> categoryObservable = APICallsHandler
                    .getAllCategoriesObservable(DateUtil.getLastCategoriesUpdateTime(context), AppHelper.getAccessToken());
            Disposable disposable = categoryObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(next -> {
                        if (next.code() == 200)
                            updateFromServer(next.body());
                        DateUtil.updateCategoriesServerTime(context, time);
                    }, error -> dispatchInfo.onNext(context.getString(R.string.error_message_from_fetch_categories)));

            compositeDisposable.add(disposable);
        }
    }

    private void updateFromServer(List<CategoryTO> list) {
        executor.execute(() -> categoryDao.insertAll(Converters.fromCategoryTOList(list))
                .subscribe(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                Log.e(TAG, "all categories inserted");
                dispose();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "error when inserting all categories ", e);
                dispose();
            }
        }));
    }

    // endregion
}
