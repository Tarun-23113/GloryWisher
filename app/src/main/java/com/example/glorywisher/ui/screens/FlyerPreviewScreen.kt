package com.example.glorywisher.ui.screens

import android.graphics.Bitmap
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glorywisher.ui.viewmodels.FlyerPreviewState
import com.example.glorywisher.ui.viewmodels.FlyerPreviewViewModel
import com.example.glorywisher.ui.viewmodels.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import android.util.Log

@Composable
private fun FlyerPreviewContent(flyerState: FlyerPreviewState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
            .background(flyerState.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = flyerState.message,
                color = flyerState.textColor,
                fontFamily = flyerState.fontFamily,
                textAlign = TextAlign.Center
            )
            if (flyerState.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = flyerState.location,
                    color = flyerState.textColor,
                    fontFamily = flyerState.fontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    enabled: Boolean = true
) {
    val colors = listOf(
        Color.White,
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color)
                    .padding(2.dp)
                    .background(
                        if (color == selectedColor) Color.Gray
                        else Color.Transparent
                    )
                    .clickable(enabled = enabled) { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlyerPreviewScreen(
    eventId: String?,
    title: String?,
    date: String?,
    recipient: String?,
    eventType: String?,
    onNavigateBack: () -> Unit,
    viewModel: FlyerPreviewViewModel = viewModel(
        factory = ViewModelFactory.create(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val flyerState by viewModel.flyerState.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }

    // Load event data if eventId is provided
    LaunchedEffect(eventId) {
        if (eventId != null && eventId != "new") {
            Log.d("FlyerPreviewScreen", "Loading event data for ID: $eventId")
            viewModel.loadEventData(eventId)
        } else if (title != null) {
            // For new events or templates
            val message = buildString {
                if (title != null) append("$title\n")
                if (date != null) append("Date: $date\n")
                if (recipient != null) append("Recipient: $recipient\n")
                if (eventType != null) append("Event Type: $eventType")
            }
            if (message.isNotBlank()) {
                viewModel.updateMessage(message)
            }
        }
    }

    // Handle loading state
    if (flyerState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Handle errors
    LaunchedEffect(flyerState.error) {
        flyerState.error?.let { error ->
            Log.e("FlyerPreviewScreen", "Error: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Flyer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.generateFlyerImage(context) },
                        enabled = !flyerState.isLoading
                    ) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(
                        onClick = { viewModel.saveToGallery(context) },
                        enabled = !flyerState.isLoading
                    ) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Flyer Preview (pure Compose)
                FlyerPreviewContent(flyerState = flyerState)

                // Customization Controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Customize Flyer",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Message Input
                        OutlinedTextField(
                            value = flyerState.message,
                            onValueChange = { viewModel.updateMessage(it) },
                            label = { Text("Message") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !flyerState.isLoading
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Location Input
                        OutlinedTextField(
                            value = flyerState.location,
                            onValueChange = { viewModel.updateLocation(it) },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !flyerState.isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Color Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Background Color")
                                ColorPicker(
                                    selectedColor = flyerState.backgroundColor,
                                    onColorSelected = { viewModel.updateBackgroundColor(it) },
                                    enabled = !flyerState.isLoading
                                )
                            }
                            Column {
                                Text("Text Color")
                                ColorPicker(
                                    selectedColor = flyerState.textColor,
                                    onColorSelected = { viewModel.updateTextColor(it) },
                                    enabled = !flyerState.isLoading
                                )
                            }
                        }
                    }
                }
            }

            // Error Snackbar
            flyerState.error?.let { error ->
                if (!error.contains("Permission") && !error.contains("Storage")) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
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