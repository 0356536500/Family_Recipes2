package com.myapps.ron.family_recipes.logic.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.logic.persistence.CategoryDao;
import com.myapps.ron.family_recipes.logic.persistence.Converters;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.layout.modelTO.CategoryTO;
import com.myapps.ron.family_recipes.utils.logic.DateUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
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

    public LiveData<List<CategoryEntity>> getAllCategoriesLiveData() {
        return categoryDao.getAllCategoriesLiveData();
    }

    public List<CategoryEntity> getAllCategories() {
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

    public void deleteAllCategories() {
        executor.execute(categoryDao::deleteAllCategories);
    }

    // region Remote Server

    public void fetchCategoriesReactive(final Context context) {
        if(MiddleWareForNetwork.checkInternetConnection(context)
                && AppHelper.getAccessToken() != null
                && DateUtil.shouldUpdateCategories(context)) {
            final String time = DateUtil.getUTCTime();

            APICallsHandler.getAllCategoriesObservable(DateUtil.getLastCategoriesUpdateTime(context), AppHelper.getAccessToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.from(executor))
                    .subscribe(new DisposableObserver<Response<List<CategoryTO>>>() {
                        @Override
                        public void onNext(Response<List<CategoryTO>> response) {
                            if (response.code() == APICallsHandler.STATUS_OK && response.body() != null) {
                                updateFromServer(response.body());
                                DateUtil.updateCategoriesServerTime(context, time);
                            } else if (response.code() != APICallsHandler.STATUS_NOT_MODIFIED) {
                                int code = response.code();
                                try {
                                    if (response.errorBody() != null)
                                        dispatchInfo.onNext(String.format("status %d" + response.errorBody().string(), code));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    dispatchInfo.onNext(e.getMessage());
                                }
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            dispatchInfo.onNext(context.getString(R.string.error_message_from_fetch_categories));
                            dispatchInfo.onError(t);
                            dispose();
                        }

                        @Override
                        public void onComplete() {
                            if (!isDisposed())
                                dispose();
                        }
                    });
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
