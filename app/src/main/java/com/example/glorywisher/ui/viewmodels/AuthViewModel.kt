package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.FirestoreRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null
)

class AuthViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<FirebaseUser>() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                setLoading()
                val result = repository.signIn(email, password)
                setSuccess(result)
                _authState.value = AuthState(user = result)
            } catch (e: Exception) {
                setError(e.message ?: "Authentication failed")
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                setLoading()
                val result = repository.signUp(email, password, name)
                setSuccess(result)
                _authState.value = AuthState(user = result)
            } catch (e: Exception) {
                setError(e.message ?: "Registration failed")
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
                _authState.value = AuthState()
            } catch (e: Exception) {
                setError(e.message ?: "Sign out failed")
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = repository.getCurrentUser()
                if (user != null) {
                    setSuccess(user)
                    _authState.value = AuthState(user = user)
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to get current user")
            }
        }
    }
} 