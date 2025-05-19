package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.EventData
import com.example.glorywisher.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class EventListState(
    val events: List<EventData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val hasMoreEvents: Boolean = true,
    val lastDocumentId: String? = null
)

class EventListViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<List<EventData>>() {
    private val _eventListState = MutableStateFlow(EventListState())
    val eventListState: StateFlow<EventListState> = _eventListState.asStateFlow()
    private val PAGE_SIZE = 20

    init {
        loadEvents()
    }

    fun loadEvents(loadMore: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!loadMore) {
                    setLoading()
                    _eventListState.value = _eventListState.value.copy(isLoading = true)
                }
                
                val currentUser = repository.getCurrentUser()
                val userId = currentUser?.uid ?: throw Exception("User not authenticated")
                
                val result = repository.getEvents(
                    userId = userId,
                    lastDocumentId = if (loadMore) _eventListState.value.lastDocumentId else null,
                    pageSize = PAGE_SIZE
                )
                
                val newEvents = if (loadMore) {
                    _eventListState.value.events + result.events
                } else {
                    result.events
                }
                
                setSuccess(newEvents)
                _eventListState.value = _eventListState.value.copy(
                    events = newEvents,
                    isLoading = false,
                    hasMoreEvents = result.hasMore,
                    lastDocumentId = result.lastDocumentId
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

    fun loadMoreEvents() {
        if (!_eventListState.value.isLoading && _eventListState.value.hasMoreEvents) {
            loadEvents(loadMore = true)
        }
    }

    fun refreshEvents() {
        _eventListState.value = _eventListState.value.copy(
            lastDocumentId = null,
            hasMoreEvents = true
        )
        loadEvents()
    }

    fun updateSearchQuery(query: String) {
        _eventListState.value = _eventListState.value.copy(
            searchQuery = query,
            lastDocumentId = null,
            hasMoreEvents = true
        )
        filterEvents(query)
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

    private fun filterEvents(query: String) {
        viewModelScope.launch {
            try {
                setLoading()
                _eventListState.value = _eventListState.value.copy(isLoading = true)
                
                val currentUser = repository.getCurrentUser()
                val userId = currentUser?.uid ?: throw Exception("User not authenticated")
                
                // Get events with pagination
                val result = repository.getEvents(
                    userId = userId,
                    pageSize = PAGE_SIZE
                )
                
                // Apply search filter
                val filteredEvents = if (query.isEmpty()) {
                    result.events
                } else {
                    result.events.filter { event ->
                        event.title.contains(query, ignoreCase = true) ||
                        event.recipient.contains(query, ignoreCase = true) ||
                        event.eventType.contains(query, ignoreCase = true)
                    }
                }
                
                setSuccess(filteredEvents)
                _eventListState.value = _eventListState.value.copy(
                    events = filteredEvents,
                    isLoading = false,
                    hasMoreEvents = result.hasMore && filteredEvents.size == PAGE_SIZE,
                    lastDocumentId = if (filteredEvents.isNotEmpty()) result.lastDocumentId else null
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SecurityException -> "Authentication required"
                    is IOException -> "Network error. Please check your connection"
                    else -> "Failed to filter events: ${e.message}"
                }
                setError(errorMessage)
                _eventListState.value = _eventListState.value.copy(
                    error = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    // Add state restoration
    fun restoreState(savedState: EventListState) {
        _eventListState.value = savedState
    }

    // Add method to clear state
    fun clearState() {
        _eventListState.value = EventListState()
    }
} 