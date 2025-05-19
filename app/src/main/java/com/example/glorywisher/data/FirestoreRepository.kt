package com.example.glorywisher.data

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class PaginatedResult<T>(
    val events: List<T>,
    val hasMore: Boolean,
    val lastDocumentId: String?
)

class FirestoreRepository(private val context: android.content.Context) {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")
    private val auth = FirebaseAuth.getInstance()

    private fun validateEvent(event: EventData): Boolean {
        return when {
            event.title.isBlank() -> {
                Log.e("Firestore", "Event validation failed: Title is empty")
                Toast.makeText(context, "Event title cannot be empty", Toast.LENGTH_LONG).show()
                false
            }
            event.date.isBlank() -> {
                Log.e("Firestore", "Event validation failed: Date is empty")
                Toast.makeText(context, "Event date cannot be empty", Toast.LENGTH_LONG).show()
                false
            }
            event.recipient.isBlank() -> {
                Log.e("Firestore", "Event validation failed: Recipient is empty")
                Toast.makeText(context, "Recipient name cannot be empty", Toast.LENGTH_LONG).show()
                false
            }
            event.eventType.isBlank() -> {
                Log.e("Firestore", "Event validation failed: Event type is empty")
                Toast.makeText(context, "Event type cannot be empty", Toast.LENGTH_LONG).show()
                false
            }
            event.userId.isBlank() -> {
                Log.e("Firestore", "Event validation failed: User ID is empty")
                Toast.makeText(context, "User ID is required", Toast.LENGTH_LONG).show()
                false
            }
            event.title.length > 100 -> {
                Log.e("Firestore", "Event validation failed: Title too long")
                Toast.makeText(context, "Title must be less than 100 characters", Toast.LENGTH_LONG).show()
                false
            }
            event.recipient.length > 100 -> {
                Log.e("Firestore", "Event validation failed: Recipient name too long")
                Toast.makeText(context, "Recipient name must be less than 100 characters", Toast.LENGTH_LONG).show()
                false
            }
            !isValidDate(event.date) -> {
                Log.e("Firestore", "Event validation failed: Invalid date format")
                Toast.makeText(context, "Invalid date format", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.isLenient = false
            formatter.parse(date)
            true
        } catch (e: Exception) {
            false
        }
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
        return try {
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
            Toast.makeText(context, "Error fetching events: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
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
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Log.d("FirebaseAuth", "Login success: ${user.email}")
                user
            } else {
                val error = "Authentication failed: No user returned"
                Log.e("FirebaseAuth", error)
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                throw Exception(error)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Login failed", e)
            Toast.makeText(context, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    suspend fun signUp(email: String, password: String, name: String): FirebaseUser {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Log.d("FirebaseAuth", "Registration success: ${user.email}")
                user
            } else {
                val error = "Registration failed: No user returned"
                Log.e("FirebaseAuth", error)
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                throw Exception(error)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Registration failed", e)
            Toast.makeText(context, "Registration error: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }
} 