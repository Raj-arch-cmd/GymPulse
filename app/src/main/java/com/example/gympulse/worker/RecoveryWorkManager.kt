package com.example.gympulse.worker

import android.content.Context
import android.util.Log
import androidx.work.WorkManager

object RecoveryWorkManager {

    /**
     * Cancels any pending recovery work (reminders or auto-checkouts)
     * for the specified user.
     */
    fun cancelRecovery(context: Context, userId: String) {
        val workName = "recovery_$userId"
        Log.d("GymPulse", "Cancelling unique work: $workName")
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }
}
