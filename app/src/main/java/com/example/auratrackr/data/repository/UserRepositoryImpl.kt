package com.example.auratrackr.data.repository

import android.content.Context
import android.net.Uri
import com.example.auratrackr.core.util.safeResult
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.PointsHistory
import com.example.auratrackr.domain.model.RequestStatus
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
        private const val USERS_COLLECTION = "users"
        private const val FRIEND_REQUESTS_COLLECTION = "friend_requests"
        private const val SUMMARIES_COLLECTION = "summaries"
        private const val PROFILE_PICTURES_PATH = "profile_pictures"
        private const val POINTS_HISTORY_COLLECTION = "points_history"

        private const val FIELD_USERNAME = "username"
        private const val FIELD_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
        private const val FIELD_WEIGHT_KG = "weightInKg"
        private const val FIELD_HEIGHT_CM = "heightInCm"
        private const val FIELD_PROFILE_PICTURE_URL = "profilePictureUrl"
        private const val FIELD_AURA_POINTS = "auraPoints"
        private const val FIELD_FRIENDS = "friends"
        private const val FIELD_STATUS = "status"
        // FIELD_UID removed as it was unused; keep only fields the repository references.
        
        // Search and chunk constants (extracted to avoid magic numbers in queries)
        private const val SEARCH_LIMIT = 10
        private const val SEARCH_UNICODE_END = "\uF8FF"
    }

    override fun getUserProfile(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "getUserProfile failed")
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
            FIELD_WEIGHT_KG to weightInKg.toLong(),
            FIELD_HEIGHT_CM to heightInCm.toLong(),
            FIELD_HAS_COMPLETED_ONBOARDING to true
        )
        firestore.collection(USERS_COLLECTION).document(uid).update(updates).await()
    }

    override suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String> {
        return safeResult(TAG, "uploadProfilePicture") {
            val storageRef = storage.reference.child("$PROFILE_PICTURES_PATH/$uid.jpg")

            // Take persistable URI permission to prevent "object does not exist" error
            try {
                context.contentResolver.takePersistableUriPermission(
                    imageUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Timber.w(TAG, "Could not take persistable permission, attempting upload anyway")
            }

            // Open input stream directly from URI to avoid permission issues
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalStateException("Cannot open input stream from URI")

            // 1. Upload using input stream
            val uploadTaskSnapshot = retryWithDelay {
                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()
                storageRef.putStream(inputStream, metadata).await()
            }

            inputStream.close()

            // 2. Use the metadata from the successful upload to get a safe reference.
            val safeStorageRef = uploadTaskSnapshot.metadata?.reference
                ?: throw IllegalStateException("Upload succeeded but metadata is null.")

            // 3. Retry fetching the URL to handle any rare backend propagation delays.
            val downloadUrl = retryWithDelay {
                safeStorageRef.downloadUrl.await().toString()
            }

            // 4. Finally, update Firestore with the guaranteed URL.
            firestore.collection(USERS_COLLECTION).document(uid).update(FIELD_PROFILE_PICTURE_URL, downloadUrl).await()
            downloadUrl
        }
    }

    override suspend fun addAuraPoints(uid: String, pointsToAdd: Int, vibeId: String): Result<Unit> {
        return safeResult(TAG, "addAuraPoints") {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
            val pointsHistoryRef = userDocRef.collection(POINTS_HISTORY_COLLECTION).document()

            val pointsHistory = PointsHistory(
                points = pointsToAdd,
                vibeId = vibeId,
                source = "COMPLETED_WORKOUT"
            )

            firestore.runTransaction { transaction ->
                transaction.update(userDocRef, FIELD_AURA_POINTS, FieldValue.increment(pointsToAdd.toLong()))
                transaction.set(pointsHistoryRef, pointsHistory)
            }.await()

            Unit
        }
    }

    override suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit> {
        return safeResult(TAG, "spendAuraPoints") {
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
            Unit
        }
    }

    override fun getPointsHistory(uid: String): Flow<List<PointsHistory>> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION).document(uid)
            .collection(POINTS_HISTORY_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "getPointsHistory failed")
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(PointsHistory::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun searchUsersByUsername(query: String, currentUserId: String): Result<List<User>> {
        return safeResult(TAG, "searchUsersByUsername") {
            val usersCollection = firestore.collection(USERS_COLLECTION)
            val queryRef = usersCollection
                .orderBy(FIELD_USERNAME)
                .startAt(query)
                .endAt(query + SEARCH_UNICODE_END)
                .limit(SEARCH_LIMIT.toLong())

            val snapshot = queryRef.get().await()
            snapshot.toObjects(User::class.java)
                .filter { it.uid != currentUserId }
        }
    }

    override suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit> = runCatching {
        val request = FriendRequest(
            senderId = sender.uid,
            senderUsername = sender.username ?: "AuraTrackr User",
            senderProfileImageUrl = sender.profilePictureUrl,
            receiverId = receiverId
        )
        firestore.collection(USERS_COLLECTION)
            .document(receiverId)
            .collection(FRIEND_REQUESTS_COLLECTION)
            .add(request)
            .await()
    }

    override fun getFriendRequests(uid: String): Flow<List<FriendRequest>> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION).document(uid).collection(FRIEND_REQUESTS_COLLECTION)
            .whereEqualTo(FIELD_STATUS, RequestStatus.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "getFriendRequests failed")
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(FriendRequest::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        return safeResult(TAG, "acceptFriendRequest") {
            firestore.runTransaction { transaction ->
                val senderDocRef = firestore.collection(USERS_COLLECTION).document(request.senderId)
                val receiverDocRef = firestore.collection(USERS_COLLECTION).document(request.receiverId)
                val requestDocRef = firestore
                    .collection(USERS_COLLECTION)
                    .document(request.receiverId)
                    .collection(FRIEND_REQUESTS_COLLECTION)
                    .document(request.id)

                transaction.update(senderDocRef, FIELD_FRIENDS, FieldValue.arrayUnion(request.receiverId))
                transaction.update(receiverDocRef, FIELD_FRIENDS, FieldValue.arrayUnion(request.senderId))
                transaction.update(requestDocRef, FIELD_STATUS, RequestStatus.ACCEPTED)
            }.await()
            Unit
        }
    }

    override suspend fun declineFriendRequest(request: FriendRequest): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(request.receiverId)
            .collection(FRIEND_REQUESTS_COLLECTION).document(request.id)
            .update(FIELD_STATUS, RequestStatus.DECLINED).await()
    }

    override suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit> {
        return safeResult(TAG, "removeFriend") {
            firestore.runTransaction { transaction ->
                val currentUserDocRef = firestore.collection(USERS_COLLECTION).document(currentUserId)
                val friendDocRef = firestore.collection(USERS_COLLECTION).document(friendId)

                transaction.update(currentUserDocRef, FIELD_FRIENDS, FieldValue.arrayRemove(friendId))
                transaction.update(friendDocRef, FIELD_FRIENDS, FieldValue.arrayRemove(currentUserId))
            }.await()
            Unit
        }
    }

    override fun getFriends(uid: String): Flow<List<User>> = callbackFlow {
        val userListener = firestore.collection(USERS_COLLECTION).document(uid)
            .addSnapshotListener { userSnapshot, error ->
                if (error != null) {
                    Timber.e(error, "getFriends user listener failed")
                    close(error)
                    return@addSnapshotListener
                }

                val friendIds = userSnapshot?.toObject(User::class.java)?.friends
                if (friendIds.isNullOrEmpty()) {
                    trySend(emptyList())
                } else {
                    launch {
                        try {
                            val friendChunks = friendIds.chunked(SEARCH_LIMIT)
                            val allFriends = mutableListOf<User>()
                            friendChunks.forEach { chunk ->
                                // Use repository helper to fetch users by ids. This moves
                                // Firestore query logic out of the large repository file.
                                val friends = fetchUsersByIds(firestore, chunk)
                                allFriends.addAll(friends)
                            }
                            trySend(allFriends)
                        } catch (e: FirebaseFirestoreException) {
                            // Narrow the caught exception to Firebase errors to avoid hiding unexpected failures.
                            Timber.e(e, "getFriends chunk query failed (firestore)")
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
                    Timber.e(error, "getUserSummary failed")
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserSummary::class.java))
            }
        awaitClose { listener.remove() }
    }
}

// Retry helper and retry defaults were moved to `UserRepositoryHelpers.kt` to
// reduce the number of functions inside this large repository implementation
// and keep file responsibilities focused.
