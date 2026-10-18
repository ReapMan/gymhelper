package com.example.gymtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, startTime: Long, endTime: Long, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("Прошедшая тренировка") }
    var notes by remember { mutableStateOf("") }

    val oneDayMs = 24 * 60 * 60 * 1000L
    var chosenDateMs by remember { mutableLongStateOf(System.currentTimeMillis() - oneDayMs) }
    var selectedPreset by remember { mutableStateOf("Вчера") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = chosenDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        chosenDateMs = it
                        selectedPreset = "Другая"
                    }
                    showDatePicker = false
                }) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Записать прошедшую тренировку", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название (например: Ноги + Плечи)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Когда была тренировка?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPreset == "Сегодня",
                        onClick = {
                            selectedPreset = "Сегодня"
                            chosenDateMs = System.currentTimeMillis()
                        },
                        label = { Text("Сегодня") }
                    )
                    FilterChip(
                        selected = selectedPreset == "Вчера",
                        onClick = {
                            selectedPreset = "Вчера"
                            chosenDateMs = System.currentTimeMillis() - oneDayMs
                        },
                        label = { Text("Вчера") }
                    )
                    FilterChip(
                        selected = selectedPreset == "2 дня назад",
                        onClick = {
                            selectedPreset = "2 дня назад"
                            chosenDateMs = System.currentTimeMillis() - (2 * oneDayMs)
                        },
                        label = { Text("2 дня") }
                    )
                    FilterChip(
                        selected = selectedPreset == "Другая",
                        onClick = { showDatePicker = true },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Календарь") }
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                ) {
                    Text(
                        text = "Дата: ${dateFormat.format(Date(chosenDateMs))}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Общая заметка (по самочувствию и т.д.)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val start = chosenDateMs
                    val end = start + (60 * 60 * 1000L) // +1 час длительности
                    onConfirm(title.trim(), start, end, notes.trim())
                    onDismiss()
                }
            ) {
                Text("Создать тренировку")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
