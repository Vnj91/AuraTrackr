package com.example.auratrackr

// This import is crucial for testing Flows with Turbine.
import app.cash.turbine.test
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.ThemeRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.features.settings.ui.ThemeSetting
import com.example.auratrackr.features.settings.viewmodel.SettingsViewModel
import com.example.auratrackr.features.settings.viewmodel.UiEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Unit tests for the [SettingsViewModel].
 * This class follows modern Android testing practices using Mockito for mocking,
 * a custom JUnit rule for managing CoroutineDispatchers, and Turbine for testing Flows.
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest {

    // This rule swaps the main dispatcher with a test dispatcher to allow for control over coroutines.
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    // Mocks for the ViewModel's dependencies.
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var themeRepository: ThemeRepository
    @Mock
    private lateinit var auth: FirebaseAuth
    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var viewModel: SettingsViewModel

    /**
     * Sets up the common test conditions before each test case is run.
     * Here, we mock the FirebaseAuth instance to return a dummy user.
     */
    @Before
    fun setup() {
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_uid")
    }

    @Test
    fun `fetchUserProfile when user exists updates state correctly`() = runTest {
        // Arrange: Define the mock data and behavior.
        val fakeUser = User(uid = "test_uid", username = "Test User", heightInCm = 180, weightInKg = 75, hasCompletedOnboarding = true)
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(fakeUser))
        // The test now calls the correct function name from the stable repository.
        whenever(themeRepository.getThemeSetting()).thenReturn(flowOf(ThemeSetting.SYSTEM))

        // Act: Create the ViewModel, which will trigger the logic in its init block.
        viewModel = SettingsViewModel(userRepository, themeRepository, auth)
        // This ensures all coroutines launched in the init block are completed before we assert.
        advanceUntilIdle()

        // Assert: Verify that the UI state was updated correctly.
        val uiState = viewModel.uiState.value
        assertEquals("Test User", uiState.username)
        assertEquals("180 cm", uiState.height)
        assertEquals("75 kg", uiState.weight)
        assertEquals(ThemeSetting.SYSTEM, uiState.themeSetting)
        assertFalse(uiState.isLoadingProfile)
    }

    @Test
    fun `onThemeSelected saves the new theme`() = runTest {
        // Arrange
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(User()))
        whenever(themeRepository.getThemeSetting()).thenReturn(flowOf(ThemeSetting.SYSTEM))
        viewModel = SettingsViewModel(userRepository, themeRepository, auth)
        advanceUntilIdle()

        // Act: Call the function we want to test.
        viewModel.onThemeSelected(ThemeSetting.DARK)
        advanceUntilIdle() // Ensure the save coroutine completes.

        // Assert: Verify that the correct method was called on our mock repository.
        verify(themeRepository).setThemeSetting(ThemeSetting.DARK)
    }

    @Test
    fun `fetchUserProfile when user is null emits error event`() = runTest {
        // Arrange
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(null))
        whenever(themeRepository.getThemeSetting()).thenReturn(flowOf(ThemeSetting.SYSTEM))

        // Act
        viewModel = SettingsViewModel(userRepository, themeRepository, auth)

        // Assert: Use Turbine to safely test the event Flow.
        viewModel.eventFlow.test {
            advanceUntilIdle() // Let the init block run and emit the event.
            val event = awaitItem() as UiEvent.ShowSnackbar
            assertEquals("User profile not found.", event.message)
            cancelAndIgnoreRemainingEvents() // Ensure no other events are emitted.
        }
        // Also assert the final state of the UI.
        assertFalse(viewModel.uiState.value.isLoadingProfile)
    }
}

