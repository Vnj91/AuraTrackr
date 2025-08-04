// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Main plugin for Android applications
    id("com.android.application") version "8.4.1" apply false

    // Main plugin for Kotlin language support
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false

    // Google's KSP (Kotlin Symbol Processing) plugin for faster annotation processing
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false

    // Hilt plugin for dependency injection
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    // Google Services plugin for Firebase integration
    id("com.google.gms.google-services") version "4.4.2" apply false
}