package com.example.glorywisher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun DatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier
) {
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Year selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { selectedYear-- }
            ) {
                Text("<")
            }
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = { selectedYear++ }
            ) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Month selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { 
                    if (selectedMonth > 0) selectedMonth-- 
                    else {
                        selectedMonth = 11
                        selectedYear--
                    }
                }
            ) {
                Text("<")
            }
            Text(
                text = getMonthName(selectedMonth),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = { 
                    if (selectedMonth < 11) selectedMonth++ 
                    else {
                        selectedMonth = 0
                        selectedYear++
                    }
                }
            ) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { 
                    if (selectedDay > 1) selectedDay-- 
                    else {
                        selectedDay = getDaysInMonth(selectedYear, selectedMonth)
                        if (selectedMonth > 0) selectedMonth--
                        else {
                            selectedMonth = 11
                            selectedYear--
                        }
                    }
                }
            ) {
                Text("<")
            }
            Text(
                text = selectedDay.toString(),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = { 
                    val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)
                    if (selectedDay < daysInMonth) selectedDay++ 
                    else {
                        selectedDay = 1
                        if (selectedMonth < 11) selectedMonth++
                        else {
                            selectedMonth = 0
                            selectedYear++
                        }
                    }
                }
            ) {
                Text(">")
            }
        }
    }

    // Update the state when selection changes
    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay)
        state.selectedDateMillis = calendar.timeInMillis
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "January"
        1 -> "February"
        2 -> "March"
        3 -> "April"
        4 -> "May"
        5 -> "June"
        6 -> "July"
        7 -> "August"
        8 -> "September"
        9 -> "October"
        10 -> "November"
        11 -> "December"
        else -> ""
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

@Composable
fun rememberDatePickerState(): DatePickerState {
    return remember { DatePickerState() }
}

class DatePickerState {
    var selectedDateMillis: Long? by mutableStateOf(null)
} 