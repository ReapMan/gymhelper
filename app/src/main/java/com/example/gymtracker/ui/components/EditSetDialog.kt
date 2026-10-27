package com.example.gymtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.ExerciseVariation
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.theme.EasyTagColor
import com.example.gymtracker.ui.theme.FailureTagColor
import com.example.gymtracker.ui.theme.TagColor
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSetDialog(
    set: WorkoutSet,
    variations: List<ExerciseVariation> = emptyList(),
    isTagsEnabled: Boolean = true,
    availableFeelings: List<String> = emptyList(),
    availableTags: List<String> = emptyList(),
    quickWeightSteps: List<Float> = listOf(-5f, -2.5f, -0.5f, 0.5f, 2.5f, 5f),
    onDismiss: () -> Unit,
    onConfirm: (updatedSet: WorkoutSet) -> Unit
) {
    val strings = LocalAppStrings.current

    var weight by remember { mutableFloatStateOf(set.weightKg) }
    var reps by remember { mutableIntStateOf(set.reps) }
    var selectedVariationId by remember { mutableStateOf(set.variationId) }

    var showDirectWeightInput by remember { mutableStateOf(false) }
    var directWeightText by remember { mutableStateOf("") }

    var showDirectRepsInput by remember { mutableStateOf(false) }
    var directRepsText by remember { mutableStateOf("") }

    val defaultTagList = remember(availableTags) {
        if (availableTags.isNotEmpty()) availableTags else listOf(strings.tagDrop, strings.tagFailure, strings.tagWarmup, strings.tagEasy)
    }
    val selectedTags = remember {
        mutableStateListOf<String>().apply {
            if (set.tags.isNotBlank()) {
                addAll(set.tags.split(", ").map { it.trim() }.filter { it.isNotEmpty() })
            }
        }
    }

    val defaultFeelingList = remember(availableFeelings) {
        if (availableFeelings.isNotEmpty()) availableFeelings else listOf(
            strings.setTypeNormal,
            strings.setTypeWarmup,
            strings.setTypeDrop,
            strings.setTypeFailure
        )
    }
    var selectedIntensity by remember { mutableStateOf(set.intensityStatus) }
    var notes by remember { mutableStateOf(set.notes) }
    var showNotesInput by remember { mutableStateOf(set.notes.isNotBlank()) }

    // Прямой ввод веса
    if (showDirectWeightInput) {
        AlertDialog(
            onDismissRequest = { showDirectWeightInput = false },
            title = { Text(strings.weightLabel) },
            text = {
                OutlinedTextField(
                    value = directWeightText,
                    onValueChange = { directWeightText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = { Text("${if (weight % 1 == 0f) weight.toInt() else weight}") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    directWeightText.replace(',', '.').toFloatOrNull()?.let {
                        weight = (((it * 10).roundToInt()) / 10f).coerceAtLeast(0f)
                    }
                    showDirectWeightInput = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectWeightInput = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    // Прямой ввод повторений
    if (showDirectRepsInput) {
        AlertDialog(
            onDismissRequest = { showDirectRepsInput = false },
            title = { Text(strings.repsLabel) },
            text = {
                OutlinedTextField(
                    value = directRepsText,
                    onValueChange = { directRepsText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("$reps") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    directRepsText.toIntOrNull()?.let { if (it > 0) reps = it }
                    showDirectRepsInput = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectRepsInput = false }) { Text(strings.cancelBtn) }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
        title = {
            Text(
                "${strings.editSetTitle}: ${strings.setLabel} ${set.setNumber}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ВЕС
                Column {
                    Text(
                        text = strings.weightLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalIconButton(onClick = {
                            weight = (((weight - 1.0f) * 10).roundToInt() / 10f).coerceAtLeast(0f)
                        }) {
                            Icon(Icons.Default.Remove, "-1")
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable {
                                    directWeightText = "${if (weight % 1 == 0f) weight.toInt() else weight}"
                                    showDirectWeightInput = true
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${if (weight % 1 == 0f) weight.toInt() else weight} ${strings.kgUnit}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalIconButton(onClick = {
                            weight = (((weight + 1.0f) * 10).roundToInt() / 10f)
                        }) {
                            Icon(Icons.Default.Add, "+1")
                        }
                    }

                    // Быстрые шаги веса
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickWeightSteps.forEach { step ->
                            val formatted = if (step % 1f == 0f) step.toInt().toString() else "%.1f".format(java.util.Locale.US, step)
                            val label = if (step > 0) "+$formatted" else formatted
                            val isPlus = step > 0
                            Surface(
                                onClick = {
                                    weight = (((weight + step) * 10).roundToInt() / 10f).coerceAtLeast(0f)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isPlus) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) 
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp, 
                                    if (isPlus) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) 
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPlus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // ПОВТОРЕНИЯ
                Column {
                    Text(
                        text = strings.repsLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalIconButton(onClick = { if (reps > 1) reps -= 1 }) {
                            Icon(Icons.Default.Remove, "-1")
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable {
                                    directRepsText = "$reps"
                                    showDirectRepsInput = true
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$reps ${strings.repsUnit}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalIconButton(onClick = { reps += 1 }) {
                            Icon(Icons.Default.Add, "+1")
                        }
                    }
                }

                // ВАРИАЦИИ / ХВАТ
                if (variations.isNotEmpty()) {
                    Column {
                        Text(
                            text = strings.variationsTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedVariationId == null,
                                onClick = { selectedVariationId = null },
                                label = { Text(strings.standardVariation) },
                                shape = RoundedCornerShape(8.dp)
                            )
                            variations.forEach { v ->
                                FilterChip(
                                    selected = selectedVariationId == v.id,
                                    onClick = { selectedVariationId = v.id },
                                    label = { Text(v.name) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                // ТИП ПОДХОДА / ОЩУЩЕНИЕ (если включено в настройках)
                if (isTagsEnabled) {
                    Column {
                        Text(
                            text = strings.setLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            defaultFeelingList.forEach { opt ->
                                FilterChip(
                                    selected = selectedIntensity.equals(opt, ignoreCase = true),
                                    onClick = {
                                        selectedIntensity = if (selectedIntensity == opt) "" else opt
                                    },
                                    label = { Text(opt) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // ТЕГИ ПОДХОДА
                    Column {
                        Text(
                            text = "Теги",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            defaultTagList.forEach { tag ->
                                val isSelected = selectedTags.contains(tag)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                    },
                                    label = { Text(tag) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when {
                                            tag.contains("Отказ") || tag.contains("Відмова") || tag.contains("ჩავარდნა") -> FailureTagColor.copy(alpha = 0.3f)
                                            tag.contains("Легко") || tag.contains("марტივი") -> EasyTagColor.copy(alpha = 0.3f)
                                            else -> TagColor.copy(alpha = 0.3f)
                                        }
                                    )
                                )
                            }
                        }
                    }
                }

                // КОМПАКТНАЯ ЗАМЕТКА (кнопка с карандашиком, экономящая место)
                if (!showNotesInput && notes.isBlank()) {
                    TextButton(
                        onClick = { showNotesInput = true },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Добавить заметку к подходу", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.workoutNotesPlaceholder) },
                        trailingIcon = {
                            IconButton(onClick = {
                                notes = ""
                                showNotesInput = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Убрать", modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = set.copy(
                        weightKg = weight,
                        reps = reps,
                        variationId = selectedVariationId,
                        intensityStatus = selectedIntensity,
                        tags = selectedTags.joinToString(", "),
                        notes = notes.trim()
                    )
                    onConfirm(updated)
                }
            ) {
                Text(strings.saveBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelBtn)
            }
        }
    )
}
