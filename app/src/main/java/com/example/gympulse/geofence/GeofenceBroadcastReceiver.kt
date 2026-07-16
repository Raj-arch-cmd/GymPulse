package com.example.gympulse.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.gympulse.repository.SessionRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                // AUTO CHECK-IN: Only after loitering for 1 minute
                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    Log.d("Geofence", "User is at $gymId. Auto checking in...")
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = sessionRepository.checkIn(userId, gymId)
                        if (result.isSuccess) {
                            GeofenceNotificationHelper.showAutoCheckInNotification(context)
                        }
                    }
                }

                // AUTO CHECK-OUT: Triggered immediately on exit
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("Geofence", "Exited gym: $gymId. Auto checking out...")
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = sessionRepository.checkOut(userId, gymId)
                        if (result.isSuccess) {
                            GeofenceNotificationHelper.showCheckOutNotification(context)
                        }
                    }
                }
            }
        }
    }
}