// Root-level build.gradle.kts

plugins {
    // 🔼 Updated to AGP 8.9.0 (minimum required for androidx.health.connect 1.1.0-rc03)
    id("com.android.application") version "8.9.0" apply false
    id("com.android.library") version "8.9.0" apply false

    // ✅ Kotlin + Kapt (compatible with AGP 8.9.0)
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.24" apply false

    // ✅ Hilt, Google Services, and KSP
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
