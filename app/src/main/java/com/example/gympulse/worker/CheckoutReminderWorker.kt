package com.example.gympulse.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CheckoutReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        // Implementation will be added in Task 4
        return Result.success()
    }
}
