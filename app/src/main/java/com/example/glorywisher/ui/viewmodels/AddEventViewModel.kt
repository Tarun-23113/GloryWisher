package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.EventData
import com.example.glorywisher.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
        _addEventState.value = _addEventState.value.copy(title = title)
    }

    fun updateDate(date: String) {
        _addEventState.value = _addEventState.value.copy(date = date)
    }

    fun updateRecipient(recipient: String) {
        _addEventState.value = _addEventState.value.copy(recipient = recipient)
    }

    fun updateEventType(eventType: String) {
        _addEventState.value = _addEventState.value.copy(eventType = eventType)
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                setLoading()
                _addEventState.value = _addEventState.value.copy(isLoading = true)
                val event = repository.getEvent(eventId)
                event?.let {
                    _addEventState.value = _addEventState.value.copy(
                        title = it.title,
                        date = it.date,
                        recipient = it.recipient,
                        eventType = it.eventType,
                        isLoading = false
                    )
                    setSuccess(it)
                } ?: run {
                    setError("Event not found")
                    _addEventState.value = _addEventState.value.copy(
                        error = "Event not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load event")
                _addEventState.value = _addEventState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun saveEvent(eventId: String = "") {
        viewModelScope.launch {
            try {
                if (!validateInput()) {
                    return@launch
                }

                setLoading()
                _addEventState.value = _addEventState.value.copy(isLoading = true)

                val currentUser = repository.getCurrentUser()
                val userId = currentUser?.uid ?: throw Exception("User not authenticated")

                val event = EventData(
                    id = eventId.ifEmpty { UUID.randomUUID().toString() },
                    title = _addEventState.value.title,
                    date = _addEventState.value.date,
                    recipient = _addEventState.value.recipient,
                    eventType = _addEventState.value.eventType,
                    userId = userId
                )

                if (eventId.isEmpty()) {
                    repository.addEvent(event)
                } else {
                    repository.updateEvent(event)
                }

                setSuccess(event)
                _addEventState.value = _addEventState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to save event")
                _addEventState.value = _addEventState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun validateInput(): Boolean {
        val state = _addEventState.value
        val errors = mutableListOf<String>()

        if (state.title.isBlank()) {
            errors.add("Title is required")
        }
        if (state.date.isBlank()) {
            errors.add("Date is required")
        } else {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val eventDate = dateFormat.parse(state.date)
                if (eventDate == null || eventDate.before(Date())) {
                    errors.add("Please select a future date")
                }
            } catch (e: Exception) {
                errors.add("Invalid date format. Use DD/MM/YYYY")
            }
        }
        if (state.recipient.isBlank()) {
            errors.add("Recipient is required")
        }
        if (state.eventType.isBlank()) {
            errors.add("Event type is required")
        }

        if (errors.isNotEmpty()) {
            _addEventState.value = _addEventState.value.copy(
                error = errors.joinToString("\n")
            )
            return false
        }
        return true
    }

    fun resetState() {
        _addEventState.value = AddEventState()
    }
} 