package com.example.gympulse.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gympulse.geofence.GeofenceNotificationHelper
import com.example.gympulse.repository.SessionRepository

class AutoCheckoutWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val sessionRepository = SessionRepository()

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val gymId = inputData.getString("gymId") ?: return Result.failure()

        Log.d("GymPulse", "AutoCheckoutWorker executing for user: $userId")

        return try {
            val result = sessionRepository.autoCheckOut(userId, gymId)
            if (result.isSuccess) {
                Log.d("GymPulse", "Auto checkout successful. Sending confirmation...")
                GeofenceNotificationHelper.showAutoCheckoutConfirmationNotification(applicationContext)
                Result.success()
            } else {
                val exception = result.exceptionOrNull()
                if (exception?.message?.contains("No active session found") == true) {
                    Log.d("GymPulse", "No active session found. Skipping auto checkout.")
                    Result.success()
                } else {
                    Log.e("GymPulse", "Auto checkout failed: ${exception?.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("GymPulse", "AutoCheckoutWorker unexpected error: ${e.message}")
            Result.retry()
        }
    }
}
