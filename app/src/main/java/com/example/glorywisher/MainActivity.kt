package com.example.glorywisher

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.glorywisher.navigation.NavGraph
import com.example.glorywisher.ui.theme.GloryWisherTheme
import com.example.glorywisher.ui.theme.ThemeState
import com.example.glorywisher.ui.theme.LocalThemeState
import com.example.glorywisher.ui.viewmodels.AuthViewModel
import com.example.glorywisher.ui.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeState = remember { ThemeState() }
            
            GloryWisherTheme(
                darkTheme = themeState.isDarkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel(
                        factory = ViewModelFactory.create(LocalContext.current)
                    )
                    val authState = authViewModel.authState.collectAsState().value
                    
                    // Handle initial navigation based on auth state
                    LaunchedEffect(authState.isAuthenticated) {
                        Log.d("MainActivity", "Auth state changed: isAuthenticated=${authState.isAuthenticated}")
                        if (authState.isAuthenticated) {
                            Log.d("MainActivity", "User is authenticated, navigating to home")
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Log.d("MainActivity", "User is not authenticated, navigating to login")
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                    
                    // Handle notification intent
                    val navigateTo = intent.getStringExtra("navigate_to")
                    val eventId = intent.getStringExtra("event_id")
                    val eventTitle = intent.getStringExtra("event_title")
                    val eventDate = intent.getStringExtra("event_date")
                    val eventRecipient = intent.getStringExtra("event_recipient")
                    val eventType = intent.getStringExtra("event_type")

                    if (navigateTo == "flyer_preview" && eventId != null) {
                        Log.d("MainActivity", "Handling notification intent: flyer_preview")
                        navController.navigate("flyer_preview/$eventId/$eventTitle/$eventDate/$eventRecipient/$eventType")
                    }

                    CompositionLocalProvider(LocalThemeState provides themeState) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}

