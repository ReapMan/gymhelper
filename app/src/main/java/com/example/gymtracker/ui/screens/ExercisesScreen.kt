package com.example.gymtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.ExerciseVariation
import com.example.gymtracker.data.model.MuscleGroup
import com.example.gymtracker.ui.GymViewModel
import com.example.gymtracker.ui.components.ExerciseIcons
import com.example.gymtracker.ui.theme.BorderDark
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SecondaryAccent
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark
import com.example.gymtracker.ui.theme.TagColor

@Composable
fun ExercisesScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val allExercises by viewModel.exercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()
    val allVariations by viewModel.allVariations.collectAsState()

    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var expandedExerciseId by remember { mutableStateOf<Long?>(null) }

    var showSimpleAddDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }

    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var variationToDelete by remember { mutableStateOf<ExerciseVariation?>(null) }
    var variationToRename by remember { mutableStateOf<ExerciseVariation?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshAllVariations()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSimpleAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить упражнение")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Строка управления группами упражнений
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Группы упражнений:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showAddGroupDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Группа", fontSize = 12.sp)
                }
            }

            // Фильтры по группам
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGroupId == null,
                        onClick = { selectedGroupId = null },
                        label = { Text("Все (${allExercises.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryNeon
                        )
                    )
                }
                items(muscleGroups) { group ->
                    val count = allExercises.count { it.muscleGroupId == group.id }
                    FilterChip(
                        selected = selectedGroupId == group.id,
                        onClick = { selectedGroupId = group.id },
                        label = { Text("${group.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryNeon
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val filteredList = if (selectedGroupId == null) {
                allExercises
            } else {
                allExercises.filter { it.muscleGroupId == selectedGroupId }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { exercise ->
                    val groupName = muscleGroups.find { it.id == exercise.muscleGroupId }?.name ?: ""
                    val exerciseVariations = allVariations.filter { it.exerciseId == exercise.id }
                    val isExpanded = expandedExerciseId == exercise.id

                    ExerciseAccordionCard(
                        exercise = exercise,
                        groupName = groupName,
                        variations = exerciseVariations,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedExerciseId = if (isExpanded) null else exercise.id
                        },
                        onIconSelected = { iconKey ->
                            viewModel.updateExerciseFull(
                                exerciseId = exercise.id,
                                name = exercise.name,
                                notes = exercise.notes,
                                icon = iconKey,
                                muscleGroupId = exercise.muscleGroupId
                            )
                        },
                        onSaveNotes = { newNotes ->
                            viewModel.updateExerciseNotes(exercise, newNotes)
                        },
                        onAddVariation = { varName ->
                            viewModel.addVariationToExercise(exercise.id, varName)
                        },
                        onRenameVariationClick = { v ->
                            variationToRename = v
                        },
                        onDeleteVariationClick = { v ->
                            variationToDelete = v
                        },
                        onDeleteExerciseClick = {
                            exerciseToDelete = exercise
                        }
                    )
                }
            }
        }

        // ДИАЛОГ ДОБАВЛЕНИЯ НОВОЙ ГРУППЫ УПРАЖНЕНИЙ
        if (showAddGroupDialog) {
            var groupName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddGroupDialog = false },
                title = { Text("Новая группа упражнений") },
                text = {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Название (например: Пресс, Шея, Кардио)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.addMuscleGroup(groupName.trim())
                            showAddGroupDialog = false
                        }
                    }) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGroupDialog = false }) { Text("Отмена") }
                }
            )
        }

        // УПРОЩЕННЫЙ ДИАЛОГ ДОБАВЛЕНИЯ УПРАЖНЕНИЯ
        if (showSimpleAddDialog) {
            SimpleAddExerciseModal(
                muscleGroups = muscleGroups,
                onDismiss = { showSimpleAddDialog = false },
                onConfirm = { name, gId ->
                    viewModel.addExerciseSimple(name, gId)
                    showSimpleAddDialog = false
                }
            )
        }

        // ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ УПРАЖНЕНИЯ
        if (exerciseToDelete != null) {
            AlertDialog(
                onDismissRequest = { exerciseToDelete = null },
                title = { Text("Удалить упражнение?") },
                text = { Text("Точно удалить «${exerciseToDelete!!.name}»? Все связанные с ним подходы и фишки также будут удалены.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteExercise(exerciseToDelete!!.id)
                            exerciseToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { exerciseToDelete = null }) { Text("Отмена") }
                }
            )
        }

        // ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ ФИШКИ
        if (variationToDelete != null) {
            val v = variationToDelete!!
            AlertDialog(
                onDismissRequest = { variationToDelete = null },
                title = { Text("Удалить фишку?") },
                text = { Text("Точно удалить фишку «${v.name}»?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteVariation(v.id, v.exerciseId)
                            variationToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { variationToDelete = null }) { Text("Отмена") }
                }
            )
        }

        // ПЕРЕИМЕНОВАНИЕ ФИШКИ
        if (variationToRename != null) {
            val v = variationToRename!!
            var editName by remember { mutableStateOf(v.name) }
            AlertDialog(
                onDismissRequest = { variationToRename = null },
                title = { Text("Изменить фишку", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Название фишки (рукоять, хват, угол)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isNotBlank()) {
                                viewModel.renameVariation(v.id, editName.trim(), v.exerciseId)
                                variationToRename = null
                            }
                        },
                        enabled = editName.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { variationToRename = null }) { Text("Отмена") }
                }
            )
        }
    }
}

