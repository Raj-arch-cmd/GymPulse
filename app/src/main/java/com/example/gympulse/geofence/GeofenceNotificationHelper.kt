package com.example.gympulse.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gympulse.MainActivity
import com.example.gympulse.util.Constants

object GeofenceNotificationHelper {

    private const val CHANNEL_ID = "gympulse_geofence"
    private const val CHANNEL_NAME = "GymPulse Geofence"

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            
            // Primary Geofence Channel
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }

            // Recovery Channel
            if (manager.getNotificationChannel(Constants.CHANNEL_ID_RECOVERY) == null) {
                val recoveryChannel = NotificationChannel(
                    Constants.CHANNEL_ID_RECOVERY,
                    Constants.CHANNEL_NAME_RECOVERY,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(recoveryChannel)
            }
        }
    }

    fun showCheckInNotification(context: Context, gymId: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("gymId", gymId)
            putExtra("action", "checkin")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("You're at the gym!")
            .setContentText("Tap to check in to GymPulse")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1001, notification)
    }

    fun showCheckOutNotification(context: Context) {
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Great workout!")
            .setContentText("You've been automatically checked out of the gym")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1002, notification)
    }

    fun showAutoCheckInNotification(context: Context) {
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("GymPulse: Session Started")
            .setContentText("You've arrived! We've automatically checked you in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1003, notification)
    }

    fun showCheckoutReminderNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID_RECOVERY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Forget to check out?")
            .setContentText("We noticed you left the gym. Tap to check out manually.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(Constants.NOTIFICATION_ID_REMINDER, notification)
    }

    fun showAutoCheckoutConfirmationNotification(context: Context) {
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID_RECOVERY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("GymPulse: Session Ended")
            .setContentText("Your session was automatically ended since you left the gym.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(Constants.NOTIFICATION_ID_AUTO_CHECKOUT, notification)
    }
}
