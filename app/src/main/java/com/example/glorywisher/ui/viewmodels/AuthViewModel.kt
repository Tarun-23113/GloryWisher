package com.example.glorywisher.ui.viewmodels

import android.util.Log
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
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<FirebaseUser>() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        Log.d("AuthViewModel", "Checking authentication state")
        viewModelScope.launch {
            try {
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    Log.d("AuthViewModel", "User is already authenticated: ${currentUser.email}")
                    _authState.value = AuthState(
                        user = currentUser,
                        isAuthenticated = true
                    )
                } else {
                    Log.d("AuthViewModel", "No authenticated user found")
                    _authState.value = AuthState(isAuthenticated = false)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking auth state", e)
                _authState.value = AuthState(
                    error = e.message,
                    isAuthenticated = false
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        Log.d("AuthViewModel", "Attempting sign in for: $email")
        viewModelScope.launch {
            try {
                setLoading()
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                val result = repository.signIn(email, password)
                Log.d("AuthViewModel", "Sign in successful for: ${result.email}")
                
                _authState.value = AuthState(
                    user = result,
                    isAuthenticated = true
                )
                setSuccess(result)
            } catch (e: Exception) {
                val error = e.message ?: "Authentication failed"
                Log.e("AuthViewModel", "Sign in failed", e)
                _authState.value = AuthState(
                    error = error,
                    isAuthenticated = false
                )
                setError(error)
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        Log.d("AuthViewModel", "Attempting sign up for: $email")
        viewModelScope.launch {
            try {
                setLoading()
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                val result = repository.signUp(email, password, name)
                Log.d("AuthViewModel", "Sign up successful for: ${result.email}")
                
                _authState.value = AuthState(
                    user = result,
                    isAuthenticated = true
                )
                setSuccess(result)
            } catch (e: Exception) {
                val error = e.message ?: "Registration failed"
                Log.e("AuthViewModel", "Sign up failed", e)
                _authState.value = AuthState(
                    error = error,
                    isAuthenticated = false
                )
                setError(error)
            }
        }
    }

    fun signOut() {
        Log.d("AuthViewModel", "Signing out user")
        viewModelScope.launch {
            try {
                repository.signOut()
                Log.d("AuthViewModel", "Sign out successful")
                _authState.value = AuthState(isAuthenticated = false)
            } catch (e: Exception) {
                val error = e.message ?: "Sign out failed"
                Log.e("AuthViewModel", "Sign out failed", e)
                _authState.value = AuthState(
                    error = error,
                    isAuthenticated = true
                )
                setError(error)
            }
        }
    }

    override fun setError(error: String) {
        _authState.value = _authState.value.copy(
            error = error,
            isLoading = false
        )
    }
} 