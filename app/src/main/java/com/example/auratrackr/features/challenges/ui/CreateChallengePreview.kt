package com.example.auratrackr.features.challenges.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.auratrackr.ui.theme.AuraTrackrTheme

@Preview(showBackground = true)
@Composable
fun CreateChallengeScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        CreateChallengeScreen(onBackClicked = {})
    }
}
