package com.example.glorywisher.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.glorywisher.ui.screens.*
import com.example.glorywisher.ui.viewmodels.AuthViewModel
import com.example.glorywisher.ui.viewmodels.ViewModelFactory

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
    val authState by authViewModel.authState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }
        composable("home") {
            HomeScreen(navController, authViewModel)
        }
        composable("event_list") {
            EventListScreen(navController)
        }
        // Simple add_event route for new events
        composable("add_event") {
            AddEventScreen(navController = navController)
        }
        // Edit event route with parameters
        composable(
            "add_event/{eventId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            AddEventScreen(
                navController = navController,
                eventId = eventId
            )
        }
        composable(
            route = "flyer_preview/{eventId}?title={title}&date={date}&recipient={recipient}&eventType={eventType}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("recipient") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("eventType") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val title = backStackEntry.arguments?.getString("title")
            val date = backStackEntry.arguments?.getString("date")
            val recipient = backStackEntry.arguments?.getString("recipient")
            val eventType = backStackEntry.arguments?.getString("eventType")

            FlyerPreviewScreen(
                eventId = eventId,
                title = title,
                date = date,
                recipient = recipient,
                eventType = eventType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("templates") {
            TemplatesScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
    }
} 