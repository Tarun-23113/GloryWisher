package com.example.glorywisher.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glorywisher.ui.viewmodels.AuthViewModel
import com.example.glorywisher.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Handle authentication state changes
    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            Log.d("HomeScreen", "User not authenticated, navigating to login")
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Handle errors
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Log.e("HomeScreen", "Auth error: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Glory Wisher",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Settings button
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // Logout button
                    IconButton(
                        onClick = {
                            Log.d("HomeScreen", "Logout button clicked")
                            viewModel.signOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Message with Animation
            val infiniteTransition = rememberInfiniteTransition(label = "welcome")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "welcome_scale"
            )

            Text(
                "Welcome ${authState.user?.email?.split("@")?.first() ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .scale(scale)
            )
            
            Text(
                "Make someone's day special",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Quick Stats Section
            QuickStatsSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Cards with Animation
            val cards = listOf(
                NavigationItem(
                    title = "Create Event",
                    description = "Add a new celebration",
                    icon = Icons.Default.Add,
                    route = "add_event",
                    color = MaterialTheme.colorScheme.primary
                ),
                NavigationItem(
                    title = "My Events",
                    description = "View and manage your events",
                    icon = Icons.Default.Event,
                    route = "event_list",
                    color = MaterialTheme.colorScheme.secondary
                ),
                NavigationItem(
                    title = "Templates",
                    description = "Browse celebration templates",
                    icon = Icons.Default.Dashboard,
                    route = "templates",
                    color = MaterialTheme.colorScheme.tertiary
                )
            )

            cards.forEachIndexed { index, item ->
                AnimatedNavigationCard(
                    item = item,
                    onClick = { navController.navigate(item.route) },
                    delay = index * 100
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickStatsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Event,
                value = "5",
                label = "Upcoming"
            )
            StatItem(
                icon = Icons.Default.Celebration,
                value = "12",
                label = "Completed"
            )
            StatItem(
                icon = Icons.Default.Favorite,
                value = "8",
                label = "Favorites"
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AnimatedNavigationCard(
    item: NavigationItem,
    onClick: () -> Unit,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(300, delayMillis = delay),
        label = "card_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    NavigationCard(
        title = item.title,
        description = item.description,
        icon = item.icon,
        onClick = onClick,
        color = item.color,
        modifier = Modifier.scale(scale)
    )
}

data class NavigationItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun NavigationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "press_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .scale(scale),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}
