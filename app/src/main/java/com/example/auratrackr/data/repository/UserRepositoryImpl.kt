package com.example.auratrackr.data.repository

import android.net.Uri
import android.util.Log
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.RequestStatus
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
        private const val USERS_COLLECTION = "users"
        private const val FRIEND_REQUESTS_COLLECTION = "friend_requests"
        private const val SUMMARIES_COLLECTION = "summaries"
        private const val PROFILE_PICTURES_PATH = "profile_pictures"

        private const val FIELD_USERNAME = "username"
        private const val FIELD_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
        private const val FIELD_WEIGHT_KG = "weightInKg"
        private const val FIELD_HEIGHT_CM = "heightInCm"
        private const val FIELD_PROFILE_PICTURE_URL = "profilePictureUrl"
        private const val FIELD_AURA_POINTS = "auraPoints"
        private const val FIELD_FRIENDS = "friends"
        private const val FIELD_STATUS = "status"
        private const val FIELD_UID = "uid"
    }

    override fun getUserProfile(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getUserProfile failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createUserProfile(user: User): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(user.uid).set(user).await()
    }

    override suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit> = runCatching {
        val updates = mapOf(
            FIELD_WEIGHT_KG to weightInKg,
            FIELD_HEIGHT_CM to heightInCm,
            FIELD_HAS_COMPLETED_ONBOARDING to true
        )
        firestore.collection(USERS_COLLECTION).document(uid).update(updates).await()
    }

    override suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child("$PROFILE_PICTURES_PATH/${uid}.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            firestore.collection(USERS_COLLECTION).document(uid).update(FIELD_PROFILE_PICTURE_URL, downloadUrl).await()
            Result.success(downloadUrl)
        } catch (e: StorageException) {
            Log.e(TAG, "uploadProfilePicture failed: StorageException", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "uploadProfilePicture failed: General Exception", e)
            Result.failure(e)
        }
    }

    override suspend fun addAuraPoints(uid: String, pointsToAdd: Int): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(uid)
            .update(FIELD_AURA_POINTS, FieldValue.increment(pointsToAdd.toLong())).await()
    }

    override suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
                val snapshot = transaction.get(userDocRef)
                val currentPoints = snapshot.getLong(FIELD_AURA_POINTS) ?: 0L
                if (currentPoints >= pointsToSpend) {
                    transaction.update(userDocRef, FIELD_AURA_POINTS, currentPoints - pointsToSpend)
                    null
                } else {
                    throw FirebaseFirestoreException("Insufficient points", FirebaseFirestoreException.Code.ABORTED)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "spendAuraPoints failed", e)
            Result.failure(e)
        }
    }

    override suspend fun searchUsersByUsername(query: String, currentUserId: String): Result<List<User>> {
        return try {
            // Use a range query for prefix matching, which is more effective for search.
            val snapshot = firestore.collection(USERS_COLLECTION)
                .orderBy(FIELD_USERNAME)
                .startAt(query)
                .endAt(query + '\uf8ff')
                .limit(10)
                .get().await()
            val users = snapshot.toObjects(User::class.java).filter { it.uid != currentUserId }
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "searchUsersByUsername failed", e)
            Result.failure(e)
        }
    }

    override suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit> = runCatching {
        val request = FriendRequest(
            senderId = sender.uid,
            senderUsername = sender.username ?: "AuraTrackr User", // Defensive fallback
            senderProfileImageUrl = sender.profilePictureUrl,
            receiverId = receiverId
        )
        firestore.collection(USERS_COLLECTION).document(receiverId).collection(FRIEND_REQUESTS_COLLECTION).add(request).await()
    }

    override fun getFriendRequests(uid: String): Flow<List<FriendRequest>> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION).document(uid).collection(FRIEND_REQUESTS_COLLECTION)
            .whereEqualTo(FIELD_STATUS, RequestStatus.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getFriendRequests failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(FriendRequest::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val senderDocRef = firestore.collection(USERS_COLLECTION).document(request.senderId)
                val receiverDocRef = firestore.collection(USERS_COLLECTION).document(request.receiverId)
                val requestDocRef = firestore.collection(USERS_COLLECTION).document(request.receiverId)
                    .collection(FRIEND_REQUESTS_COLLECTION).document(request.id)

                transaction.update(senderDocRef, FIELD_FRIENDS, FieldValue.arrayUnion(request.receiverId))
                transaction.update(receiverDocRef, FIELD_FRIENDS, FieldValue.arrayUnion(request.senderId))
                transaction.update(requestDocRef, FIELD_STATUS, RequestStatus.ACCEPTED)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "acceptFriendRequest failed", e)
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(request: FriendRequest): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(request.receiverId)
            .collection(FRIEND_REQUESTS_COLLECTION).document(request.id)
            .update(FIELD_STATUS, RequestStatus.DECLINED).await()
    }

    override suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val currentUserDocRef = firestore.collection(USERS_COLLECTION).document(currentUserId)
                val friendDocRef = firestore.collection(USERS_COLLECTION).document(friendId)

                transaction.update(currentUserDocRef, FIELD_FRIENDS, FieldValue.arrayRemove(friendId))
                transaction.update(friendDocRef, FIELD_FRIENDS, FieldValue.arrayRemove(currentUserId))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "removeFriend failed", e)
            Result.failure(e)
        }
    }

    override fun getFriends(uid: String): Flow<List<User>> = callbackFlow {
        val userListener = firestore.collection(USERS_COLLECTION).document(uid)
            .addSnapshotListener { userSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getFriends user listener failed", error)
                    close(error)
                    return@addSnapshotListener
                }

                val friendIds = userSnapshot?.toObject(User::class.java)?.friends
                if (friendIds.isNullOrEmpty()) {
                    trySend(emptyList())
                } else {
                    // Handle Firestore's 10-item limit for 'whereIn' queries by batching.
                    launch {
                        try {
                            val friendChunks = friendIds.chunked(10)
                            val allFriends = mutableListOf<User>()
                            friendChunks.forEach { chunk ->
                                val friendsSnapshot = firestore.collection(USERS_COLLECTION).whereIn(FIELD_UID, chunk).get().await()
                                allFriends.addAll(friendsSnapshot.toObjects(User::class.java))
                            }
                            trySend(allFriends)
                        } catch (e: Exception) {
                            Log.e(TAG, "getFriends chunk query failed", e)
                            close(e)
                        }
                    }
                }
            }
        awaitClose { userListener.remove() }
    }

    override fun getUserSummary(uid: String, year: String): Flow<UserSummary?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION).document(uid)
            .collection(SUMMARIES_COLLECTION).document(year)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getUserSummary failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserSummary::class.java))
            }
        awaitClose { listener.remove() }
    }
}