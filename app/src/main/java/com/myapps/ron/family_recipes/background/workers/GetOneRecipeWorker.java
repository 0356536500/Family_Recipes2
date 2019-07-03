package com.myapps.ron.family_recipes.background.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.logic.repository.RecipeRepository;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.CrashLogger;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ronginat on 08/04/2019.
 */
public class GetOneRecipeWorker extends RxWorker {

    private final String recipeId;
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
        repository = Injection.provideRecipeRepository(getApplicationContext());
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        compositeDisposable.clear();
    }

    @Override
    public Single<Result> createWork() {
        return Single.create(emitter ->
                compositeDisposable.add(APICallsHandler.getRecipeObservable(recipeId, AppHelper.getAccessToken())
                        .subscribe(response -> {
                            if (response.isSuccessful() && response.body() != null) {
                                repository.insertRecipe(response.body().toEntity());
                                emitter.onSuccess(Result.success());
                            } else
                                repository.dispatchInfoForRecipe.onNext(response.message());
                        }, throwable -> {
                            CrashLogger.logException(throwable);
                            repository.dispatchInfoForRecipe.onNext(getApplicationContext().getString(R.string.load_error_message));
                            throwable.printStackTrace();
                            emitter.onSuccess(Result.failure());
                        })
                )
        );
    }

    public static OneTimeWorkRequest getOneRecipeWorker(@NonNull String recipeId) {
        // Create a Constraints object that defines when the task should run
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // then create a OneTimeWorkRequest that uses those constraints
        return new OneTimeWorkRequest.Builder(GetOneRecipeWorker.class)
                .setConstraints(myConstraints)
                .setInputData(new Data.Builder()
                        .putString(Constants.RECIPE_ID, recipeId)
                        .build())
                .build();
    }
}
