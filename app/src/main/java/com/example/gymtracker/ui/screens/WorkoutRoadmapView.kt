package com.example.gymtracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Edit
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
import com.example.gymtracker.ui.components.ExercisePickerModal
import com.example.gymtracker.ui.i18n.LocalAppStrings
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
    val strings = LocalAppStrings.current

    val workoutPlan by viewModel.workoutPlan.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()
    val sessionSets by viewModel.activeSessionSets.collectAsState()
    val workoutSeconds by viewModel.workoutSeconds.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val isWorkoutMinimized by viewModel.isWorkoutMinimized.collectAsState()

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
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
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
        ) {
            // ШАПКА ТРЕНИРОВКИ
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
                        IconButton(
                            onClick = { viewModel.minimizeWorkout() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Свернуть тренировку",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        // Название тренировки + возможность быстро переименовать
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRenameDialog = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.title.ifBlank { strings.defaultWorkoutTitle },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = strings.renameWorkout,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${strings.totalDuration} ${formatSeconds(workoutSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Кнопка "+" для добавления упражнения
                    IconButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = strings.addExercise,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = strings.workoutPlanEmpty,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.workoutPlanEmptyDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { showAddExerciseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    strings.selectExercise,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // СПИСОК УПРАЖНЕНИЙ В РОАДМАПЕ
                itemsIndexed(workoutPlan, key = { _, ex -> ex.id }) { index, exercise ->
                    val setsForEx = sessionSets.filter { it.exerciseId == exercise.id }
                    val isCompleted = setsForEx.isNotEmpty() && setsForEx.all { it.isCompleted }
                    val isActive = selectedExercise?.id == exercise.id

                    RoadmapExerciseItem(
                        index = index,
                        totalCount = workoutPlan.size,
                        exercise = exercise,
                        setsCount = setsForEx.size,
                        isCompleted = isCompleted,
                        isActive = isActive,
                        onClick = { viewModel.enterExerciseFocus(exercise) },
                        onMoveUp = { viewModel.moveExerciseUp(index) },
                        onMoveDown = { viewModel.moveExerciseDown(index) },
                        onReplace = { exerciseToReplace = exercise },
                        onDelete = { exerciseToDeleteFromPlan = exercise }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // ЗАКРЕПЛЕННАЯ НИЖНЯЯ КНОПКА «ЗАВЕРШИТЬ ТРЕНИРОВКУ»
        // ВАЖНО ДЛЯ ASUS ROG И 3 КНОПОК: navigationBarsPadding() поднимает кнопку над системными кнопками
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = BackgroundDark
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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
                        strings.finishWorkout,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ДИАЛОГ ПЕРЕИМЕНОВАНИЯ ТРЕНИРОВКИ
        if (showRenameDialog) {
            var newTitle by remember { mutableStateOf(session.title) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text(strings.renameWorkout, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(strings.renameWorkoutPlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.renameActiveSession(newTitle)
                            showRenameDialog = false
                        }
                    ) {
                        Text(strings.saveBtn)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text(strings.cancelBtn)
                    }
                }
            )
        }

        // СОВРЕМЕННЫЙ ВЫБОР УПРАЖНЕНИЙ (МОДАЛЬНЫЙ ЭКРАН С ГРУППАМИ, ЧИПАМИ И МУЛЬТИВЫБОРОМ)
        if (showAddExerciseDialog) {
            ExercisePickerModal(
                allExercises = allExercises,
                muscleGroups = muscleGroups,
                onDismiss = { showAddExerciseDialog = false },
                onSelectExercises = { selectedList ->
                    viewModel.addExercisesToPlan(selectedList)
                    showAddExerciseDialog = false
                    if (workoutPlan.isEmpty() && selectedList.isNotEmpty()) {
                        viewModel.enterExerciseFocus(selectedList.first())
                    }
                },
                onCreateCustomExercise = { name, mgId ->
                    viewModel.createCustomExercise(name, mgId) { created ->
                        viewModel.addExerciseToPlan(created)
                        if (workoutPlan.isEmpty()) {
                            viewModel.enterExerciseFocus(created)
                        }
                    }
                }
            )
        }

        // ДИАЛОГ ЗАМЕНЫ УПРАЖНЕНИЯ
        if (exerciseToReplace != null) {
            ExercisePickerModal(
                allExercises = allExercises.filter { it.id != exerciseToReplace!!.id },
                muscleGroups = muscleGroups,
                onDismiss = { exerciseToReplace = null },
                onSelectExercises = { selectedList ->
                    if (selectedList.isNotEmpty()) {
                        viewModel.replaceExerciseInPlan(exerciseToReplace!!, selectedList.first())
                    }
                    exerciseToReplace = null
                }
            )
        }

        // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ ИЗ ПЛАНА
        if (exerciseToDeleteFromPlan != null) {
            AlertDialog(
                onDismissRequest = { exerciseToDeleteFromPlan = null },
                title = { Text(strings.deleteExerciseConfirmTitle) },
                text = { Text(strings.deleteExerciseConfirmMsg.format(exerciseToDeleteFromPlan!!.name)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeExerciseFromPlan(exerciseToDeleteFromPlan!!)
                            exerciseToDeleteFromPlan = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(strings.deleteBtn)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { exerciseToDeleteFromPlan = null }) {
                        Text(strings.cancelBtn)
                    }
                }
            )
        }

        // ДИАЛОГ ЗАВЕРШЕНИЯ ТРЕНИРОВКИ
        if (showFinishDialog) {
            var finishNotes by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text(strings.finishWorkoutDialogTitle, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${strings.totalDuration}: ${formatSeconds(workoutSeconds)}")
                        Text(strings.finishWorkoutDialogMsg)
                        OutlinedTextField(
                            value = finishNotes,
                            onValueChange = { finishNotes = it },
                            label = { Text(strings.workoutNotesPlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.finishWorkout(finishNotes)
                            showFinishDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(strings.finishBtn, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text(strings.cancelBtn)
                    }
                }
            )
        }
    }
}

@Composable
private fun RoadmapExerciseItem(
    index: Int,
    totalCount: Int,
    exercise: Exercise,
    setsCount: Int,
    isCompleted: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showMenu by remember { mutableStateOf(false) }

    val icon = ExerciseIcons.getIcon(exercise.icon)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else BorderDark,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) SurfaceVariantDark else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        SurfaceVariantDark
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (setsCount > 0) "${strings.recordedSets}: $setsCount" else strings.noSetsYet,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ПРАВАЯ ЧАСТЬ: КНОПКИ ВВЕРХ/ВНИЗ И МЕНЮ «ТРИ ТОЧКИ»
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = strings.moveUp,
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
                            contentDescription = strings.moveDown,
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
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (index > 0) {
                            DropdownMenuItem(
                                text = { Text(strings.moveUp) },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                                onClick = {
                                    onMoveUp()
                                    showMenu = false
                                }
                            )
                        }
                        if (index < totalCount - 1) {
                            DropdownMenuItem(
                                text = { Text(strings.moveDown) },
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                                onClick = {
                                    onMoveDown()
                                    showMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(strings.replaceExercise) },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, null) },
                            onClick = {
                                onReplace()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.deleteFromPlan) },
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
