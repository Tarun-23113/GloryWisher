package com.example.glorywisher.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewModelScope
import com.example.glorywisher.data.FirestoreRepository
import com.example.glorywisher.utils.AppError
import com.example.glorywisher.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import java.lang.ref.WeakReference

data class FlyerPreviewState(
    val message: String = "",
    val location: String = "",
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val fontFamily: FontFamily = FontFamily.Default,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shareUri: Uri? = null
)

class FlyerPreviewViewModel(
    private val repository: FirestoreRepository
) : BaseViewModel<Uri>() {
    private val _flyerState = MutableStateFlow(FlyerPreviewState())
    val flyerState: StateFlow<FlyerPreviewState> = _flyerState.asStateFlow()
    private var currentViewRef: WeakReference<View>? = null

    fun loadEventData(eventId: String) {
        viewModelScope.launch {
            try {
                _flyerState.value = _flyerState.value.copy(isLoading = true, error = null)
                Log.d("FlyerPreviewViewModel", "Loading event data for ID: $eventId")
                
                val event = repository.getEvent(eventId)
                if (event != null) {
                    val message = buildString {
                        append("${event.title}\n")
                        append("Date: ${event.date}\n")
                        append("Recipient: ${event.recipient}\n")
                        append("Event Type: ${event.eventType}")
                    }
                    _flyerState.value = _flyerState.value.copy(
                        message = message,
                        isLoading = false
                    )
                    Log.d("FlyerPreviewViewModel", "Event data loaded successfully")
                } else {
                    _flyerState.value = _flyerState.value.copy(
                        error = "Event not found",
                        isLoading = false
                    )
                    Log.e("FlyerPreviewViewModel", "Event not found: $eventId")
                }
            } catch (e: Exception) {
                Log.e("FlyerPreviewViewModel", "Error loading event data", e)
                _flyerState.value = _flyerState.value.copy(
                    error = "Error loading event: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updateMessage(message: String) {
        _flyerState.value = _flyerState.value.copy(message = message)
    }

    fun updateLocation(location: String) {
        _flyerState.value = _flyerState.value.copy(location = location)
    }

    fun updateBackgroundColor(color: Color) {
        _flyerState.value = _flyerState.value.copy(backgroundColor = color)
    }

    fun updateTextColor(color: Color) {
        _flyerState.value = _flyerState.value.copy(textColor = color)
    }

    fun updateFontFamily(fontFamily: FontFamily) {
        _flyerState.value = _flyerState.value.copy(fontFamily = fontFamily)
    }

    fun setView(view: View) {
        currentViewRef = WeakReference(view)
    }

    private fun getCurrentView(): View? {
        return currentViewRef?.get()
    }

    fun clearView() {
        currentViewRef?.clear()
        currentViewRef = null
    }

    override fun onCleared() {
        super.onCleared()
        clearView()
    }

    fun generateFlyerImage(context: Context) {
        viewModelScope.launch {
            try {
                setLoading()
                _flyerState.value = _flyerState.value.copy(isLoading = true)

                val view = getCurrentView() ?: throw AppError.ValidationError("View not initialized")
                if (view.width <= 0 || view.height <= 0) {
                    throw AppError.ValidationError("Invalid view dimensions")
                }

                // Create bitmap from view
                view.isDrawingCacheEnabled = true
                val bitmap = Bitmap.createBitmap(view.drawingCache)
                view.isDrawingCacheEnabled = false

                if (bitmap == null) {
                    throw AppError.ValidationError("Failed to create bitmap from view")
                }

                // Save bitmap to file
                val file = File(context.cacheDir, "flyer_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                if (!file.exists() || file.length() == 0L) {
                    throw AppError.ValidationError("Failed to save flyer image")
                }

                // Create URI for sharing
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                setSuccess(uri)
                _flyerState.value = _flyerState.value.copy(
                    shareUri = uri,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                val appError = ErrorHandler.handle(e)
                _flyerState.value = _flyerState.value.copy(
                    error = ErrorHandler.getErrorMessage(appError),
                    isLoading = false
                )
            }
        }
    }

    fun shareFlyer(context: Context) {
        viewModelScope.launch {
            try {
                setLoading()
                _flyerState.value = _flyerState.value.copy(isLoading = true)

                val shareUri = _flyerState.value.shareUri
                    ?: throw AppError.ValidationError("No flyer image available to share")

                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, shareUri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    android.content.Intent.createChooser(shareIntent, "Share Flyer")
                )

                _flyerState.value = _flyerState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                val appError = ErrorHandler.handle(e)
                _flyerState.value = _flyerState.value.copy(
                    error = ErrorHandler.getErrorMessage(appError),
                    isLoading = false
                )
            }
        }
    }

    fun saveToGallery(context: Context) {
        viewModelScope.launch {
            try {
                setLoading()
                _flyerState.value = _flyerState.value.copy(isLoading = true)

                val view = getCurrentView() ?: throw AppError.ValidationError("View not initialized")
                val shareUri = _flyerState.value.shareUri
                    ?: throw AppError.ValidationError("No flyer image available to save")

                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "flyer_${System.currentTimeMillis()}.png")
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GloryWisher")
                        put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = context.contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw AppError.StorageError("Failed to create media entry")

                uri.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        view.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(view.drawingCache)
                            ?: throw AppError.StorageError("Failed to create bitmap")
                        view.isDrawingCacheEnabled = false
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                }

                _flyerState.value = _flyerState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                val appError = ErrorHandler.handle(e)
                _flyerState.value = _flyerState.value.copy(
                    error = ErrorHandler.getErrorMessage(appError),
                    isLoading = false
                )
            }
        }
    }
} 