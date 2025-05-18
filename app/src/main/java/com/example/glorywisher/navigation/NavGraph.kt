package com.example.glorywisher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.glorywisher.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }
        composable("home") {
            EventListScreen(navController)
        }
        composable(
            "add_event/{eventId}/{title}/{date}/{recipient}/{eventType}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("recipient") { type = NavType.StringType },
                navArgument("eventType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val title = backStackEntry.arguments?.getString("title")
            val date = backStackEntry.arguments?.getString("date")
            val recipient = backStackEntry.arguments?.getString("recipient")
            val eventType = backStackEntry.arguments?.getString("eventType")
            AddEventScreen(
                navController = navController,
                eventId = eventId,
                title = title,
                date = date,
                recipient = recipient,
                eventType = eventType
            )
        }
        composable(
            "flyer_preview/{eventId}/{title}/{date}/{recipient}/{eventType}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("recipient") { type = NavType.StringType },
                navArgument("eventType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val title = backStackEntry.arguments?.getString("title")
            val date = backStackEntry.arguments?.getString("date")
            val recipient = backStackEntry.arguments?.getString("recipient")
            val eventType = backStackEntry.arguments?.getString("eventType")
            FlyerPreviewScreen(
                navController = navController,
                eventId = eventId,
                title = title,
                date = date,
                recipient = recipient,
                eventType = eventType
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