package com.example.glorywisher.utils

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.UnknownHostException

sealed class AppError : Exception() {
    data class NetworkError(override val message: String) : AppError()
    data class AuthError(override val message: String) : AppError()
    data class DatabaseError(override val message: String) : AppError()
    data class StorageError(override val message: String) : AppError()
    data class ValidationError(override val message: String) : AppError()
    data class PermissionError(override val message: String) : AppError()
    data class UnknownError(override val message: String) : AppError()
}

object ErrorHandler {
    fun handle(throwable: Throwable): AppError {
        return when (throwable) {
            is FirebaseAuthException -> handleAuthError(throwable)
            is FirebaseFirestoreException -> handleDatabaseError(throwable)
            is IOException, is UnknownHostException -> AppError.NetworkError(
                "Network error: ${throwable.message ?: "Please check your internet connection"}"
            )
            is SecurityException -> AppError.PermissionError(
                "Permission denied: ${throwable.message ?: "Required permissions not granted"}"
            )
            is AppError -> throwable
            else -> AppError.UnknownError(
                throwable.message ?: "An unexpected error occurred"
            )
        }
    }

    private fun handleAuthError(error: FirebaseAuthException): AppError {
        return when (error.errorCode) {
            "ERROR_INVALID_EMAIL" -> AppError.ValidationError("Invalid email format")
            "ERROR_WRONG_PASSWORD" -> AppError.AuthError("Incorrect password")
            "ERROR_USER_NOT_FOUND" -> AppError.AuthError("User not found")
            "ERROR_EMAIL_ALREADY_IN_USE" -> AppError.AuthError("Email already in use")
            "ERROR_WEAK_PASSWORD" -> AppError.ValidationError("Password is too weak")
            "ERROR_USER_DISABLED" -> AppError.AuthError("Account has been disabled")
            "ERROR_TOO_MANY_REQUESTS" -> AppError.AuthError("Too many attempts. Please try again later")
            "ERROR_NETWORK_REQUEST_FAILED" -> AppError.NetworkError("Network connection failed")
            "ERROR_OPERATION_NOT_ALLOWED" -> AppError.AuthError("Operation not allowed")
            "ERROR_INVALID_CREDENTIAL" -> AppError.AuthError("Invalid credentials")
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> 
                AppError.AuthError("Account exists with different sign-in method")
            else -> AppError.AuthError(error.message ?: "Authentication failed")
        }
    }

    private fun handleDatabaseError(error: FirebaseFirestoreException): AppError {
        return when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                AppError.PermissionError("Permission denied to access the database")
            FirebaseFirestoreException.Code.NOT_FOUND -> 
                AppError.DatabaseError("Requested data not found")
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> 
                AppError.DatabaseError("Data already exists")
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> 
                AppError.DatabaseError("Database quota exceeded")
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> 
                AppError.DatabaseError("Operation cannot be executed in the current system state")
            FirebaseFirestoreException.Code.ABORTED -> 
                AppError.DatabaseError("Operation was aborted")
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> 
                AppError.DatabaseError("Operation was attempted past the valid range")
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> 
                AppError.DatabaseError("Operation is not implemented or not supported")
            FirebaseFirestoreException.Code.INTERNAL -> 
                AppError.DatabaseError("Internal error occurred")
            FirebaseFirestoreException.Code.UNAVAILABLE -> 
                AppError.DatabaseError("Service is currently unavailable")
            FirebaseFirestoreException.Code.DATA_LOSS -> 
                AppError.DatabaseError("Unrecoverable data loss or corruption")
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                AppError.AuthError("User is not authenticated")
            else -> AppError.DatabaseError(error.message ?: "Database operation failed")
        }
    }

    fun getErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> "Network Error: ${error.message}"
            is AppError.AuthError -> "Authentication Error: ${error.message}"
            is AppError.DatabaseError -> "Database Error: ${error.message}"
            is AppError.StorageError -> "Storage Error: ${error.message}"
            is AppError.ValidationError -> "Validation Error: ${error.message}"
            is AppError.PermissionError -> "Permission Error: ${error.message}"
            is AppError.UnknownError -> "Error: ${error.message}"
        }
    }
} 