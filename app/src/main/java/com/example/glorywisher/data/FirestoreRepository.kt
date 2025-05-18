package com.example.glorywisher.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
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
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Authentication failed")
    }

    suspend fun signUp(email: String, password: String, name: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Registration failed")
    }

    suspend fun signOut() {
        auth.signOut()
    }
} 