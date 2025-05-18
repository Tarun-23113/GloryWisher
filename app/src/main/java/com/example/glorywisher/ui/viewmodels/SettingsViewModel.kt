package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Settings(
    val notificationsEnabled: Boolean = true,
    val reminderTime: String = "3 hours before",
    val darkMode: Boolean = false,
    val language: String = "English"
)

data class SettingsState(
    val settings: Settings = Settings(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class SettingsViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<Settings>() {
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                setLoading()
                _settingsState.value = _settingsState.value.copy(isLoading = true)
                
                // In a real app, these would be loaded from SharedPreferences or Firestore
                val settings = Settings()
                setSuccess(settings)
                _settingsState.value = _settingsState.value.copy(
                    settings = settings,
                    isLoading = false
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load settings")
                _settingsState.value = _settingsState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                setLoading()
                _settingsState.value = _settingsState.value.copy(
                    settings = _settingsState.value.settings.copy(notificationsEnabled = enabled),
                    isLoading = true
                )
                // Save to Firestore or SharedPreferences
                setSuccess(_settingsState.value.settings)
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to update notifications")
                _settingsState.value = _settingsState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            try {
                setLoading()
                _settingsState.value = _settingsState.value.copy(
                    settings = _settingsState.value.settings.copy(reminderTime = time),
                    isLoading = true
                )
                // Save to Firestore or SharedPreferences
                setSuccess(_settingsState.value.settings)
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to update reminder time")
                _settingsState.value = _settingsState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                setLoading()
                _settingsState.value = _settingsState.value.copy(
                    settings = _settingsState.value.settings.copy(darkMode = enabled),
                    isLoading = true
                )
                // Save to Firestore or SharedPreferences
                setSuccess(_settingsState.value.settings)
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to update dark mode")
                _settingsState.value = _settingsState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            try {
                setLoading()
                _settingsState.value = _settingsState.value.copy(
                    settings = _settingsState.value.settings.copy(language = language),
                    isLoading = true
                )
                // Save to Firestore or SharedPreferences
                setSuccess(_settingsState.value.settings)
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to update language")
                _settingsState.value = _settingsState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
} 