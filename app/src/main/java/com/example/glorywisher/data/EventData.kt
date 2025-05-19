package com.example.glorywisher.data

import java.text.SimpleDateFormat
import java.util.*

data class EventData(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val recipient: String = "",
    val eventType: String = "",
    val userId: String = ""
) {
    companion object {
        private const val MAX_TITLE_LENGTH = 100
        private const val MAX_RECIPIENT_LENGTH = 100
        private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun validate(event: EventData): List<String> {
            val errors = mutableListOf<String>()

            if (event.title.isBlank()) {
                errors.add("Title is required")
            } else if (event.title.length > MAX_TITLE_LENGTH) {
                errors.add("Title must be less than $MAX_TITLE_LENGTH characters")
            }

            if (event.date.isBlank()) {
                errors.add("Date is required")
            } else {
                try {
                    val eventDate = DATE_FORMAT.parse(event.date)
                    if (eventDate == null || eventDate.before(Date())) {
                        errors.add("Please select a future date")
                    }
                } catch (e: Exception) {
                    errors.add("Invalid date format. Use DD/MM/YYYY")
                }
            }

            if (event.recipient.isBlank()) {
                errors.add("Recipient is required")
            } else if (event.recipient.length > MAX_RECIPIENT_LENGTH) {
                errors.add("Recipient name must be less than $MAX_RECIPIENT_LENGTH characters")
            }

            if (event.eventType.isBlank()) {
                errors.add("Event type is required")
            }

            if (event.userId.isBlank()) {
                errors.add("User ID is required")
            }

            return errors
        }

        fun formatDate(date: Date): String {
            return DATE_FORMAT.format(date)
        }

        fun parseDate(dateStr: String): Date? {
            return try {
                DATE_FORMAT.parse(dateStr)
            } catch (e: Exception) {
                null
            }
        }
    }
} 