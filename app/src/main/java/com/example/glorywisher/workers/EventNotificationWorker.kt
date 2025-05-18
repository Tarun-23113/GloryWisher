package com.example.glorywisher.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.glorywisher.MainActivity
import com.example.glorywisher.R
import com.example.glorywisher.data.EventData

class EventNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val EVENT_ID = "event_id"
        const val EVENT_TITLE = "event_title"
        const val EVENT_DATE = "event_date"
        const val EVENT_RECIPIENT = "event_recipient"
        const val EVENT_TYPE = "event_type"
        const val CHANNEL_ID = "event_notifications"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(EVENT_ID) ?: return Result.failure()
        val eventTitle = inputData.getString(EVENT_TITLE) ?: return Result.failure()
        val eventDate = inputData.getString(EVENT_DATE) ?: return Result.failure()
        val eventRecipient = inputData.getString(EVENT_RECIPIENT) ?: return Result.failure()
        val eventType = inputData.getString(EVENT_TYPE) ?: return Result.failure()

        createNotificationChannel()
        showNotification(eventId, eventTitle, eventDate, eventRecipient, eventType)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Notifications"
            val descriptionText = "Notifications for upcoming events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        eventId: String,
        eventTitle: String,
        eventDate: String,
        eventRecipient: String,
        eventType: String
    ) {
        // Create intent for opening the flyer preview
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "flyer_preview")
            putExtra("event_id", eventId)
            putExtra("event_title", eventTitle)
            putExtra("event_date", eventDate)
            putExtra("event_recipient", eventRecipient)
            putExtra("event_type", eventType)
        }

        // Create unique request code for each notification
        val requestCode = eventId.hashCode()

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's Event: $eventTitle")
            .setContentText("It's $eventRecipient's $eventType today!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Don't forget! Today is $eventRecipient's $eventType. Tap to view and share the flyer."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()

        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, notification)
    }
} 