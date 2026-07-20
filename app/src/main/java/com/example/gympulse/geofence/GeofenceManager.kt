package com.example.gympulse.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun addGeofence(gymId: String, latitude: Double, longitude: Double) {
        if (!hasLocationPermission()) {
            Log.e("Geofence", "Location permission not granted")
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(gymId)
            .setCircularRegion(latitude, longitude, 150f) // 150m radius
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // We listen for EXIT (leaving) for auto-checkout recovery
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener { Log.d("Geofence", "Exit Geofence added for: $gymId") }
            .addOnFailureListener { e -> Log.e("Geofence", "Failed: ${e.message}") }
    }

    // ... removeGeofence and removeAllGeofences remain the same ...

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                Log.d("Geofence", "All geofences removed successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Geofence", "Failed to remove all geofences: ${e.message}")
            }
    }
}