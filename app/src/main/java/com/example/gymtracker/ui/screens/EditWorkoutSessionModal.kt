package com.example.gymtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.components.AddSetDialog
import com.example.gymtracker.ui.components.EditSetDialog
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutSessionModal(
    session: WorkoutSession,
    viewModel: GymViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(session.title) }
    var notes by remember { mutableStateOf(session.notes) }
    var startTimeMs by remember { mutableLongStateOf(session.startTime) }
    var showDatePicker by remember { mutableStateOf(false) }

    var sessionSets by remember { mutableStateOf<List<WorkoutSet>>(emptyList()) }
    val allExercises by viewModel.exercises.collectAsState()
    val allVariations by viewModel.allVariations.collectAsState()

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseForNewSet by remember { mutableStateOf<Exercise?>(null) }
    var setForEdit by remember { mutableStateOf<WorkoutSet?>(null) }
    var setForDeletionConfirm by remember { mutableStateOf<WorkoutSet?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshSets() {
        coroutineScope.launch {
            sessionSets = viewModel.getSetsForHistorySession(session.id)
        }
    }

    LaunchedEffect(session.id) {
        sessionSets = viewModel.getSetsForHistorySession(session.id)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startTimeMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startTimeMs = it }
                    showDatePicker = false
                }) {
                    Text("OK")
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
        title = {
            Text("Редактирование тренировки", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дата: ${dateFormat.format(Date(startTimeMs))}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Изменить дату", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Общая заметка") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Упражнения и подходы:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddExerciseDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить упр.", fontSize = 11.sp)
                    }
                }

                // Список подходов по упражнениям
                LazyColumn(modifier = Modifier.weight(1f)) {
                    val grouped = sessionSets.groupBy { it.exerciseId }
                    if (grouped.isEmpty()) {
                        item {
                            Text(
                                "Подходов нет",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        grouped.forEach { (exId, sets) ->
                            val exName = allExercises.find { it.id == exId }?.name ?: "Упражнение"
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 13.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            allExercises.find { it.id == exId }?.let { exerciseForNewSet = it }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Добавить подход", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            items(sets) { s ->
                                val varName = s.variationId?.let { vId -> allVariations.find { it.id == vId }?.name }
                                Card(
                                    onClick = { setForEdit = s },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Сет ${s.setNumber}: ${if (s.weightKg % 1 == 0f) s.weightKg.toInt() else s.weightKg} кг × ${s.reps}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (!varName.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = PrimaryNeon.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = varName,
                                                            color = PrimaryNeon,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            if (s.intensityStatus.isNotBlank() || s.tags.isNotBlank()) {
                                                val tagsText = listOf(s.intensityStatus, s.tags).filter { it.isNotBlank() }.joinToString(" • ")
                                                Text(tagsText, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { setForEdit = s },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Редактировать подход",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { setForDeletionConfirm = s },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Удалить подход",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateSessionDetails(
                        session.id,
                        title.trim(),
                        notes.trim(),
                        startTimeMs,
                        session.endTime
                    )
                    onDismiss()
                }
            ) {
                Text("Сохранить всё")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    // ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ ПОДХОДА
    val activeSetForDeletion = setForDeletionConfirm
    if (activeSetForDeletion != null) {
        AlertDialog(
            onDismissRequest = { setForDeletionConfirm = null },
            title = { Text("Удалить подход?") },
            text = { Text("Точно удалить сет ${activeSetForDeletion.setNumber} (${activeSetForDeletion.weightKg} кг × ${activeSetForDeletion.reps})?") },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = activeSetForDeletion
                        setForDeletionConfirm = null
                        coroutineScope.launch {
                            viewModel.deleteSetSync(toDel)
                            refreshSets()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { setForDeletionConfirm = null }) { Text("Отмена") }
            }
        )
    }

    // РЕДАКТИРОВАНИЕ ПОДХОДА В ТРЕНИРОВКЕ
    val activeSetForEdit = setForEdit
    if (activeSetForEdit != null) {
        val exerciseVariations = allVariations.filter { it.exerciseId == activeSetForEdit.exerciseId }
        EditSetDialog(
            set = activeSetForEdit,
            variations = exerciseVariations,
            onDismiss = { setForEdit = null },
            onConfirm = { updated ->
                setForEdit = null
                coroutineScope.launch {
                    viewModel.updateSetSync(updated)
                    refreshSets()
                }
            }
        )
    }

    // ДОБАВЛЕНИЕ ПОДХОДА В РЕДАКТИРУЕМУЮ ТРЕНИРОВКУ
    val activeExerciseForNewSet = exerciseForNewSet
    if (activeExerciseForNewSet != null) {
        val targetExerciseId = activeExerciseForNewSet.id
        val exerciseVariations = allVariations.filter { it.exerciseId == targetExerciseId }
        AddSetDialog(
            initialWeight = 60f,
            initialReps = 10,
            variations = exerciseVariations,
            onDismiss = { exerciseForNewSet = null },
            onConfirm = { w, r, tags, intensity, sNotes, varId ->
                exerciseForNewSet = null
                coroutineScope.launch {
                    viewModel.addSetToHistorySession(
                        sessionId = session.id,
                        exerciseId = targetExerciseId,
                        weight = w,
                        reps = r,
                        tags = tags,
                        intensity = intensity,
                        notes = sNotes,
                        variationId = varId
                    )
                    refreshSets()
                }
            }
        )
    }

    // ВЫБОР УПРАЖНЕНИЯ ДЛЯ ДОБАВЛЕНИЯ В ТРЕНИРОВКУ
    if (showAddExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Выбери упражнение") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(allExercises) { ex ->
                        Card(
                            onClick = {
                                exerciseForNewSet = ex
                                showAddExerciseDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                        ) {
                            Text(ex.name, modifier = Modifier.padding(10.dp), fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) { Text("Отмена") }
            }
        )
    }
}
