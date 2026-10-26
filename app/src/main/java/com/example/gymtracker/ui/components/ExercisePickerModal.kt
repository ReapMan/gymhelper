package com.example.gymtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gymtracker.data.model.Exercise
import com.example.gymtracker.data.model.MuscleGroup
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.i18n.localizeMuscleGroupName
import com.example.gymtracker.ui.theme.BackgroundDark
import com.example.gymtracker.ui.theme.BorderDark
import com.example.gymtracker.ui.theme.PrimaryNeon
import com.example.gymtracker.ui.theme.SurfaceDark
import com.example.gymtracker.ui.theme.SurfaceVariantDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerModal(
    allExercises: List<Exercise>,
    muscleGroups: List<MuscleGroup> = emptyList(),
    onDismiss: () -> Unit,
    onSelectExercises: (List<Exercise>) -> Unit,
    onCreateCustomExercise: ((name: String, muscleGroupId: Long) -> Unit)? = null
) {
    val strings = LocalAppStrings.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroupId by remember { mutableStateOf<Long?>(null) } // null = Все
    val selectedExercises = remember { mutableStateListOf<Exercise>() }

    var showCreateCustomDialog by remember { mutableStateOf(false) }

    // Фильтрация по поиску и мышечной группе
    val filteredExercises = allExercises.filter { ex ->
        val matchesQuery = searchQuery.isBlank() || ex.name.contains(searchQuery, ignoreCase = true)
        val matchesGroup = selectedMuscleGroupId == null || ex.muscleGroupId == selectedMuscleGroupId
        matchesQuery && matchesGroup
    }

    // Группировка упражнений по мышечным группам
    val groupedExercises: Map<MuscleGroup, List<Exercise>> = remember(filteredExercises, muscleGroups) {
        val groupsMap = muscleGroups.associateBy { it.id }
        filteredExercises.groupBy { ex ->
            groupsMap[ex.muscleGroupId] ?: MuscleGroup(id = 0L, name = strings.mgOther)
        }.toSortedMap(compareBy { it.name })
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ШАПКА
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = strings.cancelBtn,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.chooseExerciseTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (onCreateCustomExercise != null) {
                        Button(
                            onClick = { showCreateCustomDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.createCustomExercise,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ПОЛЕ ПОИСКА
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = { Text(strings.searchPlaceholder, color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderDark
                    )
                )

                // ГОРИЗОНТАЛЬНЫЕ ЧИПЫ ГРУПП МЫШЦ
                if (muscleGroups.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Чип «Все мышцы»
                        item {
                            FilterChip(
                                selected = selectedMuscleGroupId == null,
                                onClick = { selectedMuscleGroupId = null },
                                label = { Text(strings.allMuscleGroups, fontWeight = FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = SurfaceDark,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Чипы конкретных групп мышц
                        items(muscleGroups, key = { it.id }) { group ->
                            val isSelected = selectedMuscleGroupId == group.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMuscleGroupId = if (isSelected) null else group.id
                                },
                                label = {
                                    Text(
                                        localizeMuscleGroupName(group.name, strings),
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = SurfaceDark,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // СПИСОК УПРАЖНЕНИЙ
                if (filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.noExercisesFound,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedExercises.forEach { (group, exercisesInGroup) ->
                            // Заголовок группы мышц
                            item(key = "header_${group.id}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${localizeMuscleGroupName(group.name, strings)} (${exercisesInGroup.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Карточки упражнений
                            items(exercisesInGroup, key = { it.id }) { exercise ->
                                val isSelected = selectedExercises.any { it.id == exercise.id }
                                val icon = ExerciseIcons.getIcon(exercise.icon)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) {
                                                selectedExercises.removeAll { it.id == exercise.id }
                                            } else {
                                                selectedExercises.add(exercise)
                                            }
                                        }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else BorderDark,
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
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
                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else SurfaceVariantDark
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = exercise.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (exercise.notes.isNotBlank()) {
                                                    Text(
                                                        text = exercise.notes,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    if (!selectedExercises.any { it.id == exercise.id }) {
                                                        selectedExercises.add(exercise)
                                                    }
                                                } else {
                                                    selectedExercises.removeAll { it.id == exercise.id }
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary,
                                                uncheckedColor = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ПЛАВАЮЩАЯ НИЖНЯЯ КНОПКА ДОБАВЛЕНИЯ В ТРЕНИРОВКУ
            AnimatedVisibility(
                visible = selectedExercises.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = {
                        onSelectExercises(selectedExercises.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.addSelectedCount.format(selectedExercises.size),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // ДИАЛОГ БЫСТРОГО СОЗДАНИЯ СВОЕГО УПРАЖНЕНИЯ
    if (showCreateCustomDialog && onCreateCustomExercise != null) {
        var newName by remember { mutableStateOf("") }
        var selectedGroupForNew by remember { mutableStateOf(muscleGroups.firstOrNull()?.id ?: 1L) }
        var expandedGroupDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateCustomDialog = false },
            title = { Text(strings.createCustomExercise, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(strings.newExerciseNamePlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (muscleGroups.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedGroupDropdown,
                            onExpandedChange = { expandedGroupDropdown = it }
                        ) {
                            val selectedName = muscleGroups.find { it.id == selectedGroupForNew }?.let {
                                localizeMuscleGroupName(it.name, strings)
                            } ?: strings.mgOther

                            OutlinedTextField(
                                value = selectedName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.selectMuscleGroup) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroupDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedGroupDropdown,
                                onDismissRequest = { expandedGroupDropdown = false }
                            ) {
                                muscleGroups.forEach { mg ->
                                    DropdownMenuItem(
                                        text = { Text(localizeMuscleGroupName(mg.name, strings)) },
                                        onClick = {
                                            selectedGroupForNew = mg.id
                                            expandedGroupDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newName.trim()
                        if (name.isNotEmpty()) {
                            onCreateCustomExercise(name, selectedGroupForNew)
                            showCreateCustomDialog = false
                        }
                    },
                    enabled = newName.trim().isNotEmpty()
                ) {
                    Text(strings.createAndAddBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCustomDialog = false }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }
}
