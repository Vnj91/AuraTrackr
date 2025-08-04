package com.example.auratrackr.data.repository

import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * The concrete implementation of the UserRepository interface.
 * This class handles all data operations with Firebase Firestore for user profiles.
 *
 * @property firestore An instance of FirebaseFirestore, injected by Hilt.
 */
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override fun getUserProfile(uid: String): Flow<User?> = callbackFlow {
        val snapshotListener = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user).isSuccess
            }
        awaitClose { snapshotListener.remove() }
    }

    override suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit> {
        return try {
            val updates = mapOf(
                "weightInKg" to weightInKg,
                "heightInCm" to heightInCm,
                "hasCompletedOnboarding" to true
            )
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAuraPoints(uid: String, pointsToAdd: Int): Result<Unit> {
        return try {
            val userDocRef = usersCollection.document(uid)
            userDocRef.update("auraPoints", FieldValue.increment(pointsToAdd.toLong())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atomically subtracts a specified number of Aura Points from a user's profile.
     * This is done within a Firestore transaction to ensure safety.
     */
    override suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit> {
        return try {
            val userDocRef = usersCollection.document(uid)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val currentPoints = snapshot.getLong("auraPoints") ?: 0L

                // Check if the user has enough points
                if (currentPoints >= pointsToSpend) {
                    transaction.update(userDocRef, "auraPoints", FieldValue.increment(-pointsToSpend.toLong()))
                    null // Success
                } else {
                    // Throw an exception to abort the transaction
                    throw FirebaseFirestoreException("Insufficient points", FirebaseFirestoreException.Code.ABORTED)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
