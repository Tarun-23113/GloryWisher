package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.EventData
import com.example.glorywisher.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventListState(
    val events: List<EventData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class EventListViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<List<EventData>>() {
    private val _eventListState = MutableStateFlow(EventListState())
    val eventListState: StateFlow<EventListState> = _eventListState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                setLoading()
                _eventListState.value = _eventListState.value.copy(isLoading = true)
                
                val currentUser = repository.getCurrentUser()
                val userId = currentUser?.uid ?: throw Exception("User not authenticated")
                
                val events = repository.getEvents(userId)
                setSuccess(events)
                _eventListState.value = _eventListState.value.copy(
                    events = events,
                    isLoading = false
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load events")
                _eventListState.value = _eventListState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                setLoading()
                repository.deleteEvent(eventId)
                loadEvents() // Reload the list after deletion
            } catch (e: Exception) {
                setError(e.message ?: "Failed to delete event")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _eventListState.value = _eventListState.value.copy(searchQuery = query)
        filterEvents(query)
    }

    private fun filterEvents(query: String) {
        viewModelScope.launch {
            try {
                setLoading()
                val currentUser = repository.getCurrentUser()
                val userId = currentUser?.uid ?: throw Exception("User not authenticated")
                
                val allEvents = repository.getEvents(userId)
                val filteredEvents = if (query.isEmpty()) {
                    allEvents
                } else {
                    allEvents.filter { event ->
                        event.title.contains(query, ignoreCase = true) ||
                        event.recipient.contains(query, ignoreCase = true) ||
                        event.eventType.contains(query, ignoreCase = true)
                    }
                }
                setSuccess(filteredEvents)
                _eventListState.value = _eventListState.value.copy(
                    events = filteredEvents,
                    isLoading = false
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to filter events")
                _eventListState.value = _eventListState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
} 