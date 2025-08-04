package com.example.auratrackr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The main Application class for AuraTrackr.
 * The @HiltAndroidApp annotation is essential. It triggers Hilt's code generation
 * and creates the dependency injection container for the entire application.
 * This class serves as the entry point for Hilt.
 */
@HiltAndroidApp
class AuraTrackrApp : Application() {
    // This class can be empty. Its only job is to hold the annotation.
}
