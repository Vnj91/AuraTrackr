package com.example.auratrackr.data.repository

import com.example.auratrackr.domain.model.Challenge
import com.example.auratrackr.domain.repository.ChallengeRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The concrete implementation of the ChallengeRepository.
 * This class handles all data operations with Firebase Firestore for group challenges.
 */
@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChallengeRepository {

    // A reference to the top-level "challenges" collection
    private val challengesCollection = firestore.collection("challenges")

    override suspend fun createChallenge(challenge: Challenge): Result<Unit> {
        return try {
            // Create a new document in the challenges collection
            challengesCollection.add(challenge).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserChallenges(uid: String): Flow<List<Challenge>> = callbackFlow {
        // This query finds all challenges where the user's ID is in the "participants" array
        val listener = challengesCollection
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val challenges = snapshot?.toObjects(Challenge::class.java) ?: emptyList()
                trySend(challenges).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override suspend fun joinChallenge(challengeId: String, uid: String): Result<Unit> {
        return try {
            // Atomically add the user's ID to the participants list
            challengesCollection.document(challengeId)
                .update("participants", FieldValue.arrayUnion(uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChallengeProgress(challengeId: String, progressToAdd: Long): Result<Unit> {
        return try {
            // Atomically increment the current progress of the challenge
            challengesCollection.document(challengeId)
                .update("currentProgress", FieldValue.increment(progressToAdd)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
