package com.ronginat.family_recipes.background.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        lastModifiedContent = workerParams.getInputData().getString(Constants.LAST_MODIFIED);
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
                compositeDisposable.add(APICallsHandler.getRecipeContentObservable(this.recipeId, this.lastModifiedContent, AppHelper.getAccessToken())
                    .subscribe(response -> {
                        if (response.isSuccessful() && response.body() != null) {
                            repository.insertContentRecipe(response.body().toEntity());
                            emitter.onSuccess(Result.success());
                        }
                        else if (!response.isSuccessful() && response.code() != 304) {
                            if (response.errorBody() != null) {
                                try {
                                    String message = response.errorBody().string();
                                    repository.dispatchInfoForRecipe.onNext(message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    repository.dispatchInfoForRecipe.onNext(
                                            getApplicationContext().getString(R.string.load_error_message));
                                }
                            } else
                                repository.dispatchInfoForRecipe.onNext(
                                        getApplicationContext().getString(R.string.load_error_message));
                            emitter.onSuccess(Result.failure());
                        } else
                            emitter.onSuccess(Result.success());
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
                        .putString(Constants.LAST_MODIFIED, lastModified)
                        .build())
                .build();
    }
}
