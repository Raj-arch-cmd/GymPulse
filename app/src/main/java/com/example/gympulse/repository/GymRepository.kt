package com.example.gympulse.repository

import com.example.gympulse.model.Gym
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GymRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val gymsCollection = firestore.collection("gyms")

    suspend fun registerGym(gym: Gym): Result<String> {
        return try {
            val docRef = gymsCollection.document()
            val gymWithId = gym.copy(gymId = docRef.id)
            docRef.set(gymWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllGyms(): Result<List<Gym>> {
        return try {
            val snapshot = gymsCollection.get().await()
            val gyms = snapshot.documents.mapNotNull { it.toObject(Gym::class.java) }
            Result.success(gyms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGymById(gymId: String): Result<Gym> {
        return try {
            val doc = gymsCollection.document(gymId).get().await()
            val gym = doc.toObject(Gym::class.java) ?: throw Exception("Gym not found")
            Result.success(gym)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGymCount(gymId: String, increment: Boolean): Result<Unit> {
        return try {
            val doc = gymsCollection.document(gymId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(doc)
                val currentCount = snapshot.getLong("currentCount") ?: 0
                val newCount = if (increment)
                    currentCount + 1 else maxOf(0, currentCount - 1)
                transaction.update(doc, "currentCount", newCount)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToGymCount(gymId: String, onUpdate: (Int) -> Unit) =
        gymsCollection.document(gymId)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.getLong("currentCount")?.toInt() ?: 0
                onUpdate(count)
            }
}