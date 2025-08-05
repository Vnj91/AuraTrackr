package com.example.auratrackr.data.repository

import android.net.Uri
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.RequestStatus
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage // <-- INJECT FIREBASE STORAGE
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    // --- Profile Management ---
    override fun getUserProfile(uid: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.toObject(User::class.java)).isSuccess
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit> {
        return try {
            val updates = mapOf("weightInKg" to weightInKg, "heightInCm" to heightInCm, "hasCompletedOnboarding" to true)
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String> {
        return try {
            // 1. Create a reference to the storage location (e.g., "profile_pictures/userId.jpg")
            val storageRef = storage.reference.child("profile_pictures/${uid}.jpg")

            // 2. Upload the file from the local URI
            storageRef.putFile(imageUri).await()

            // 3. Get the public download URL of the uploaded file
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // 4. Update the user's profile in Firestore with the new URL
            usersCollection.document(uid).update("profilePictureUrl", downloadUrl).await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // --- Gamification ---
    override suspend fun addAuraPoints(uid: String, pointsToAdd: Int): Result<Unit> {
        return try {
            usersCollection.document(uid).update("auraPoints", FieldValue.increment(pointsToAdd.toLong())).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val userDocRef = usersCollection.document(uid)
                val snapshot = transaction.get(userDocRef)
                val currentPoints = snapshot.getLong("auraPoints") ?: 0L
                if (currentPoints >= pointsToSpend) {
                    transaction.update(userDocRef, "auraPoints", FieldValue.increment(-pointsToSpend.toLong()))
                    null
                } else {
                    throw FirebaseFirestoreException("Insufficient points", FirebaseFirestoreException.Code.ABORTED)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- Friends System ---
    override suspend fun searchUsersByUsername(query: String): Result<List<User>> {
        return try {
            val snapshot = usersCollection.whereEqualTo("username", query).limit(10).get().await()
            Result.success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit> {
        return try {
            val request = FriendRequest(senderId = sender.uid, senderUsername = sender.username ?: "AuraTrackr User", receiverId = receiverId)
            usersCollection.document(receiverId).collection("friend_requests").add(request).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getFriendRequests(uid: String): Flow<List<FriendRequest>> = callbackFlow {
        val listener = usersCollection.document(uid).collection("friend_requests")
            .whereEqualTo("status", RequestStatus.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(FriendRequest::class.java) ?: emptyList()).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val senderDocRef = usersCollection.document(request.senderId)
                val receiverDocRef = usersCollection.document(request.receiverId)
                val requestDocRef = usersCollection.document(request.receiverId).collection("friend_requests").document(request.id)
                transaction.update(senderDocRef, "friends", FieldValue.arrayUnion(request.receiverId))
                transaction.update(receiverDocRef, "friends", FieldValue.arrayUnion(request.senderId))
                transaction.update(requestDocRef, "status", RequestStatus.ACCEPTED)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun declineFriendRequest(request: FriendRequest): Result<Unit> {
        return try {
            usersCollection.document(request.receiverId).collection("friend_requests")
                .document(request.id).update("status", RequestStatus.DECLINED).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getFriends(uid: String): Flow<List<User>> = callbackFlow {
        val userListener = usersCollection.document(uid).addSnapshotListener { userSnapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val friendIds = userSnapshot?.toObject(User::class.java)?.friends
            if (friendIds.isNullOrEmpty()) {
                trySend(emptyList()).isSuccess
            } else {
                val friendsListener = usersCollection.whereIn("uid", friendIds)
                    .addSnapshotListener { friendsSnapshot, friendsError ->
                        if (friendsError != null) { close(friendsError); return@addSnapshotListener }
                        trySend(friendsSnapshot?.toObjects(User::class.java) ?: emptyList()).isSuccess
                    }
                awaitClose { friendsListener.remove() }
            }
        }
        awaitClose { userListener.remove() }
    }

    // --- Aura Wrapped Summary ---
    override fun getUserSummary(uid: String, year: String): Flow<UserSummary?> = callbackFlow {
        val listener = usersCollection.document(uid).collection("summaries").document(year)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObject(UserSummary::class.java)).isSuccess
            }
        awaitClose { listener.remove() }
    }
}
