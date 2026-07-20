package com.example.gympulse.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AutoCheckoutWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        // Implementation will be added in Task 5
        return Result.success()
    }
}
