package com.example.gymtracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.WorkoutSession
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.components.ExerciseIcons
import com.example.gymtracker.ui.theme.BackgroundDark
import com.example.gymtracker.ui.theme.BorderDark
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark

@Composable
fun WorkoutRoadmapView(
    viewModel: GymViewModel,
    session: WorkoutSession,
    modifier: Modifier = Modifier
) {
    val workoutPlan by viewModel.workoutPlan.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()
    val sessionSets by viewModel.activeSessionSets.collectAsState()
    val workoutSeconds by viewModel.workoutSeconds.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val isWorkoutMinimized by viewModel.isWorkoutMinimized.collectAsState()

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var exerciseToReplace by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDeleteFromPlan by remember { mutableStateOf<Exercise?>(null) }

    fun formatSeconds(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    BackHandler {
        viewModel.minimizeWorkout()
    }

    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp)
        ) {
            // ШАПКА
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!isWorkoutMinimized) {
                            IconButton(
                                onClick = { viewModel.minimizeWorkout() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariantDark)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "В меню",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(
                                text = session.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Общая длительность ${formatSeconds(workoutSeconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Кнопка "+" добавить упражнение в сценарий
                    IconButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Добавить упражнение",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ПУСТОЙ ПЛАН
            if (workoutPlan.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "План тренировки пуст",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Нажми «+» вверху, чтобы добавить упражнения в сценарий.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddExerciseDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Выбрать упражнение")
                            }
                        }
                    }
                }
            } else {
                // СПИСОК УПРАЖНЕНИЙ БЕЗ БЛОКОВ ОТДЫХА
                itemsIndexed(workoutPlan, key = { _, ex -> ex.id }) { index, exercise ->
                    val setsForEx = sessionSets.filter { it.exerciseId == exercise.id }
                    val isCompleted = setsForEx.isNotEmpty()
                    val isActive = selectedExercise?.id == exercise.id

                    RoadmapExerciseItem(
                        exercise = exercise,
                        index = index,
                        totalCount = workoutPlan.size,
                        setsCount = setsForEx.size,
                        isCompleted = isCompleted,
                        isActive = isActive,
                        onClick = { viewModel.enterExerciseFocus(exercise) },
                        onMoveUp = { viewModel.moveExerciseUp(index) },
                        onMoveDown = { viewModel.moveExerciseDown(index) },
                        onReplace = { exerciseToReplace = exercise },
                        onDelete = { exerciseToDeleteFromPlan = exercise }
                    )

                    // Непрерывная соединительная линия между упражнениями
                    if (index < workoutPlan.size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(start = 21.dp)
                                .width(2.dp)
                                .height(16.dp)
                                .background(BorderDark)
                        )
                    }
                }
            }
        }

        // КНОПКА ЗАВЕРШИТЬ СНИЗУ
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = BackgroundDark
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                ) {
                    Text(
                        "Завершить тренировку",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ДИАЛОГ ВЫБОРА УПРАЖНЕНИЯ
        if (showAddExerciseDialog) {
            ExercisePickerModal(
                allExercises = allExercises,
                onDismiss = { showAddExerciseDialog = false },
                onSelect = { ex ->
                    viewModel.addExerciseToPlan(ex)
                    showAddExerciseDialog = false
                    if (workoutPlan.isEmpty()) {
                        viewModel.enterExerciseFocus(ex)
                    }
                }
            )
        }

        // ДИАЛОГ ЗАМЕНЫ УПРАЖНЕНИЯ
        if (exerciseToReplace != null) {
            ExercisePickerModal(
                allExercises = allExercises.filter { it.id != exerciseToReplace!!.id },
                onDismiss = { exerciseToReplace = null },
                onSelect = { newEx ->
                    viewModel.replaceExerciseInPlan(exerciseToReplace!!, newEx)
                    exerciseToReplace = null
                }
            )
        }

        // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ УПРАЖНЕНИЯ ИЗ ПЛАНА
        if (exerciseToDeleteFromPlan != null) {
            AlertDialog(
                onDismissRequest = { exerciseToDeleteFromPlan = null },
                title = { Text("Удалить из плана?") },
                text = { Text("Удалить «${exerciseToDeleteFromPlan!!.name}» из текущей тренировки?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeExerciseFromPlan(exerciseToDeleteFromPlan!!)
                            exerciseToDeleteFromPlan = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { exerciseToDeleteFromPlan = null }) { Text("Отмена") }
                }
            )
        }

        // ДИАЛОГ ЗАВЕРШЕНИЯ ТРЕНИРОВКИ
        if (showFinishDialog) {
            var finishNotes by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Завершить тренировку?", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Длительность: ${formatSeconds(workoutSeconds)}")
                        Text("Все подходы сохранены в историю.")
                        OutlinedTextField(
                            value = finishNotes,
                            onValueChange = { finishNotes = it },
                            label = { Text("Заметка к тренировке (опционально)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.finishWorkout(finishNotes)
                            showFinishDialog = false
                        }
                    ) {
                        Text("Завершить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) { Text("Продолжить") }
                }
            )
        }
    }
}

@Composable
private fun RoadmapExerciseItem(
    exercise: Exercise,
    index: Int,
    totalCount: Int,
    setsCount: Int,
    isCompleted: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(44.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) PrimaryNeon.copy(alpha = 0.2f) else SurfaceVariantDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ExerciseIcons.getIcon(exercise.icon),
                    contentDescription = null,
                    tint = if (isActive) PrimaryNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) PrimaryNeon else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (setsCount > 0) "Выполнено: $setsCount подходов" else "Без цели / Начать",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Выполнено",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // ПРЯМЫЕ КНОПКИ ПЕРЕМЕЩЕНИЯ (ВВЕРХ / ВНИЗ)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = index > 0,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Выше",
                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = index < totalCount - 1,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Ниже",
                                tint = if (index < totalCount - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Опции",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (index > 0) {
                                DropdownMenuItem(
                                    text = { Text("Переместить выше") },
                                    leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                                    onClick = {
                                        onMoveUp()
                                        showMenu = false
                                    }
                                )
                            }
                            if (index < totalCount - 1) {
                                DropdownMenuItem(
                                    text = { Text("Переместить ниже") },
                                    leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                                    onClick = {
                                        onMoveDown()
                                        showMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Заменить упражнение") },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, null) },
                                onClick = {
                                    onReplace()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить из плана") },
                                leadingIcon = { Icon(Icons.Default.Close, null) },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerModal(
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = allExercises.filter { it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбери упражнение") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск упражнения...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { ex ->
                        Card(
                            onClick = { onSelect(ex) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                        ) {
                            Text(
                                text = ex.name,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
