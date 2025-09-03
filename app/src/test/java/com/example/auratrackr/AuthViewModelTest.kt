package com.example.auratrackr

import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
// ✅ The test now correctly imports the state classes from their new, separate file.
import com.example.auratrackr.features.onboarding.viewmodel.AuthNavigationTarget
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for the [AuthViewModel].
 * This class verifies the core authentication logic by mocking Firebase and repository dependencies.
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    // This rule ensures coroutines are executed predictably in tests.
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Mocks for all external dependencies of the ViewModel.
    @Mock
    private lateinit var auth: FirebaseAuth
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var viewModel: AuthViewModel
    // This will hold the listener passed to Firebase Auth so we can trigger it manually.
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    @Before
    fun setup() {
        // The setup block is now minimal. It only contains mocks that are truly
        // universal to every single test, like capturing the auth listener.
        whenever(auth.addAuthStateListener(any())).doAnswer { invocation ->
            authStateListener = invocation.arguments[0] as FirebaseAuth.AuthStateListener
            null
        }
    }

    @Test
    fun `when auth listener detects logged in user with completed profile, state navigates to dashboard`() = runTest {
        // Arrange: Set up all specific mocks needed JUST for this test.
        val existingUser = User(uid = "test_uid", hasCompletedOnboarding = true)
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(existingUser))
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_uid")

        // Act: Create the ViewModel, which adds the auth listener in its init block.
        viewModel = AuthViewModel(auth, userRepository)
        // Manually trigger the listener, simulating Firebase notifying the app of the logged-in user.
        authStateListener?.onAuthStateChanged(auth)
        advanceUntilIdle() // Ensure all coroutines triggered by the listener complete.

        // Assert: The final state should be a Success state pointing to the Dashboard.
        val finalState = viewModel.authState.value
        assertIs<AuthState.Success>(finalState)
        assertEquals(AuthNavigationTarget.GoToDashboard, finalState.navigationTarget)
    }

    @Test
    fun `when auth listener detects logged in user without profile, state navigates to onboarding`() = runTest {
        // Arrange: Mock the repository to return null, as if the user has no profile yet.
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(null))
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_uid")

        // Act
        viewModel = AuthViewModel(auth, userRepository)
        authStateListener?.onAuthStateChanged(auth)
        advanceUntilIdle()

        // Assert: The final state should be a Success state pointing to the Fitness Onboarding screen.
        val finalState = viewModel.authState.value
        assertIs<AuthState.Success>(finalState)
        assertEquals(AuthNavigationTarget.GoToFitnessOnboarding, finalState.navigationTarget)
    }

    @Test
    fun `when auth listener detects no user, state navigates to login`() = runTest {
        // Arrange: Mock that FirebaseAuth has no current user.
        whenever(auth.currentUser).thenReturn(null)

        // Act
        viewModel = AuthViewModel(auth, userRepository)
        authStateListener?.onAuthStateChanged(auth)
        advanceUntilIdle()

        // Assert: The final state should be a Success state pointing to the Login screen.
        val finalState = viewModel.authState.value
        assertIs<AuthState.Success>(finalState)
        assertEquals(AuthNavigationTarget.GoToLogin, finalState.navigationTarget)
    }
}

