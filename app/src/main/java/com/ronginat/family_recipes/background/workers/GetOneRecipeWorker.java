package com.ronginat.family_recipes.background.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.APICallsHandler;
import com.ronginat.family_recipes.layout.cognito.AppHelper;
import com.ronginat.family_recipes.logic.Injection;
import com.ronginat.family_recipes.logic.repository.RecipeRepository;
import com.ronginat.family_recipes.utils.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ronginat on 08/04/2019.
 */
public class GetOneRecipeWorker extends RxWorker {

    private final String recipeId, lastModified;
    private final RecipeRepository repository;
    private final CompositeDisposable compositeDisposable;
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker,
     *                     which recipe to download
     */
    public GetOneRecipeWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        recipeId = workerParams.getInputData().getString(Constants.RECIPE_ID);
        lastModified = workerParams.getInputData().getString(Constants.LAST_MODIFIED);
        repository = Injection.provideRecipeRepository(getApplicationContext());
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        compositeDisposable.clear();
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        return Single.create(emitter ->
                compositeDisposable.add(APICallsHandler.getRecipeObservable(this.recipeId, this.lastModified, AppHelper.getAccessToken())
                        .subscribe(response -> {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.e(getClass().getSimpleName(), response.body().toString());
                                repository.insertRecipe(response.body().toEntity());
                                emitter.onSuccess(Result.success());
                            } else {
                                if (response.errorBody() != null) {
                                    String message = response.errorBody().string();
                                    repository.dispatchInfoForRecipe.onNext(message);
                                } else
                                    repository.dispatchInfoForRecipe.onNext(getApplicationContext().getString(R.string.load_error_message));
                                emitter.onSuccess(Result.failure());
                            }
                        }, throwable -> {
                            CrashLogger.logException(throwable);
                            repository.dispatchInfoForRecipe.onNext(getApplicationContext().getString(R.string.load_error_message));
                            throwable.printStackTrace();
                            emitter.onSuccess(Result.failure());
                        })
                )
        );
    }

    public static OneTimeWorkRequest getOneRecipeWorker(@NonNull String recipeId, String lastModified) {
        // Create a Constraints object that defines when the task should run
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // then create a OneTimeWorkRequest that uses those constraints
        return new OneTimeWorkRequest.Builder(GetOneRecipeWorker.class)
                .setConstraints(myConstraints)
                .setInputData(new Data.Builder()
                        .putString(Constants.RECIPE_ID, recipeId)
                        .putString(Constants.LAST_MODIFIED, lastModified)
                        .build())
                .build();
    }
}
