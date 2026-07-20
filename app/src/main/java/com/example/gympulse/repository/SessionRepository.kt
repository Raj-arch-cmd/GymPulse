package com.example.gympulse.repository

import android.util.Log
import com.example.gympulse.model.Session
import com.example.gympulse.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class SessionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val sessionsCollection = firestore.collection("sessions")

    /**
     * Creates a real-time Flow that emits true if the user has an active session
     * and false otherwise. Automatically cleans up the listener when cancelled.
     */
    fun listenToActiveSession(userId: String, gymId: String): Flow<Boolean> = callbackFlow {
        val registration = sessionsCollection
            .whereEqualTo(Constants.FIELD_USER_ID, userId)
            .whereEqualTo(Constants.FIELD_GYM_ID, gymId)
            .whereEqualTo(Constants.FIELD_SESSION_STATUS, Constants.SESSION_STATUS_ACTIVE)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GymPulse", "Firestore Listener Error: ${error.message}")
                    return@addSnapshotListener
                }

                val hasActive = snapshot != null && !snapshot.isEmpty
                trySend(hasActive)
            }

        // Essential: Removes the listener when the ViewModel scope is cleared
        awaitClose {
            Log.d("GymPulse", "Closing session listener for $userId")
            registration.remove()
        }
    }

    suspend fun checkIn(userId: String, gymId: String): Result<String> {
        return try {
            // 1. Pre-check: Does the user already have an active session?
            // This is done before the transaction to identify the session to check against.
            val active = getActiveSession(userId, gymId)
            if (active != null) {
                return Result.failure(Exception("You already have an active session at this gym."))
            }

            val gymRef = firestore.collection("gyms").document(gymId)
            val sessionRef = sessionsCollection.document()

            val calendar = Calendar.getInstance()
            val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            val dayOfWeek = days[calendar.get(Calendar.DAY_OF_WEEK) - 1]
            val hourSlot = calendar.get(Calendar.HOUR_OF_DAY)

            val session = Session(
                sessionId = sessionRef.id,
                userId = userId,
                gymId = gymId,
                checkInTime = Timestamp.now(),
                dayOfWeek = dayOfWeek,
                hourSlot = hourSlot,
                sessionStatus = Constants.SESSION_STATUS_ACTIVE
            )

            firestore.runTransaction { transaction ->
                // 2. Verify Gym exists and read occupancy
                val gymSnapshot = transaction.get(gymRef)
                if (!gymSnapshot.exists()) {
                    throw Exception("Gym not found")
                }

                val currentCount = gymSnapshot.getLong(Constants.FIELD_CURRENT_COUNT) ?: 0

                // 3. Create the Session
                transaction.set(sessionRef, session)

                // 4. Increment Gym occupancy
                transaction.update(gymRef, Constants.FIELD_CURRENT_COUNT, currentCount + 1)

                sessionRef.id
            }.await()

            Result.success(sessionRef.id)
        } catch (e: Exception) {
            Log.e("GymPulse", "Check-in Transaction Failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkOut(userId: String, gymId: String): Result<Unit> {
        return performCheckoutTransaction(userId, gymId, Constants.CHECKOUT_TYPE_MANUAL)
    }

    suspend fun autoCheckOut(userId: String, gymId: String): Result<Unit> {
        return performCheckoutTransaction(userId, gymId, Constants.CHECKOUT_TYPE_AUTO)
    }

    private suspend fun performCheckoutTransaction(
        userId: String,
        gymId: String,
        checkoutType: String
    ): Result<Unit> {
        return try {
            // 1. Find the active session for this user at this gym
            val activeSession = getActiveSession(userId, gymId)
                ?: return Result.failure(Exception("No active session found."))

            val gymRef = firestore.collection("gyms").document(gymId)
            val sessionRef = sessionsCollection.document(activeSession.sessionId)
            val checkOutTime = Timestamp.now()

            // Calculate duration in minutes
            val checkInTime = activeSession.checkInTime ?: checkOutTime
            val durationMillis = checkOutTime.toDate().time - checkInTime.toDate().time
            val durationMinutes = durationMillis / (1000 * 60)

            firestore.runTransaction { transaction ->
                // 2. Read current gym occupancy
                val gymSnapshot = transaction.get(gymRef)
                if (!gymSnapshot.exists()) {
                    throw Exception("Gym not found")
                }

                val currentCount = gymSnapshot.getLong(Constants.FIELD_CURRENT_COUNT) ?: 0
                
                // 3. Update the Session document
                transaction.update(
                    sessionRef,
                    mapOf(
                        "checkOutTime" to checkOutTime,
                        "duration" to durationMinutes,
                        Constants.FIELD_SESSION_STATUS to Constants.SESSION_STATUS_COMPLETED,
                        "checkoutType" to checkoutType
                    )
                )

                // 4. Decrement Gym occupancy (Prevent negative values)
                val newCount = if (currentCount > 0) currentCount - 1 else 0
                transaction.update(gymRef, Constants.FIELD_CURRENT_COUNT, newCount)

                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GymPulse", "$checkoutType Checkout Transaction Failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getActiveSession(userId: String, gymId: String): Session? {
        return try {
            val snapshot = sessionsCollection
                .whereEqualTo(Constants.FIELD_USER_ID, userId)
                .whereEqualTo(Constants.FIELD_GYM_ID, gymId)
                .whereEqualTo(Constants.FIELD_SESSION_STATUS, Constants.SESSION_STATUS_ACTIVE)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(Session::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserSessionCount(userId: String): Int {
        return try {
            val snapshot = sessionsCollection
                .whereEqualTo(Constants.FIELD_USER_ID, userId)
                .whereNotEqualTo(Constants.FIELD_SESSION_STATUS, Constants.SESSION_STATUS_ACTIVE)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}