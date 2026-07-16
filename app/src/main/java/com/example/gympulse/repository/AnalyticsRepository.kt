package com.example.gympulse.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AnalyticsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val sessionsCollection = firestore.collection("sessions")

    /**
     * Fetches all finished sessions for a gym and groups them by hour.
     * Returns a string like: "06:00 - 10 people, 17:00 - 45 people"
     */
    suspend fun getGymHourlyStats(gymId: String): String {
        return try {
            val snapshot = sessionsCollection
                .whereEqualTo("gymId", gymId)
                // Filter for completed sessions to get accurate historical data
                .whereIn("sessionStatus", listOf("manual_checkout", "auto_checkout"))
                .get()
                .await()

            val hourlyMap = mutableMapOf<Int, Int>()

            for (doc in snapshot.documents) {
                val hour = doc.getLong("hourSlot")?.toInt() ?: continue
                hourlyMap[hour] = hourlyMap.getOrDefault(hour, 0) + 1
            }

            if (hourlyMap.isEmpty()) return "No historical data available yet."

            hourlyMap.entries
                .sortedBy { it.key }
                .joinToString(", ") { "${it.key}:00 - ${it.value} visits" }

        } catch (e: Exception) {
            Log.e("Analytics", "Error fetching stats: ${e.message}")
            "Error retrieving gym data."
        }
    }
}