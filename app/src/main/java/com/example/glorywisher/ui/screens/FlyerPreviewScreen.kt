package com.example.glorywisher.ui.screens

import android.graphics.Bitmap
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlyerPreviewScreen(
    navController: NavController,
    eventId: String? = null,
    title: String? = null,
    date: String? = null,
    recipient: String? = null,
    eventType: String? = null
) {
    var message by remember { mutableStateOf("Wishing you a wonderful $eventType!") }
    var location by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(true) }
    var selectedColor by remember { mutableStateOf(Color.White) }
    var selectedFontFamily by remember { mutableStateOf(FontFamily.Default) }
    val context = LocalContext.current
    var composeView by remember { mutableStateOf<ComposeView?>(null) }

    // Permission launcher for saving to gallery
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            saveFlyerToGallery(context, composeView)
        }
    }

    // Color palette
    val colorPalette = listOf(
        Color.White,
        Color(0xFFFFE4E1), // Misty Rose
        Color(0xFFE6E6FA), // Lavender
        Color(0xFFF0FFF0), // Honeydew
        Color(0xFFF5F5DC)  // Beige
    )

    // Font families
    val fontFamilies = listOf(
        FontFamily.Default to "Default",
        FontFamily.Serif to "Serif",
        FontFamily.SansSerif to "Sans Serif",
        FontFamily.Monospace to "Monospace"
    )

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flyer Preview") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            // Display event details
            Text(
                text = title ?: "No Title",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: ${date ?: "No Date"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Recipient: ${recipient ?: "No Recipient"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Event Type: ${eventType ?: "No Type"}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Edit/Preview toggle button
            IconButton(
                onClick = { isEditMode = !isEditMode },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.VisibilityOff else Icons.Default.Edit,
                    contentDescription = if (isEditMode) "Preview" else "Edit"
                )
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    // Color palette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorPalette.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = 2.dp,
                                        color = if (color == selectedColor) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    // Font selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        fontFamilies.forEach { (font, name) ->
                            Text(
                                text = name,
                                fontFamily = font,
                                modifier = Modifier
                                    .clickable { selectedFontFamily = font }
                                    .padding(8.dp)
                                    .background(
                                        if (font == selectedFontFamily) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Message field
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = selectedFontFamily
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    )

                    // Location field
                    TextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        label = { Text("Location") }
                    )
                }
            }

            // Flyer Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                            text = title ?: "No Title",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Date: ${date ?: "No Date"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recipient: ${recipient ?: "No Recipient"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Share Button
            Button(
                onClick = {
                    // TODO: Implement sharing functionality
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Share Flyer")
            }
        }
    }

    // Hidden ComposeView for capturing
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                setContent {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(selectedColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = message,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = selectedFontFamily,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.3f),
                                        blurRadius = 3f
                                    )
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Date: ${date ?: "No Date"}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            if (location.isNotBlank()) {
                                Text(
                                    text = "Location: $location",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            Text(
                                text = "Recipient: ${recipient ?: "No Recipient"}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }.also { composeView = it }
        },
        modifier = Modifier.size(0.dp)
    )
}

private fun saveFlyerToGallery(context: android.content.Context, composeView: ComposeView?) {
    composeView?.let { view ->
        try {
            val bitmap = captureComposableAsBitmap(view)
            val filename = "Flyer_${System.currentTimeMillis()}.png"
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Flyers")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }

                Toast.makeText(
                    context,
                    "Flyer saved to gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to save flyer: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }
}

private fun captureComposableAsBitmap(view: View): Bitmap {
    // Measure the view to get its dimensions
    view.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    
    // Layout the view with the measured dimensions
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    
    // Create a bitmap with the view's dimensions
    val bitmap = Bitmap.createBitmap(
        view.measuredWidth,
        view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    
    // Draw the view onto the bitmap's canvas
    view.draw(android.graphics.Canvas(bitmap))
    
    return bitmap
}

private fun shareImageViaWhatsApp(context: android.content.Context, bitmap: Bitmap) {
    try {
        // Create images directory in cache if it doesn't exist
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        
        // Create the image file
        val file = File(imagesFolder, "flyer_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { stream ->
            // Compress and save the bitmap
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
        }

        // Get the content URI using FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        // Create and launch the share intent
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(
                context,
                "WhatsApp is not installed on your device",
                Toast.LENGTH_LONG
            ).show()
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Failed to share image: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        e.printStackTrace()
    }
} 