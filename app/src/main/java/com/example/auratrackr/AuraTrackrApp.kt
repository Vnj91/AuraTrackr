package com.example.auratrackr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber // ✅ FIX: Added the import for Timber

/**
 * The main Application class for AuraTrackr.
 *
 * The @HiltAndroidApp annotation is essential. It triggers Hilt's code generation
 * and creates the dependency injection container that is attached to the application's
 * lifecycle. This class serves as the parent container for all other DI containers.
 */
@HiltAndroidApp
class AuraTrackrApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // This is the ideal place to initialize libraries that need to be available
        // throughout the entire application lifecycle.
        // Example: Initialize a logging library like Timber for debug builds.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
