package com.myapps.ron.family_recipes.background.workers;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.logic.Injection;
import com.myapps.ron.family_recipes.logic.repository.RecipeRepository;
import com.myapps.ron.family_recipes.layout.APICallsHandler;
import com.myapps.ron.family_recipes.layout.cognito.AppHelper;
import com.myapps.ron.family_recipes.layout.modelTO.RecipeTO;
import com.myapps.ron.family_recipes.utils.Constants;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import retrofit2.Response;

/**
 * Created by ronginat on 08/04/2019.
 */
public class GetOneRecipeWorker extends RxWorker {

    private final String recipeId;
    private final RecipeRepository repository;
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker,
     *                     which recipe to download
     */
    public GetOneRecipeWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        recipeId = workerParams.getInputData().getString(Constants.RECIPE_ID);
        repository = Injection.provideRecipeRepository(getApplicationContext());
    }

    @Override
    public Single<Result> createWork() {
        return Single.create(emitter -> {
            String token = AppHelper.getAccessToken();
            if (token != null) {
                getOneRecipeWithAccessToken(emitter, token);
            } else {
                AppHelper.currSessionObservable.subscribe(new DisposableObserver<CognitoUserSession>() {
                    @Override
                    public void onNext(CognitoUserSession cognitoUserSession) {
                        if (cognitoUserSession != null) {
                            getOneRecipeWithAccessToken(emitter, cognitoUserSession.getAccessToken().getJWTToken());
                            dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                        e.printStackTrace();
                        emitter.onSuccess(Result.failure());
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        dispose();
                    }
                });
            }
        });
    }

    private void getOneRecipeWithAccessToken(SingleEmitter<ListenableWorker.Result> emitter, String token) {
        APICallsHandler.getRecipeObservable(recipeId, token)
                .subscribe(new DisposableObserver<Response<RecipeTO>>() {
                    @Override
                    public void onNext(Response<RecipeTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            repository.insertRecipeCompletable(response.body().toEntity())
                                    .subscribe(new DisposableCompletableObserver() {
                                        @Override
                                        public void onComplete() {
                                            emitter.onSuccess(Result.success());
                                            dispose();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            emitter.onSuccess(Result.failure());
                                            dispose();
                                        }
                                    });
                        } else
                            repository.dispatchInfo.onNext(
                                    getApplicationContext().getString(R.string.load_error_message)
                                            + " " + response.message());
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(getClass().getSimpleName(), t.getMessage());
                        t.printStackTrace();
                        emitter.onSuccess(Result.failure());
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(getClass().getSimpleName(), "API complete");
                        if (!isDisposed())
                            dispose();
                    }
                });
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
