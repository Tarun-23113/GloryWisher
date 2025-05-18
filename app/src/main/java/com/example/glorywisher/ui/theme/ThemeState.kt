package com.example.glorywisher.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class ThemeState {
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun updateThemeMode(darkMode: Boolean) {
        isDarkMode = darkMode
    }
} 