package com.myapps.ron.family_recipes.logic.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.layout.modelTO.CategoryTO;
import com.myapps.ron.family_recipes.logic.persistence.CategoryDao;
import com.myapps.ron.family_recipes.logic.persistence.Converters;
import com.myapps.ron.family_recipes.model.CategoryEntity;
import com.myapps.ron.family_recipes.utils.logic.CrashLogger;
import com.myapps.ron.family_recipes.utils.logic.DateUtil;

import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Response;

/**
 * Created by ronginat on 14/01/2019.
 */
public class CategoryRepository {
    //private final String TAG = getClass().getSimpleName();
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

    /*public void insertQuery(String name, int size) {
        executor.execute(() -> categoryDao.insertAll(AppDatabases.generateData(name, size)));
    }*/

    /*public void deleteAllCategories() {
        executor.execute(categoryDao::deleteAllCategories);
    }*/

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
                            if (response.isSuccessful() && response.body() != null) {
                                updateFromServer(response.body());
                                DateUtil.updateCategoriesServerTime(context, time);
                            } else if (response.code() != APICallsHandler.STATUS_NOT_MODIFIED) {
                                dispatchInfo.onNext(context.getString(R.string.error_message_from_fetch_categories));
                            }
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            dispatchInfo.onNext(context.getString(R.string.error_message_from_fetch_categories));
                            CrashLogger.logException(t);
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

    /**
     * update from online server.
     * Override existing categories if needed
     * @param list to insert
     */
    private void updateFromServer(List<CategoryTO> list) {
        executor.execute(() -> {
            CompositeDisposable compositeDisposable = new CompositeDisposable();
            compositeDisposable.add(categoryDao.insertAll(Converters.fromCategoryTOList(list))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.from(executor))
                    .subscribe(compositeDisposable::clear, CrashLogger::logException));
        });
    }

    // endregion
}
