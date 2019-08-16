package com.ronginat.family_recipes.background.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.ronginat.family_recipes.layout.cognito.AppHelper;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ronginat on 20/06/2019
 *
 * This worker is waiting for a valid cognito session.
 * When acquired, continue with the actual work
 */
public class BeginContinuationWorker extends RxWorker {

    private CompositeDisposable compositeDisposable;

    public BeginContinuationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.compositeDisposable = new CompositeDisposable();
    }

    @NonNull
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

    static OneTimeWorkRequest getSessionWaiterWorker(boolean batteryNoTooLow) {
        // Create a Constraints object that defines when the task should run
        Constraints.Builder constraintsBuilder =  new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(batteryNoTooLow);

        // then create a OneTimeWorkRequest that uses those constraints

        return new OneTimeWorkRequest.Builder(BeginContinuationWorker.class)
                .setConstraints(constraintsBuilder.build())
                .build();
    }
}
