package com.myapps.ron.family_recipes.background.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.RxWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.myapps.ron.family_recipes.layout.cognito.AppHelper;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ronginat on 20/06/2019
 *
 * This worker is waiting for a valid cognito session.
 * When acquired, continue with the actual work defined by {@link WORKERS}
 */
public class BeginContinuationWorker extends RxWorker {

    public enum WORKERS { GET_RECIPE, GET_CONTENT, POST_RECIPE, GET_USER }

    private CompositeDisposable compositeDisposable;

    public BeginContinuationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public Single<Result> createWork() {
        if (AppHelper.getAccessToken() != null)
            return Single.just(Result.success());
        return Single.create(emitter ->
                compositeDisposable.add(AppHelper.currSessionObservable.subscribe(
                        next -> emitter.onSuccess(Result.success()),
                        throwable -> emitter.onSuccess(Result.failure())))
        );
    }

    @Override
    public void onStopped() {
        super.onStopped();
        this.compositeDisposable.clear();
    }

    private static OneTimeWorkRequest getSessionWaiterWorker(WORKERS nextWorker) {
        // Create a Constraints object that defines when the task should run
        Constraints.Builder constraintsBuilder =  new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED);
        if (nextWorker.equals(WORKERS.POST_RECIPE))
            constraintsBuilder.setRequiresBatteryNotLow(true);

        // then create a OneTimeWorkRequest that uses those constraints

        return new OneTimeWorkRequest.Builder(BeginContinuationWorker.class)
                .setConstraints(constraintsBuilder.build())
                .build();
    }

    public static void enqueueWorkContinuationWithValidSession(WORKERS nextWorker, String recipeId) {
        try {
            WorkManager.getInstance()
                    .beginWith(getSessionWaiterWorker(nextWorker))
                    .then(getNextWorkerRequest(nextWorker, recipeId))
                    .enqueue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static OneTimeWorkRequest getNextWorkerRequest(WORKERS worker, String recipeId) {
        switch (worker) {
            /*case GET_RECIPE:
                return GetOneRecipeWorker.getOneRecipeWorker(recipeId);
            case GET_CONTENT:
                return GetRecipeContentWorker.getRecipeContentWorker(recipeId, null);
            case POST_RECIPE:
                return PostRecipeScheduledWorker.createPostRecipesWorker();
            case GET_USER:
                return GetUserDetailsWorker.getUserDetailsWorker();*/
            default:
                throw new RuntimeException("Worker is not valid!");
        }
    }
}
