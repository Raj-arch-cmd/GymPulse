package com.example.gympulse.repository

import com.example.gympulse.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    val currentUser get() = auth.currentUser

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")
            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = role
            )
            usersCollection.document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            if (isNewUser) {
                val user = User(
                    uid = uid,
                    name = result.user?.displayName ?: "",
                    email = result.user?.email ?: "",
                    role = "member"
                )
                usersCollection.document(uid).set(user).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): String {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.getString("role") ?: "member"
        } catch (e: Exception) {
            "member"
        }
    }

    suspend fun updateUserGymId(uid: String, gymId: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update(mapOf("gymId" to gymId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}