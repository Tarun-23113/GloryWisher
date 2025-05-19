package com.example.glorywisher.ui.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.EventData
import com.example.glorywisher.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class AddEventState(
    val title: String = "",
    val date: String = "",
    val recipient: String = "",
    val eventType: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AddEventViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<EventData>() {
    private val _addEventState = MutableStateFlow(AddEventState())
    val addEventState: StateFlow<AddEventState> = _addEventState.asStateFlow()

    fun updateTitle(title: String) {
        Log.d("AddEventViewModel", "Updating title: $title")
        _addEventState.value = _addEventState.value.copy(title = title)
    }

    fun updateDate(date: String) {
        Log.d("AddEventViewModel", "Updating date: $date")
        _addEventState.value = _addEventState.value.copy(date = date)
    }

    fun updateRecipient(recipient: String) {
        Log.d("AddEventViewModel", "Updating recipient: $recipient")
        _addEventState.value = _addEventState.value.copy(recipient = recipient)
    }

    fun updateEventType(eventType: String) {
        Log.d("AddEventViewModel", "Updating event type: $eventType")
        _addEventState.value = _addEventState.value.copy(eventType = eventType)
    }

    fun loadEvent(eventId: String) {
        Log.d("AddEventViewModel", "Loading event with ID: $eventId")
        viewModelScope.launch {
            try {
                if (eventId.isBlank()) {
                    val error = "Invalid event ID"
                    Log.e("AddEventViewModel", error)
                    setError(error)
                    _addEventState.value = _addEventState.value.copy(
                        error = error,
                        isLoading = false
                    )
                    return@launch
                }

                setLoading()
                _addEventState.value = _addEventState.value.copy(isLoading = true)
                
                Log.d("AddEventViewModel", "Fetching event from repository")
                val event = repository.getEvent(eventId)
                
                event?.let {
                    Log.d("AddEventViewModel", "Event loaded successfully: ${it.title}")
                    _addEventState.value = _addEventState.value.copy(
                        title = it.title,
                        date = it.date,
                        recipient = it.recipient,
                        eventType = it.eventType,
                        isLoading = false
                    )
                    setSuccess(it)
                } ?: run {
                    val error = "Event not found"
                    Log.e("AddEventViewModel", error)
                    setError(error)
                    _addEventState.value = _addEventState.value.copy(
                        error = error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                val error = e.message ?: "Failed to load event"
                Log.e("AddEventViewModel", "Error loading event", e)
                setError(error)
                _addEventState.value = _addEventState.value.copy(
                    error = error,
                    isLoading = false
                )
            }
        }
    }

    fun saveEvent(eventId: String = "") {
        Log.d("AddEventViewModel", "Saving event with ID: $eventId")
        viewModelScope.launch {
            try {
                if (!validateInput()) {
                    Log.e("AddEventViewModel", "Input validation failed")
                    return@launch
                }

                setLoading()
                _addEventState.value = _addEventState.value.copy(
                    isLoading = true,
                    error = null
                )

                Log.d("AddEventViewModel", "Getting current user")
                val currentUser = repository.getCurrentUser()
                if (currentUser == null) {
                    val error = "User not authenticated"
                    Log.e("AddEventViewModel", error)
                    setError(error)
                    _addEventState.value = _addEventState.value.copy(
                        error = error,
                        isLoading = false
                    )
                    return@launch
                }

                val userId = currentUser.uid
                Log.d("AddEventViewModel", "Creating event for user: $userId")

                val event = EventData(
                    id = eventId.ifEmpty { UUID.randomUUID().toString() },
                    title = _addEventState.value.title,
                    date = _addEventState.value.date,
                    recipient = _addEventState.value.recipient,
                    eventType = _addEventState.value.eventType,
                    userId = userId
                )

                if (eventId.isEmpty()) {
                    Log.d("AddEventViewModel", "Adding new event: ${event.title}")
                    repository.addEvent(event)
                } else {
                    Log.d("AddEventViewModel", "Updating existing event: ${event.title}")
                    repository.updateEvent(event)
                }

                Log.d("AddEventViewModel", "Event saved successfully")
                setSuccess(event)
                _addEventState.value = _addEventState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                val error = e.message ?: "Failed to save event"
                Log.e("AddEventViewModel", "Error saving event", e)
                setError(error)
                _addEventState.value = _addEventState.value.copy(
                    error = error,
                    isLoading = false
                )
            }
        }
    }

    private fun validateInput(): Boolean {
        Log.d("AddEventViewModel", "Validating input")
        val state = _addEventState.value
        
        val event = EventData(
            title = state.title,
            date = state.date,
            recipient = state.recipient,
            eventType = state.eventType,
            userId = repository.getCurrentUser()?.uid ?: ""
        )
        
        val errors = EventData.validate(event)
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n")
            Log.e("AddEventViewModel", "Validation errors: $errorMessage")
            _addEventState.value = _addEventState.value.copy(
                error = errorMessage
            )
            return false
        }
        
        Log.d("AddEventViewModel", "Input validation successful")
        return true
    }

    fun resetState() {
        Log.d("AddEventViewModel", "Resetting state")
        _addEventState.value = AddEventState()
    }
} 