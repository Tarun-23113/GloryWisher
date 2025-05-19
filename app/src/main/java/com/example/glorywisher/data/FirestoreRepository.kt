package com.example.glorywisher.data

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.IOException

data class PaginatedResult<T>(
    val events: List<T>,
    val hasMore: Boolean,
    val lastDocumentId: String?
)

class FirestoreRepository(private val context: android.content.Context) {
    private val db = FirebaseFirestore.getInstance().apply {
        // Enable offline persistence
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
    }
    private val eventsCollection = db.collection("events")
    private val auth = FirebaseAuth.getInstance()

    // Add retry logic
    private suspend fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw IllegalStateException("Should not reach here")
    }

    private fun validateEvent(event: EventData): Boolean {
        val errors = EventData.validate(event)
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n")
            Log.e("Firestore", "Event validation failed: $errorMessage")
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun validateUserAccess(userId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.uid != userId) {
            throw SecurityException("Unauthorized access")
        }
    }

    suspend fun addEvent(event: EventData) {
        try {
            validateUserAccess(event.userId)
            if (!validateEvent(event)) {
                throw IllegalArgumentException("Invalid event data")
            }

            Log.d("Firestore", "Adding event: ${event.title}")
            val result = eventsCollection.add(event).await()
            Log.d("Firestore", "Event created successfully with ID: ${result.id}")
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to create event", e)
            Toast.makeText(context, "Error creating event: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    suspend fun getEvents(
        userId: String,
        lastDocumentId: String? = null,
        pageSize: Int = 20
    ): PaginatedResult<EventData> {
        return withRetry {
            try {
                validateUserAccess(userId)
                Log.d("Firestore", "Fetching events for user: $userId")
                
                var query = eventsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())

                if (lastDocumentId != null) {
                    val lastDocument = eventsCollection.document(lastDocumentId).get().await()
                    query = query.startAfter(lastDocument)
                }

                val snapshot = query.get().await()
                val events = snapshot.documents.mapNotNull { it.toObject(EventData::class.java) }
                
                Log.d("Firestore", "Successfully fetched ${events.size} events")
                
                PaginatedResult(
                    events = events,
                    hasMore = events.size == pageSize,
                    lastDocumentId = if (events.isNotEmpty()) snapshot.documents.last().id else null
                )
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to fetch events", e)
                when (e) {
                    is FirebaseFirestoreException -> {
                        when (e.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                throw SecurityException("Permission denied to access events")
                            FirebaseFirestoreException.Code.UNAVAILABLE ->
                                throw IOException("Firestore is currently unavailable")
                            else -> throw e
                        }
                    }
                    else -> throw e
                }
            }
        }
    }

    suspend fun updateEvent(event: EventData) {
        try {
            validateUserAccess(event.userId)
            if (!validateEvent(event)) {
                throw IllegalArgumentException("Invalid event data")
            }

            Log.d("Firestore", "Updating event: ${event.id}")
            eventsCollection.document(event.id).set(event).await()
            Log.d("Firestore", "Event updated successfully")
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to update event", e)
            Toast.makeText(context, "Error updating event: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    suspend fun deleteEvent(id: String) {
        try {
            val event = getEvent(id) ?: throw IllegalArgumentException("Event not found")
            validateUserAccess(event.userId)

            Log.d("Firestore", "Deleting event: $id")
            eventsCollection.document(id).delete().await()
            Log.d("Firestore", "Event deleted successfully")
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to delete event", e)
            Toast.makeText(context, "Error deleting event: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    suspend fun getEvent(eventId: String): EventData? {
        return try {
            Log.d("Firestore", "Fetching event: $eventId")
            val document = eventsCollection.document(eventId).get().await()
            val event = document.toObject(EventData::class.java)
            if (event != null) {
                Log.d("Firestore", "Successfully fetched event: ${event.title}")
            } else {
                Log.w("Firestore", "Event not found: $eventId")
            }
            event
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch event", e)
            Toast.makeText(context, "Error fetching event: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signIn(email: String, password: String): FirebaseUser {
        return try {
            // Validate input
            if (email.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Email and password cannot be empty")
            }

            // Attempt sign in
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Verify user profile exists
                val userDoc = db.collection("users").document(user.uid).get().await()
                if (!userDoc.exists()) {
                    // If profile doesn't exist, sign out and throw error
                    auth.signOut()
                    throw Exception("User profile not found. Please sign up again.")
                }
                
                Log.d("FirebaseAuth", "Login success: ${user.email}")
                user
            } else {
                val error = "Authentication failed: No user returned"
                Log.e("FirebaseAuth", error)
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                throw Exception(error)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_INVALID_EMAIL" -> "Invalid email format"
                        "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                        "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                        "ERROR_USER_DISABLED" -> "This account has been disabled"
                        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
                        else -> "Login failed: ${e.message}"
                    }
                }
                is IllegalArgumentException -> e.message ?: "Invalid input"
                else -> "Login failed: ${e.message}"
            }
            Log.e("FirebaseAuth", "Login failed", e)
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            throw Exception(errorMessage)
        }
    }

    suspend fun signUp(email: String, password: String, name: String): FirebaseUser {
        return try {
            // Create user with email and password
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Create user profile in Firestore
                val userProfile = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                
                db.collection("users")
                    .document(user.uid)
                    .set(userProfile)
                    .await()
                
                Log.d("FirebaseAuth", "Registration success: ${user.email}")
                user
            } else {
                val error = "Registration failed: No user returned"
                Log.e("FirebaseAuth", error)
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                throw Exception(error)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                        "ERROR_INVALID_EMAIL" -> "Invalid email format"
                        "ERROR_WEAK_PASSWORD" -> "Password should be at least 6 characters"
                        else -> "Registration failed: ${e.message}"
                    }
                }
                else -> "Registration failed: ${e.message}"
            }
            Log.e("FirebaseAuth", "Registration failed", e)
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            throw Exception(errorMessage)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }
} 