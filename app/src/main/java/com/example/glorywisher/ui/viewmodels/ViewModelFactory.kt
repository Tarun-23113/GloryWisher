package com.example.glorywisher.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.glorywisher.data.FirestoreRepository

class ViewModelFactory(
    private val repository: FirestoreRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository) as T
            modelClass.isAssignableFrom(EventListViewModel::class.java) ->
                EventListViewModel(repository) as T
            modelClass.isAssignableFrom(AddEventViewModel::class.java) ->
                AddEventViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun create(context: android.content.Context): ViewModelFactory {
            val repository = FirestoreRepository()
            return ViewModelFactory(repository)
        }
    }
} 