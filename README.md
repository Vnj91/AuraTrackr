AuraTrackr ✨
An all-in-one Android wellness companion for fitness, focus, and well-being, built with modern, native technologies.

🤔 Why AuraTrackr?
In a world of constant digital distractions, it's easy to lose the balance between our physical goals and our mental focus. I wanted to create a single, unified platform that doesn't just track your activities but actively gamifies the process of self-improvement. AuraTrackr is my answer—an all-in-one Android wellness companion designed to help you build your "aura" by rewarding you for both staying active and staying focused.

✨ Key Features
🏋️ Custom Workout & Activity Scheduling: Design your perfect week by building personalized schedules for the gym, studying, work, or home life.

🎯 Digital Focus Mode: Reclaim your time by setting daily usage limits for distracting apps. When your time is up, a task screen appears to help you get back on track.

🏆 Gamified Progress System: Earn "Aura Points" for completing workouts and staying within your app budgets. Watch your aura grow as you build consistent, healthy habits.

🤝 Social Leaderboards & Challenges: Connect with friends, see how you stack up on the weekly leaderboard, and create custom challenges to stay motivated together.

🎨 Polished & Personalized UI: A clean, modern interface built entirely with Jetpack Compose and Material 3, featuring dynamic themes, delightful animations, and a user-centric design.

🌙 Light & Dark Mode: A complete, professionally designed Material 3 theme that is fully accessible and looks great in any mode.

📸 Profile Customization: Upload a custom profile picture to personalize your account.

🛠️ Tech Stack & Architecture
This project was a deep dive into modern Android development practices, showcasing a professional and scalable setup.

UI: 100% Kotlin with Jetpack Compose & Material 3.

Architecture: MVVM (Model-View-ViewModel) with a clean, repository-pattern architecture.

Dependency Injection: Hilt for managing dependencies across the app.

Asynchronous Programming: Kotlin Coroutines & Flow for a fully reactive UI and efficient background tasks.

Backend: Firebase (Authentication, Firestore for real-time data, and Storage for user-uploaded content).

Local Storage: Room Database for structured data and Jetpack DataStore for simple key-value preferences.

Testing: A robust suite of unit tests using JUnit, Mockito, and the Turbine library to ensure logical correctness and reliability.

Image Loading: Coil for efficient and simple image loading.

Animations: A rich set of animations built with Compose's animation APIs and the Konfetti library for delightful user feedback.

🚀 Getting Started
To build and run this project yourself, you will need to set up your own Firebase backend.

Prerequisites
Android Studio (latest stable version)

A Google account for Firebase

Setup Instructions
Clone the Repository

git clone [https://github.com/Vnj91/AuraTrackr.git](https://github.com/Vnj91/AuraTrackr.git)

Create a Firebase Project

Go to the Firebase Console.

Click "Add project" and follow the on-screen instructions.

Register Your App

Inside your new Firebase project, add a new Android app.

The package name must be com.example.auratrackr.

You can skip the SHA-1 key for now.

Download Your Configuration File

After registering the app, Firebase will prompt you to download a google-services.json file.

Download this file and place it in the app/ directory of the project (E:\AuraTrackr\app\google-services.json).

(Note: The .gitignore file in this project is correctly configured to prevent this file from being uploaded to GitHub, keeping your keys secure.)

Enable Firebase Services

In the Firebase Console, go to the Authentication section and enable the "Email/Password" and "Anonymous" sign-in methods.

Go to the Firestore Database section, create a database, and start in "Test mode" for now.

Go to the Storage section, create a storage bucket, and update the security rules to allow reads and writes for authenticated users (a simple rule like allow read, write: if request.auth != null; is a good start).

Build and Run

Open the project in Android Studio.

Let Gradle sync the project.

Build and run the app on an emulator or a physical device.

This project has been a challenging but incredibly rewarding journey. Thank you for following along. I hope you enjoy exploring the code!
