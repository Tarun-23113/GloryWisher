package com.example.glorywisher.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glorywisher.ui.viewmodels.AddEventViewModel
import com.example.glorywisher.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    navController: NavController,
    eventId: String? = null,
    title: String? = null,
    date: String? = null,
    recipient: String? = null,
    eventType: String? = null,
    viewModel: AddEventViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
) {
    val addEventState by viewModel.addEventState.collectAsState()
    val context = LocalContext.current

    Log.d("AddEventScreen", "Initializing screen with eventId: $eventId")

    LaunchedEffect(eventId) {
        if (eventId != null) {
            Log.d("AddEventScreen", "Loading event with ID: $eventId")
            viewModel.loadEvent(eventId)
        }
    }

    LaunchedEffect(addEventState.isSuccess) {
        if (addEventState.isSuccess) {
            Log.d("AddEventScreen", "Event saved successfully, navigating back")
            try {
                navController.popBackStack()
            } catch (e: Exception) {
                Log.e("AddEventScreen", "Navigation error", e)
                Toast.makeText(context, "Error navigating back: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(addEventState.error) {
        addEventState.error?.let { error ->
            Log.e("AddEventScreen", "Error state: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) "Add Event" else "Edit Event") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Log.d("AddEventScreen", "Back button clicked")
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("AddEventScreen", "Navigation error", e)
                                Toast.makeText(context, "Error navigating back: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = addEventState.title,
                onValueChange = { 
                    Log.d("AddEventScreen", "Title changed: $it")
                    viewModel.updateTitle(it)
                },
                label = { Text("Event Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = addEventState.date,
                onValueChange = { 
                    Log.d("AddEventScreen", "Date changed: $it")
                    viewModel.updateDate(it)
                },
                label = { Text("Date (DD/MM/YYYY)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = addEventState.recipient,
                onValueChange = { 
                    Log.d("AddEventScreen", "Recipient changed: $it")
                    viewModel.updateRecipient(it)
                },
                label = { Text("Recipient") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = addEventState.eventType,
                onValueChange = { 
                    Log.d("AddEventScreen", "Event type changed: $it")
                    viewModel.updateEventType(it)
                },
                label = { Text("Event Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (addEventState.isLoading) {
                Log.d("AddEventScreen", "Showing loading indicator")
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            addEventState.error?.let { error ->
                Log.e("AddEventScreen", "Displaying error: $error")
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    Log.d("AddEventScreen", "Save button clicked for eventId: $eventId")
                    try {
                        viewModel.saveEvent(eventId ?: "")
                    } catch (e: Exception) {
                        Log.e("AddEventScreen", "Error saving event", e)
                        Toast.makeText(context, "Error saving event: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(if (eventId == null) "Add Event" else "Update Event")
            }
        }
    }
} 