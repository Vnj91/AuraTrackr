package com.example.auratrackr.data.repository

import com.example.auratrackr.domain.model.Challenge
import com.example.auratrackr.domain.repository.ChallengeRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The concrete implementation of the [ChallengeRepository].
 * This class handles all data operations with Firebase Firestore for group challenges,
 * using coroutines and Flow for asynchronous and reactive data handling.
 */
@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChallengeRepository {

    companion object {
        private const val CHALLENGES_COLLECTION = "challenges"
        private const val FIELD_PARTICIPANTS = "participants"
        private const val FIELD_CURRENT_PROGRESS = "currentProgress"
    }

    override suspend fun createChallenge(challenge: Challenge): Result<Unit> {
        return try {
            firestore.collection(CHALLENGES_COLLECTION).add(challenge).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "createChallenge failed")
            Result.failure(e)
        }
    }

    override fun getChallenge(challengeId: String): Flow<Challenge?> = callbackFlow {
        val docRef = firestore.collection(CHALLENGES_COLLECTION).document(challengeId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "getChallenge failed for id: $challengeId")
                close(error)
                return@addSnapshotListener
            }
            val challenge = snapshot?.toObject(Challenge::class.java)
            trySend(challenge)
        }
        awaitClose { listener.remove() }
    }

    override fun getUserChallenges(uid: String): Flow<List<Challenge>> = callbackFlow {
        val listener = firestore.collection(CHALLENGES_COLLECTION)
            .whereArrayContains(FIELD_PARTICIPANTS, uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "getUserChallenges failed for uid: $uid")
                    close(error)
                    return@addSnapshotListener
                }
                val challenges = snapshot?.toObjects(Challenge::class.java) ?: emptyList()
                trySend(challenges)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun joinChallenge(challengeId: String, uid: String): Result<Unit> {
        return try {
            firestore.collection(CHALLENGES_COLLECTION).document(challengeId)
                .update(FIELD_PARTICIPANTS, FieldValue.arrayUnion(uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "joinChallenge failed")
            Result.failure(e)
        }
    }

    override suspend fun leaveChallenge(challengeId: String, uid: String): Result<Unit> {
        return try {
            firestore.collection(CHALLENGES_COLLECTION).document(challengeId)
                .update(FIELD_PARTICIPANTS, FieldValue.arrayRemove(uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "leaveChallenge failed")
            Result.failure(e)
        }
    }

    override suspend fun updateChallengeProgress(challengeId: String, progressToAdd: Long): Result<Unit> {
        return try {
            firestore.collection(CHALLENGES_COLLECTION).document(challengeId)
                .update(FIELD_CURRENT_PROGRESS, FieldValue.increment(progressToAdd)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "updateChallengeProgress failed")
            Result.failure(e)
        }
    }
}
