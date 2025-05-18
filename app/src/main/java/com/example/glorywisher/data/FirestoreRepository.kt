package com.example.glorywisher.data

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val context: android.content.Context) {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")
    private val auth = FirebaseAuth.getInstance()

    suspend fun addEvent(event: EventData) {
        eventsCollection.document(event.id).set(event).await()
    }

    suspend fun getEvents(userId: String): List<EventData> {
        val snapshot = eventsCollection.whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(EventData::class.java) }
    }

    suspend fun updateEvent(event: EventData) {
        eventsCollection.document(event.id).set(event).await()
    }

    suspend fun deleteEvent(id: String) {
        eventsCollection.document(id).delete().await()
    }

    suspend fun getEvent(eventId: String): EventData? {
        return try {
            val document = eventsCollection.document(eventId).get().await()
            document.toObject(EventData::class.java)
        } catch (e: Exception) {
            null
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