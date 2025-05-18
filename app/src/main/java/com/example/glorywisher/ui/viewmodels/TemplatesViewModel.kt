package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.FirestoreRepository
import com.example.glorywisher.ui.screens.Template
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class TemplatesState(
    val templates: List<Template> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTemplate: Template? = null
)

class TemplatesViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<List<Template>>() {
    private val _templatesState = MutableStateFlow(TemplatesState())
    val templatesState: StateFlow<TemplatesState> = _templatesState.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            try {
                setLoading()
                _templatesState.value = _templatesState.value.copy(isLoading = true)
                
                // For now, using hardcoded templates. In a real app, these would come from Firestore
                val templates = listOf(
                    Template(
                        id = "birthday",
                        title = "Birthday Celebration",
                        description = "Colorful birthday template with balloons",
                        backgroundColor = Color(0xFFFFE4E1),
                        textColor = Color(0xFF000000)
                    ),
                    Template(
                        id = "anniversary",
                        title = "Anniversary Special",
                        description = "Elegant anniversary design",
                        backgroundColor = Color(0xFFE6E6FA),
                        textColor = Color(0xFF000000)
                    ),
                    Template(
                        id = "graduation",
                        title = "Graduation Day",
                        description = "Academic achievement celebration",
                        backgroundColor = Color(0xFFF0FFF0),
                        textColor = Color(0xFF000000)
                    ),
                    Template(
                        id = "wedding",
                        title = "Wedding Invitation",
                        description = "Classic wedding design",
                        backgroundColor = Color(0xFFF5F5DC),
                        textColor = Color(0xFF000000)
                    )
                )
                
                setSuccess(templates)
                _templatesState.value = _templatesState.value.copy(
                    templates = templates,
                    isLoading = false
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load templates")
                _templatesState.value = _templatesState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun selectTemplate(template: Template) {
        _templatesState.value = _templatesState.value.copy(selectedTemplate = template)
    }

    fun clearSelection() {
        _templatesState.value = _templatesState.value.copy(selectedTemplate = null)
    }
} 