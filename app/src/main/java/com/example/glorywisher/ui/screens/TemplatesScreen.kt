package com.example.glorywisher.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Template(
    val id: String,
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val textColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(navController: NavController) {
    val templates = remember {
        listOf(
            // Celebration Templates
            Template(
                id = "1",
                title = "Birthday",
                description = "Celebrate with joy and happiness",
                backgroundColor = Color(0xFFFFE4E1),
                textColor = Color(0xFF8B0000)
            ),
            Template(
                id = "2",
                title = "Anniversary",
                description = "Cherish your special moments",
                backgroundColor = Color(0xFFE6E6FA),
                textColor = Color(0xFF4B0082)
            ),
            Template(
                id = "3",
                title = "Wedding",
                description = "Begin your journey together",
                backgroundColor = Color(0xFFF0FFF0),
                textColor = Color(0xFF006400)
            ),
            Template(
                id = "4",
                title = "Graduation",
                description = "Celebrate your achievements",
                backgroundColor = Color(0xFFF5F5DC),
                textColor = Color(0xFF8B4513)
            ),
            // Professional Templates
            Template(
                id = "5",
                title = "Business",
                description = "Professional and elegant",
                backgroundColor = Color(0xFFF0F8FF),
                textColor = Color(0xFF000080)
            ),
            Template(
                id = "6",
                title = "Conference",
                description = "Formal and structured",
                backgroundColor = Color(0xFFFAF0E6),
                textColor = Color(0xFF2F4F4F)
            ),
            // Seasonal Templates
            Template(
                id = "7",
                title = "Christmas",
                description = "Festive holiday spirit",
                backgroundColor = Color(0xFFFFF0F5),
                textColor = Color(0xFF8B0000)
            ),
            Template(
                id = "8",
                title = "New Year",
                description = "Welcome new beginnings",
                backgroundColor = Color(0xFF000000),
                textColor = Color(0xFFFFD700)
            ),
            // Special Occasions
            Template(
                id = "9",
                title = "Baby Shower",
                description = "Welcome new life",
                backgroundColor = Color(0xFFE0FFFF),
                textColor = Color(0xFF4169E1)
            ),
            Template(
                id = "10",
                title = "House Warming",
                description = "Celebrate new home",
                backgroundColor = Color(0xFFF0E68C),
                textColor = Color(0xFF8B4513)
            ),
            // Cultural Templates
            Template(
                id = "11",
                title = "Diwali",
                description = "Festival of lights",
                backgroundColor = Color(0xFFFFA500),
                textColor = Color(0xFF800000)
            ),
            Template(
                id = "12",
                title = "Eid",
                description = "Blessed celebrations",
                backgroundColor = Color(0xFF98FB98),
                textColor = Color(0xFF006400)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onClick = {
                        // Navigate to flyer preview with template
                        navController.navigate("flyer_preview/new?template=${template.title}")
                    }
                )
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: Template,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = template.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = template.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = template.textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.description,
                fontSize = 14.sp,
                color = template.textColor.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
} 