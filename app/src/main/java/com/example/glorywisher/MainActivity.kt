package com.example.glorywisher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.glorywisher.navigation.NavGraph
import com.example.glorywisher.ui.theme.GloryWisherTheme
import com.example.glorywisher.ui.theme.ThemeState
import com.example.glorywisher.ui.theme.LocalThemeState

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
                    
                    // Handle notification intent
                    val navigateTo = intent.getStringExtra("navigate_to")
                    val eventId = intent.getStringExtra("event_id")
                    val eventTitle = intent.getStringExtra("event_title")
                    val eventDate = intent.getStringExtra("event_date")
                    val eventRecipient = intent.getStringExtra("event_recipient")
                    val eventType = intent.getStringExtra("event_type")

                    if (navigateTo == "flyer_preview" && eventId != null) {
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