@Composable
private fun ExerciseAccordionCard(
    exercise: Exercise,
    groupName: String,
    variations: List<ExerciseVariation>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onIconSelected: (String) -> Unit,
    onSaveNotes: (String) -> Unit,
    onAddVariation: (String) -> Unit,
    onRenameVariationClick: (ExerciseVariation) -> Unit,
    onDeleteVariationClick: (ExerciseVariation) -> Unit,
    onDeleteExerciseClick: () -> Unit
) {
    var notesText by remember(exercise.notes) { mutableStateOf(exercise.notes) }
    var newVarText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = if (isExpanded) androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // ШАПКА КАРТОЧКИ (ВСЕГДА ВИДНА, НАЖАТИЕ РАСКРЫВАЕТ ШТОРКУ)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // АВАТАР-ИКОНКА УПРАЖНЕНИЯ
                    Surface(
                        shape = CircleShape,
                        color = if (isExpanded) PrimaryNeon.copy(alpha = 0.2f) else SurfaceVariantDark,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = ExerciseIcons.getIcon(exercise.icon),
                                contentDescription = null,
                                tint = if (isExpanded) PrimaryNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) PrimaryNeon else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (groupName.isNotBlank()) {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = " • ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "Фишек: ${variations.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteExerciseClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить упражнение",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                        tint = if (isExpanded) PrimaryNeon else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ШТОРКА (РАСКРЫВАЮЩАЯСЯ ЧАСТЬ)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = BorderDark)

                    // 1. ВЫБОР ИКОНКИ ДЛЯ УПРАЖНЕНИЯ
                    Column {
                        Text(
                            text = "Иконка упражнения:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(ExerciseIcons.allIcons) { iconItem ->
                                val isSelected = exercise.icon == iconItem.key
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryNeon.copy(alpha = 0.25f) else SurfaceVariantDark,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onIconSelected(iconItem.key) }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) PrimaryNeon else BorderDark,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = iconItem.icon,
                                            contentDescription = iconItem.title,
                                            tint = if (isSelected) PrimaryNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = iconItem.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryNeon else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. ЗАМЕТКА И НАСТРОЙКИ ТРЕНАЖЕРА
                    Column {
                        Text(
                            text = "Памятка / настройки тренажера:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                placeholder = { Text("Сиденье 4, угол 30°, упор 2...", fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onSaveNotes(notesText.trim()) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ОК")
                            }
                        }
                    }

                    // 3. ФИШКИ УПРАЖНЕНИЯ (РУКОЯТИ, ХВАТЫ, УГЛЫ)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Фишки (рукояти, углы, хваты):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryAccent
                            )
                            Text(
                                text = "${variations.size} шт.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (variations.isEmpty()) {
                            Text(
                                text = "Фишки пока не добавлены. Добавь первую ниже (например: прямая рукоять, канаты, узкий хват).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                variations.forEach { v ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = PrimaryNeon.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(8.dp)
                                                ) {}
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = v.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { onRenameVariationClick(v) },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Переименовать фишку",
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { onDeleteVariationClick(v) },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Удалить фишку",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ПОЛЕ ДОБАВЛЕНИЯ НОВОЙ ФИШКИ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newVarText,
                                onValueChange = { newVarText = it },
                                placeholder = { Text("Новая фишка...", fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newVarText.isNotBlank()) {
                                        onAddVariation(newVarText.trim())
                                        newVarText = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                enabled = newVarText.isNotBlank()
                            ) {
                                Text("+")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleAddExerciseModal(
    muscleGroups: List<MuscleGroup>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, muscleGroupId: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf(muscleGroups.firstOrNull()?.id ?: 1L) }
    var expanded by remember { mutableStateOf(false) }

    val selectedGroupName = muscleGroups.find { it.id == selectedGroupId }?.name ?: "Выбери группу"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое упражнение", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название упражнения") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedGroupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Группа мышц") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        muscleGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedGroupId)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
