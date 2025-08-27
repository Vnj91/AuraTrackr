package com.example.auratrackr.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.repository.ThemeRepository
import com.example.auratrackr.features.settings.ui.ThemeSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * A simple ViewModel responsible for providing the user's currently selected theme setting.
 * This ViewModel is used at the top level of the application (in MainActivity) to
 * determine which theme (Light, Dark, or System Default) to apply.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    themeRepository: ThemeRepository
) : ViewModel() {

    /**
     * A StateFlow that emits the current [ThemeSetting] preference of the user.
     * It observes the ThemeRepository and provides the latest value to the UI.
     */
    val themeSetting: StateFlow<ThemeSetting> = themeRepository.getThemeSetting()
        .stateIn(
            scope = viewModelScope,
            // The flow starts when a UI component subscribes and stops 5 seconds after the last subscriber disappears.
            started = SharingStarted.WhileSubscribed(5_000),
            // The initial value before the repository provides the saved one.
            initialValue = ThemeSetting.SYSTEM
        )
}
