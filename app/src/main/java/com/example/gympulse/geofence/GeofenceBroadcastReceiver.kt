package com.example.gympulse.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gympulse.repository.SessionRepository
import com.example.gympulse.util.Constants
import com.example.gympulse.worker.CheckoutReminderWorker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val sessionRepository = SessionRepository()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e("Geofence", "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        triggeringGeofences.forEach { geofence ->
            val gymId = geofence.requestId

            when (transitionType) {
                // RECOVERY TRIGGER: Start the recovery chain when user leaves the gym
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("Geofence", "Exited gym: $gymId. Checking for active session...")
                    
                    CoroutineScope(Dispatchers.IO).launch {
                        val activeSession = sessionRepository.getActiveSession(userId, gymId)
                        if (activeSession != null) {
                            Log.d("Geofence", "Active session found. Scheduling reminder...")
                            
                            val workRequest = OneTimeWorkRequestBuilder<CheckoutReminderWorker>()
                                .setInitialDelay(Constants.REMINDER_DELAY_MINUTES, TimeUnit.MINUTES)
                                .build()

                            WorkManager.getInstance(context).enqueueUniqueWork(
                                "recovery_$userId",
                                ExistingWorkPolicy.REPLACE,
                                workRequest
                            )
                        } else {
                            Log.d("Geofence", "No active session. Recovery not needed.")
                        }
                    }
                }
            }
        }
    }
}
