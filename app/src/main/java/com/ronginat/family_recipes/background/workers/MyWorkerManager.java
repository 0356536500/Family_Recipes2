package com.ronginat.family_recipes.background.workers;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ronginat.family_recipes.utils.logic.CrashLogger;

/**
 * Created by ronginat on 16/08/2019
 */
public class MyWorkerManager {

    private MyWorkerManager() {
    }

    public static class Builder {
        private Context context;
        //private OneTimeWorkRequest beginWithRequest;
        private boolean batteryNotTooLow = false;
        private OneTimeWorkRequest oneTimeWorkRequest;

        public Builder() {
            super();
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder requiresBattery(boolean batteryNoTooLow) {
            this.batteryNotTooLow = batteryNoTooLow;
            return this;
        }

        public Builder nextWorkRequest(OneTimeWorkRequest oneTimeWorkRequest) {
            this.oneTimeWorkRequest = oneTimeWorkRequest;
            return this;
        }

        public void startWork() {
            try {
                WorkManager.getInstance(context)
                        .beginWith(BeginContinuationWorker.getSessionWaiterWorker(this.batteryNotTooLow))
                        .then(this.oneTimeWorkRequest)
                        .enqueue();
            } catch (Exception ex) {
                ex.printStackTrace();
                CrashLogger.logException(ex);
            }
        }

    }
}
