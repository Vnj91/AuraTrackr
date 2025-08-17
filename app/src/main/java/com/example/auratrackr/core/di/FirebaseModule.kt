package com.example.auratrackr.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module responsible for providing singleton instances of Firebase services.
 * This ensures that components throughout the app can access a single, shared
 * instance of Firebase Authentication, Firestore, and Storage.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provides a singleton instance of [FirebaseAuth].
     * @return The default FirebaseAuth instance.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Provides a singleton instance of [FirebaseFirestore].
     * This implementation includes an example of how to apply custom settings,
     * such as enabling offline persistence, which is crucial for a good user experience.
     *
     * @return A configured FirebaseFirestore instance.
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        // Example: Configure Firestore settings. Enabling persistence allows the app
        // to work offline by caching data locally.
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        firestore.firestoreSettings = settings
        return firestore
    }

    /**
     * Provides a singleton instance of [FirebaseStorage].
     * @return The default FirebaseStorage instance.
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}