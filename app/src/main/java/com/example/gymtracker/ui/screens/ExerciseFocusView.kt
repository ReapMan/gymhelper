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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Toc
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseHistoryDay
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.components.AddSetDialog
import com.example.gymtracker.ui.components.ExerciseIcons
import com.example.gymtracker.ui.components.SetRow
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
    val exerciseVariations by viewModel.exerciseVariations.collectAsState()
    val selectedVariation by viewModel.selectedVariation.collectAsState()
    val lastSetHint by viewModel.lastSetHint.collectAsState()
    val sessionSets by viewModel.activeSessionSets.collectAsState()

    val currentExerciseSeconds by viewModel.currentExerciseSeconds.collectAsState()
    val restSeconds by viewModel.restSeconds.collectAsState()
    val isPerformingSet by viewModel.isPerformingSet.collectAsState()

    val exerciseSets = sessionSets.filter { it.exerciseId == exercise.id }

    var showAddSetDialog by remember { mutableStateOf(false) }
    var showAddVariationDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showPyramidHistoryDialog by remember { mutableStateOf(false) }
    var setToDelete by remember { mutableStateOf<WorkoutSet?>(null) }

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
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
        ) {
            // ВЕРХНЯЯ ПАНЕЛЬ: ТАЙМЕР ПО ЦЕНТРУ + КВАДРАТНАЯ КНОПКА ПЛАНА СПРАВА ВВЕРХУ
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка назад к роадмапу
                    IconButton(
                        onClick = { viewModel.exitExerciseFocus() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "К плану тренировки",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Таймер упражнения СТРОГО ПО ЦЕНТРУ СВЕРХУ
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceVariantDark,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "Упражнение: ${formatSeconds(currentExerciseSeconds)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    // Квадратная кнопка плана тренировки СПРАВА ВВЕРХУ
                    IconButton(
                        onClick = { viewModel.exitExerciseFocus() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Toc,
                            contentDescription = "План тренировки",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // КАРТОЧКА УПРАЖНЕНИЯ
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryNeon.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = ExerciseIcons.getIcon(exercise.icon),
                                            contentDescription = null,
                                            tint = PrimaryNeon,
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
                                    contentDescription = "Заметка к тренажеру",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Заметка к тренажеру
                        if (exercise.notes.isNotBlank()) {
                            Text(
                                text = "📌 ${exercise.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ФИШКА (бывшая Разновидность)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Фишка:",
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
                                    label = { Text("Стандарт") },
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
                                    Icon(Icons.Default.Add, contentDescription = "Добавить фишку")
                                }
                            }
                        }

                        // ПОДСКАЗКА ПРОШЛОГО РЕЗУЛЬТАТА + КНОПКА «ВСЯ ИСТОРИЯ ПОДХОДОВ»
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (lastSetHint != null) {
                                Surface(
                                    color = PrimaryContainerDark.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = PrimaryNeon,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val varSuffix = selectedVariation?.let { " (${it.name})" } ?: ""
                                        Text(
                                            text = "Прошлый раз$varSuffix: ${if (lastSetHint!!.weightKg % 1 == 0f) lastSetHint!!.weightKg.toInt() else lastSetHint!!.weightKg}×${lastSetHint!!.reps}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNeon
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            // Кнопка для просмотра полной лесенки/елочки прошлых дней
                            OutlinedButton(
                                onClick = { showPyramidHistoryDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("История фишки", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // БЛОК СОСТОЯНИЯ: ПОДХОД / ОТДЫХ
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
                                text = "🔥 ИДЁТ ПОДХОД #${exerciseSets.size + 1}",
                                fontWeight = FontWeight.Black,
                                color = PrimaryNeon,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Как закончишь — жми кнопку снизу",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "ОТДЫХ",
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
                            Text(
                                text = "Восстанавливай дыхание",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // СПИСОК ВЫПОЛНЕННЫХ ПОДХОДОВ ЗА СЕГОДНЯ
            if (exerciseSets.isNotEmpty()) {
                item {
                    Text(
                        text = "Подходы за сегодня (${exerciseSets.size}):",
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
                            text = "Подходов пока нет.\nНажми «Начать подход» снизу.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // ПЛАВАЮЩИЕ КНОПКИ УПРАВЛЕНИЯ ВНИЗУ
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
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
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Закончить подход и записать",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
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
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Начать подход",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.goToNextExercise() },
                            modifier = Modifier.height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("След. упражнение", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ДИАЛОГИ
        if (showAddSetDialog) {
            val lastSet = exerciseSets.lastOrNull()
            val initW = lastSet?.weightKg ?: lastSetHint?.weightKg ?: 60f
            val initR = lastSet?.reps ?: lastSetHint?.reps ?: 10

            AddSetDialog(
                initialWeight = initW,
                initialReps = initR,
                onDismiss = { showAddSetDialog = false },
                onConfirm = { w, r, tags, intensity, notes ->
                    viewModel.recordSetAndStartRest(w, r, tags, intensity, notes)
                    showAddSetDialog = false
                }
            )
        }

        // ДИАЛОГ ДОБАВЛЕНИЯ ФИШКИ
        if (showAddVariationDialog) {
            var varName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddVariationDialog = false },
                title = { Text("Новая фишка") },
                text = {
                    OutlinedTextField(
                        value = varName,
                        onValueChange = { varName = it },
                        label = { Text("Название (например: V-образная, Узкий хват)") },
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
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVariationDialog = false }) { Text("Отмена") }
                }
            )
        }

        if (showEditNotesDialog) {
            var nText by remember { mutableStateOf(exercise.notes) }
            AlertDialog(
                onDismissRequest = { showEditNotesDialog = false },
                title = { Text("Памятка по тренажеру") },
                text = {
                    OutlinedTextField(
                        value = nText,
                        onValueChange = { nText = it },
                        label = { Text("Настройки высоты, сиденья, упора...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateExerciseNotes(exercise, nText.trim())
                            showEditNotesDialog = false
                        }
                    ) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNotesDialog = false }) { Text("Отмена") }
                }
            )
        }

        // МОДАЛКА: ПОЛНАЯ ИСТОРИЯ ПОДХОДОВ ДЛЯ ЭТОЙ ФИШКИ (ЕЛОЧКА/ПИРАМИДА)
        if (showPyramidHistoryDialog) {
            var historyDays by remember { mutableStateOf<List<ExerciseHistoryDay>>(emptyList()) }
            LaunchedEffect(exercise.id, selectedVariation?.id) {
                historyDays = viewModel.loadExerciseHistory(exercise.id, selectedVariation?.id)
            }

            val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
            val curVarName = selectedVariation?.name ?: "Стандарт"

            AlertDialog(
                onDismissRequest = { showPyramidHistoryDialog = false },
                title = {
                    Column {
                        Text(exercise.name, fontWeight = FontWeight.Black)
                        Text(
                            "История фишки: $curVarName",
                            fontSize = 13.sp,
                            color = SecondaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    if (historyDays.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ранее подходов с этой фишкой не было",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.height(350.dp)) {
                            items(historyDays) { day ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "${dateFormat.format(Date(day.timestamp))} • ${day.sessionTitle}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Выводим все подходы дня (лесенка / елочка!)
                                        day.sets.forEach { s ->
                                            Text(
                                                text = "Сет ${s.setNumber}:  ${if (s.weightKg % 1 == 0f) s.weightKg.toInt() else s.weightKg} кг × ${s.reps} повт." +
                                                        if (s.intensityStatus.isNotBlank()) " [${s.intensityStatus}]" else "" +
                                                        if (s.tags.isNotBlank()) " (${s.tags})" else "",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
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
                        Text("Закрыть")
                    }
                }
            )
        }

        // ДИАЛОГ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ ПОДХОДА
        val targetSet = setToDelete
        if (targetSet != null) {
            AlertDialog(
                onDismissRequest = { setToDelete = null },
                title = { Text("Удалить подход?") },
                text = {
                    val w = if (targetSet.weightKg % 1 == 0f) targetSet.weightKg.toInt() else targetSet.weightKg
                    Text("Удалить подход #${targetSet.setNumber} ($w кг × ${targetSet.reps} повт.)?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSet(targetSet)
                            setToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { setToDelete = null }) { Text("Отмена") }
                }
            )
        }
    }
}
