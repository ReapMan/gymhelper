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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseHistoryDay
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.RestTimerMode
import com.example.gymtracker.ui.components.AddSetDialog
import com.example.gymtracker.ui.components.EditSetDialog
import com.example.gymtracker.ui.components.ExerciseIcons
import com.example.gymtracker.ui.components.SetRow
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.theme.BackgroundDark
import com.example.gymtracker.ui.theme.PrimaryContainerDark
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SecondaryAccent
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark
import com.example.gymtracker.ui.theme.TagColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseFocusView(
    viewModel: GymViewModel,
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val exerciseVariations by viewModel.exerciseVariations.collectAsState()
    val selectedVariation by viewModel.selectedVariation.collectAsState()
    val lastSetHint by viewModel.lastSetHint.collectAsState()
    val sessionSets by viewModel.activeSessionSets.collectAsState()

    val currentExerciseSeconds by viewModel.currentExerciseSeconds.collectAsState()
    val restSeconds by viewModel.restSeconds.collectAsState()
    val isPerformingSet by viewModel.isPerformingSet.collectAsState()

    val restTimerMode by viewModel.restTimerMode.collectAsState()
    val targetRestSeconds by viewModel.targetRestSeconds.collectAsState()
    val quickWeightStepsVariant by viewModel.quickWeightStepsVariant.collectAsState()

    val isSetTagsEnabled by viewModel.isSetTagsEnabled.collectAsState()
    val availableFeelings by viewModel.availableFeelings.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    val exerciseSets = sessionSets.filter { it.exerciseId == exercise.id }

    var showAddSetDialog by remember { mutableStateOf(false) }
    var showAddVariationDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showPyramidHistoryDialog by remember { mutableStateOf(false) }
    var setToDelete by remember { mutableStateOf<WorkoutSet?>(null) }
    var setToEdit by remember { mutableStateOf<WorkoutSet?>(null) }

    fun formatSeconds(sec: Long): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    BackHandler {
        viewModel.exitExerciseFocus()
    }

    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                top = 8.dp
            )
        ) {
            // ВЕРХНИЙ БАР: К СЦЕНАРИЮ + ТАЙМЕР
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.exitExerciseFocus() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.toRoadmap,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = formatSeconds(currentExerciseSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.size(42.dp))
                }
            }

            // КАРТОЧКА УПРАЖНЕНИЯ: ИКОНКА, НАЗВАНИЕ, ФИШКИ И ПОДСКАЗКА
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = ExerciseIcons.getIcon(exercise.icon),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showEditNotesDialog = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = strings.editNotes,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Заметка к упражнению
                        if (exercise.notes.isNotBlank()) {
                            Text(
                                text = "📝 ${exercise.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Фишки (вариации хвата/техники)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.variationsTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedVariation == null,
                                    onClick = { viewModel.selectVariation(null) },
                                    label = { Text(strings.standardVariation) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SecondaryAccent.copy(alpha = 0.3f)
                                    )
                                )
                            }
                            items(exerciseVariations) { v ->
                                FilterChip(
                                    selected = selectedVariation?.id == v.id,
                                    onClick = { viewModel.selectVariation(v) },
                                    label = { Text(v.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SecondaryAccent.copy(alpha = 0.3f)
                                    )
                                )
                            }
                            item {
                                IconButton(
                                    onClick = { showAddVariationDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = strings.addVariationBtn)
                                }
                            }
                        }

                        // Подсказка прошлого подхода + кнопка истории
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (lastSetHint != null) {
                                Surface(
                                    onClick = { showPyramidHistoryDialog = true },
                                    color = PrimaryContainerDark.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val varSuffix = selectedVariation?.let { " (${it.name})" } ?: ""
                                        val weightText = if (lastSetHint!!.weightKg % 1 == 0f) lastSetHint!!.weightKg.toInt() else lastSetHint!!.weightKg
                                        Text(
                                            text = "${strings.prevWorkoutHistory}$varSuffix $weightText ${strings.kgUnit} × ${lastSetHint!!.reps}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // Кнопка истории прошлых тренировок
                            Button(
                                onClick = { showPyramidHistoryDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceVariantDark,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SecondaryAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.historyTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // БЛОК ТАЙМЕРА: СЕТ / ОТДЫХ
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPerformingSet) {
                            PrimaryContainerDark
                        } else {
                            SurfaceVariantDark
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isPerformingSet) {
                            Text(
                                text = "🔥 ${strings.setLabel} #${exerciseSets.size + 1}",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.startSet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            if (restTimerMode == RestTimerMode.COUNTDOWN) {
                                val remaining = targetRestSeconds - restSeconds
                                val isOver = remaining <= 0

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isOver) "🔔 Отдых завершён!" else "⏳ ${strings.restTimer}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isOver) MaterialTheme.colorScheme.primary else TagColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(план: ${formatSeconds(targetRestSeconds)})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isOver) "+${formatSeconds(kotlin.math.abs(remaining))}" else formatSeconds(remaining),
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Быстрые кнопки управления временем отдыха на лету
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = { viewModel.adjustTargetRestSeconds(-30L) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "-30 с",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        onClick = { viewModel.adjustTargetRestSeconds(30L) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "+30 с",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        onClick = { viewModel.adjustTargetRestSeconds(60L) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "+1 мин",
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.resetRestTimer() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Сброс таймера",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = strings.restTimer,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TagColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatSeconds(restSeconds),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // СПИСОК ВЫПОЛНЕННЫХ ПОДХОДОВ
            if (exerciseSets.isNotEmpty()) {
                item {
                    Text(
                        text = "${strings.recordedSets} (${exerciseSets.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(exerciseSets, key = { it.id }) { set ->
                    val varName = exerciseVariations.find { it.id == set.variationId }?.name
                    SetRow(
                        set = set,
                        variationName = varName,
                        onDelete = { setToDelete = set },
                        onEdit = { setToEdit = set },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${strings.noSetsYet}\n${strings.noSetsYetDesc}",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // ПЛАВАЮЩИЙ НИЖНИЙ БАР УПРАВЛЕНИЯ СЕТОМ
        // ВАЖНО: navigationBarsPadding() поднимает бар над системными кнопками Asus ROG
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = SurfaceDark,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isPerformingSet) {
                    Button(
                        onClick = { showAddSetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            strings.finishSet,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startSet() },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                strings.startSet,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.goToNextExercise() },
                            modifier = Modifier.height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.toRoadmap, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ДИАЛОГ ДОБАВЛЕНИЯ СЕТА
        if (showAddSetDialog) {
            val lastSet = exerciseSets.lastOrNull()
            val initW = lastSet?.weightKg ?: lastSetHint?.weightKg ?: 60f
            val initR = lastSet?.reps ?: lastSetHint?.reps ?: 10

            AddSetDialog(
                initialWeight = initW,
                initialReps = initR,
                variations = exerciseVariations,
                initialVariationId = selectedVariation?.id,
                isTagsEnabled = isSetTagsEnabled,
                availableFeelings = availableFeelings,
                availableTags = availableTags,
                quickWeightSteps = quickWeightStepsVariant.steps,
                onDismiss = { showAddSetDialog = false },
                onConfirm = { w, r, tags, intensity, notes, varId ->
                    viewModel.recordSetAndStartRest(w, r, tags, intensity, notes)
                    showAddSetDialog = false
                }
            )
        }

        // ДИАЛОГ РЕДАКТИРОВАНИЯ СЕТА (ПО ТАПУ НА ЛЮБОЙ СЕТ)
        if (setToEdit != null) {
            EditSetDialog(
                set = setToEdit!!,
                variations = exerciseVariations,
                isTagsEnabled = isSetTagsEnabled,
                availableFeelings = availableFeelings,
                availableTags = availableTags,
                quickWeightSteps = quickWeightStepsVariant.steps,
                onDismiss = { setToEdit = null },
                onConfirm = { updated ->
                    viewModel.updateSet(updated)
                    setToEdit = null
                }
            )
        }

        // ДИАЛОГ ДОБАВЛЕНИЯ ФИШКИ
        if (showAddVariationDialog) {
            var varName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddVariationDialog = false },
                title = { Text(strings.addVariationTitle) },
                text = {
                    OutlinedTextField(
                        value = varName,
                        onValueChange = { varName = it },
                        label = { Text(strings.variationNamePlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (varName.isNotBlank()) {
                                viewModel.addVariationToExercise(exercise.id, varName.trim())
                                showAddVariationDialog = false
                            }
                        }
                    ) {
                        Text(strings.saveBtn)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVariationDialog = false }) { Text(strings.cancelBtn) }
                }
            )
        }

        // ДИАЛОГ РЕДАКТИРОВАНИЯ ЗАМЕТКИ
        if (showEditNotesDialog) {
            var nText by remember { mutableStateOf(exercise.notes) }
            AlertDialog(
                onDismissRequest = { showEditNotesDialog = false },
                title = { Text(strings.editNotes) },
                text = {
                    OutlinedTextField(
                        value = nText,
                        onValueChange = { nText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateExerciseNotes(exercise, nText.trim())
                            showEditNotesDialog = false
                        }
                    ) {
                        Text(strings.saveBtn)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNotesDialog = false }) { Text(strings.cancelBtn) }
                }
            )
        }

        // ДИАЛОГ ИСТОРИИ ПРОШЛЫХ ТРЕНИРОВОК
        if (showPyramidHistoryDialog) {
            var historyDays by remember { mutableStateOf<List<ExerciseHistoryDay>>(emptyList()) }
            LaunchedEffect(exercise.id, selectedVariation?.id) {
                historyDays = viewModel.loadExerciseHistory(exercise.id, selectedVariation?.id)
            }

            AlertDialog(
                onDismissRequest = { showPyramidHistoryDialog = false },
                title = { Text(strings.prevWorkoutHistory) },
                text = {
                    if (historyDays.isEmpty()) {
                        Text(strings.noHistoryYet)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(historyDays) { day ->
                                val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(day.timestamp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(dateStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        day.sets.forEach { s ->
                                            Text(
                                                text = "${strings.setLabel} ${s.setNumber}: ${if (s.weightKg % 1 == 0f) s.weightKg.toInt() else s.weightKg} ${strings.kgUnit} × ${s.reps} ${strings.repsUnit}",
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPyramidHistoryDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // ДИАЛОГ УДАЛЕНИЯ СЕТА
        if (setToDelete != null) {
            val targetSet = setToDelete!!
            AlertDialog(
                onDismissRequest = { setToDelete = null },
                title = { Text(strings.deleteSetConfirmTitle) },
                text = {
                    Text("${strings.deleteSetConfirmMsg} (${strings.setLabel} #${targetSet.setNumber}, ${targetSet.weightKg} ${strings.kgUnit} × ${targetSet.reps})")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSet(targetSet)
                            setToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(strings.deleteBtn)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { setToDelete = null }) { Text(strings.cancelBtn) }
                }
            )
        }
    }
}
