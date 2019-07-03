package com.myapps.ron.family_recipes.background.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.IOException;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ronginat on 20/06/2019
 */
public class GetRecipeContentWorker extends RxWorker {

    private final String recipeId, lastModifiedContent;
    private final RecipeRepository repository;
    private final CompositeDisposable compositeDisposable;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker,
     *                     which recipe content to download
     */
    public GetRecipeContentWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        recipeId = workerParams.getInputData().getString(Constants.RECIPE_ID);
        lastModifiedContent = workerParams.getInputData().getString(Constants.CONTENT_MODIFIED);
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
                compositeDisposable.add(APICallsHandler.getRecipeContentObservable(this.recipeId, lastModifiedContent, AppHelper.getAccessToken())
                    .subscribe(response -> {
                        if (response.isSuccessful() && response.body() != null) {
                            repository.insertContentRecipe(response.body().toEntity());
                        }
                        else if (!response.isSuccessful() && response.code() != 304) {
                            try {
                                String message = response.errorBody() != null ? response.errorBody().string() : "";
                                repository.dispatchInfoForRecipe.onNext(
                                        getApplicationContext().getString(R.string.load_error_message)
                                                + ", " + message);
                            } catch (IOException e) {
                                e.printStackTrace();
                                repository.dispatchInfoForRecipe.onNext(
                                        getApplicationContext().getString(R.string.load_error_message));
                            }
                        }
                    }, throwable -> {
                        repository.dispatchInfoForRecipe.onNext(
                                getApplicationContext().getString(R.string.load_error_message));
                        CrashLogger.logException(throwable);
                        emitter.onSuccess(Result.failure());
                    })
                )
        );
    }

    public static OneTimeWorkRequest getRecipeContentWorker(@NonNull String recipeId, @Nullable String lastModified) {
        // Create a Constraints object that defines when the task should run
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // then create a OneTimeWorkRequest that uses those constraints

        return new OneTimeWorkRequest.Builder(GetRecipeContentWorker.class)
                .setConstraints(myConstraints)
                .setInputData(new Data.Builder()
                        .putString(Constants.RECIPE_ID, recipeId)
                        .putString(Constants.CONTENT_MODIFIED, lastModified)
                        .build())
                .build();
    }
}
