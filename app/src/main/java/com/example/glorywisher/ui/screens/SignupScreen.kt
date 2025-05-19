package com.example.glorywisher.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glorywisher.ui.viewmodels.AuthViewModel
import com.example.glorywisher.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.create(LocalContext.current))
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            Log.d("SignupScreen", "Signup successful, navigating to home")
            try {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } catch (e: Exception) {
                Log.e("SignupScreen", "Navigation error", e)
                Toast.makeText(context, "Error navigating: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Log.e("SignupScreen", "Error state: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Log.d("SignupScreen", "Back button clicked")
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("SignupScreen", "Navigation error", e)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = name,
                onValueChange = { 
                    Log.d("SignupScreen", "Name changed: $it")
                    name = it
                },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = email,
                onValueChange = { 
                    Log.d("SignupScreen", "Email changed: $it")
                    email = it
                },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = password,
                onValueChange = { 
                    Log.d("SignupScreen", "Password changed")
                    password = it
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            TextField(
                value = confirmPassword,
                onValueChange = { 
                    Log.d("SignupScreen", "Confirm password changed")
                    confirmPassword = it
                },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (authState.isLoading) {
                Log.d("SignupScreen", "Showing loading indicator")
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }

            authState.error?.let { error ->
                Log.e("SignupScreen", "Displaying error: $error")
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    Log.d("SignupScreen", "Sign up button clicked")
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    try {
                        viewModel.signUp(email, password, name)
                    } catch (e: Exception) {
                        Log.e("SignupScreen", "Error during signup", e)
                        Toast.makeText(context, "Error during signup: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Sign Up")
            }
        }
    }
} 