// This file is now the single source of truth for all plugin versions.

// 1. Configure where Gradle should look for plugins.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// 2. Define the versions for all plugins used in the project.
// ✅ THE DEFINITIVE FIX: These plugin versions are modern and compatible with your libraries.
plugins {
    id("com.android.application") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    // Code style and static analysis plugins
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1" apply false
}

// 3. Configure where Gradle should look for library dependencies.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 4. Define the project structure.
rootProject.name = "AuraTrackr"
include(":app")

