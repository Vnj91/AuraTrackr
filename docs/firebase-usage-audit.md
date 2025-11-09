# Firebase Usage Audit

This file lists files that directly reference Firebase SDK components and recommended next steps to refactor to an `AuthRepository`/`UserRepository` abstraction.

Files referencing Firebase Auth / Firebase SDK (partial list from grep):

- `app/src/main/java/com/example/auratrackr/features/wrapped/viewmodel/WrappedViewModel.kt` — `FirebaseAuth` used directly
- `app/src/main/java/com/example/auratrackr/features/workout/viewmodel/WorkoutViewModel.kt` — `FirebaseAuth` used directly
- `app/src/main/java/com/example/auratrackr/features/settings/viewmodel/SettingsViewModel.kt` — `FirebaseAuth` used directly
- `app/src/main/java/com/example/auratrackr/features/schedule/viewmodel/ScheduleViewModel.kt` — `FirebaseAuth` used directly
- `app/src/main/java/com/example/auratrackr/features/schedule/viewmodel/ScheduleEditorViewModel.kt` — `FirebaseAuth` used directly
- `app/src/main/java/com/example/auratrackr/features/live/viewmodel/LiveActivityViewModel.kt` — `FirebaseAuth` used directly
- Tests referencing Firebase (ok to mock but note for test cleanup):
  - `app/src/test/java/.../AuthViewModelTest.kt`
  - `app/src/test/java/.../SettingsViewModelTest.kt`
  - `app/src/test/java/.../UserRepositoryImplTest.kt`

Recommended refactor plan (high-level):

1. Create `AuthRepository` interface exposing only required auth operations (currentUserId, signIn, signOut, observeAuthState).
2. Provide an implementation `FirebaseAuthRepository` that wraps `FirebaseAuth` and is injected using Hilt.
3. Update ViewModels to depend on `AuthRepository` instead of `FirebaseAuth` directly.
4. Update tests to mock `AuthRepository` instead of `FirebaseAuth`.
5. Repeat for other Firebase services (Firestore, Storage) with `UserRepository` / `StorageRepository` interfaces.

Notes:
- Avoid passing Android `Context` to repositories. Use `Application` only where absolutely needed and via `@ApplicationContext` injection.
- Add integration tests for the repository wrapper to verify interaction with the real Firebase SDK in a controlled test environment if needed.
