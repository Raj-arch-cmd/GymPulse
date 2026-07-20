package com.example.gympulse.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.gympulse.geofence.GeofenceNotificationHelper
import com.example.gympulse.repository.SessionRepository
import com.example.gympulse.util.Constants
import java.util.concurrent.TimeUnit

class CheckoutReminderWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val sessionRepository = SessionRepository()

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val gymId = inputData.getString("gymId") ?: return Result.failure()

        Log.d("GymPulse", "CheckoutReminderWorker executing for user: $userId")

        return try {
            val activeSession = sessionRepository.getActiveSession(userId, gymId)

            if (activeSession != null) {
                Log.d("GymPulse", "Session still active. Showing reminder...")
                
                // 1. Show the reminder notification
                GeofenceNotificationHelper.showCheckoutReminderNotification(applicationContext)

                // 2. Schedule the final Auto Checkout worker
                val workRequest = OneTimeWorkRequestBuilder<AutoCheckoutWorker>()
                    .setInitialDelay(Constants.AUTO_CHECKOUT_DELAY_MINUTES, TimeUnit.MINUTES)
                    .setInputData(workDataOf("userId" to userId, "gymId" to gymId))
                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "recovery_$userId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
                
                Result.success()
            } else {
                Log.d("GymPulse", "User already checked out manually. Recovery chain terminated.")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("GymPulse", "CheckoutReminderWorker failed: ${e.message}")
            Result.retry()
        }
    }
}
