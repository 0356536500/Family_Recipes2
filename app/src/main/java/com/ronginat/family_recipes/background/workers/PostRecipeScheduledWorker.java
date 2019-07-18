package com.ronginat.family_recipes.background.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ronginat.family_recipes.background.services.PostEnqueuedRecipesService;

/**
 * Created by ronginat on 20/02/2019.
 */
public class PostRecipeScheduledWorker extends Worker {

    public PostRecipeScheduledWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        //Log.e(TAG, "doWork");
        PostEnqueuedRecipesService.startActionPostRecipeFromQueue(getApplicationContext());
        //Log.e(TAG, "finish work");
        // Indicate success or failure with your return value:
        return Result.success();

        // Returning Result.retry() tells WorkManager to try this task again later
        // Result.failure() says not to try again
    }

    static OneTimeWorkRequest createPostRecipesWorker() {
        // Create a Constraints object that defines when the task should run
        Constraints myConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        // then create a OneTimeWorkRequest that uses those constraints
        return new OneTimeWorkRequest.Builder(PostRecipeScheduledWorker.class)
                .setConstraints(myConstraints)
                .build();
    }
}
