package com.example.auratrackr

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
import kotlin.test.assertEquals // ✅ FIX: Standardized on the kotlin.test assertion library.
import kotlin.test.assertFalse

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var themeRepository: ThemeRepository
    @Mock
    private lateinit var auth: FirebaseAuth
    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_uid")
    }


    @Test
    fun `fetchUserProfile when user exists updates state correctly`() = runTest {
        // Arrange
        val fakeUser = User(uid = "test_uid", username = "Test User", heightInCm = 180, weightInKg = 75)
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(fakeUser))
        whenever(themeRepository.getTheme()).thenReturn(flowOf(ThemeSetting.SYSTEM))

        // Act
        viewModel = SettingsViewModel(userRepository, themeRepository, auth)
        advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals("Test User", uiState.username)
        assertEquals("180 cm", uiState.height)
        assertEquals("75 kg", uiState.weight)
        assertFalse(uiState.isLoadingProfile)
    }

    @Test
    fun `onThemeSelected saves the new theme`() = runTest {
        // Arrange
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(User()))
        whenever(themeRepository.getTheme()).thenReturn(flowOf(ThemeSetting.SYSTEM))
        viewModel = SettingsViewModel(userRepository, themeRepository, auth)
        advanceUntilIdle()

        // Act
        viewModel.onThemeSelected(ThemeSetting.DARK)
        advanceUntilIdle()

        // Assert
        verify(themeRepository).setTheme(ThemeSetting.DARK)
    }

    @Test
    fun `fetchUserProfile when user is null emits error event`() = runTest {
        // Arrange
        whenever(userRepository.getUserProfile("test_uid")).thenReturn(flowOf(null))
        whenever(themeRepository.getTheme()).thenReturn(flowOf(ThemeSetting.SYSTEM))

        // Act & Assert
        viewModel = SettingsViewModel(userRepository, themeRepository, auth)

        viewModel.eventFlow.test {
            advanceUntilIdle()
            val event = awaitItem() as UiEvent.ShowSnackbar
            assertEquals("User profile not found.", event.message)
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.uiState.value.isLoadingProfile)
    }
}

