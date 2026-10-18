package com.example.gymtracker.ui.components

import androidx.compose.foundation.clickable
import com.example.gymtracker.data.model.ExerciseVariation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.gymtracker.ui.theme.EasyTagColor
import com.example.gymtracker.ui.theme.FailureTagColor
import com.example.gymtracker.ui.theme.TagColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSetDialog(
    initialWeight: Float,
    initialReps: Int,
    variations: List<ExerciseVariation> = emptyList(),
    initialVariationId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (weight: Float, reps: Int, tags: List<String>, intensity: String, notes: String, variationId: Long?) -> Unit
) {
    var weight by remember { mutableFloatStateOf(if (initialWeight > 0) initialWeight else 60f) }
    var reps by remember { mutableIntStateOf(if (initialReps > 0) initialReps else 10) }
    var selectedVariationId by remember { mutableStateOf<Long?>(initialVariationId) }

    // Состояния для прямого ввода с клавиатуры при клике на центр
    var showDirectWeightInput by remember { mutableStateOf(false) }
    var directWeightText by remember { mutableStateOf("") }

    var showDirectRepsInput by remember { mutableStateOf(false) }
    var directRepsText by remember { mutableStateOf("") }

    val defaultTags = listOf("Дропсет", "Другой тренажер", "С читингом", "В бинтах", "Разминка")
    val selectedTags = remember { mutableStateListOf<String>() }

    val intensityOptions = listOf("Легко", "Нормально", "Тяжело", "Отказ")
    var selectedIntensity by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }

    // Диалог прямого ввода веса
    if (showDirectWeightInput) {
        AlertDialog(
            onDismissRequest = { showDirectWeightInput = false },
            title = { Text("Введи точный вес (кг)") },
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
                    directWeightText.toFloatOrNull()?.let { weight = it }
                    showDirectWeightInput = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectWeightInput = false }) { Text("Отмена") }
            }
        )
    }

    // Диалог прямого ввода повторений
    if (showDirectRepsInput) {
        AlertDialog(
            onDismissRequest = { showDirectRepsInput = false },
            title = { Text("Введи точное число повторений") },
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
                TextButton(onClick = { showDirectRepsInput = false }) { Text("Отмена") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.navigationBarsPadding().imePadding(),
        title = {
            Text(
                "Запись подхода",
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
                // ВЕС (Шаг 1 кг + тап по центру для ввода)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Вес снаряда",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "нажми на число для ввода",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalIconButton(onClick = { if (weight >= 1f) weight -= 1f }) {
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
                                text = "${if (weight % 1 == 0f) weight.toInt() else weight} кг",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        FilledTonalIconButton(onClick = { weight += 1f }) {
                            Icon(Icons.Default.Add, "+1")
                        }
                    }
                }

                // ПОВТОРЕНИЯ (Шаг 1 + тап по центру)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Повторения",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "нажми на число для ввода",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
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
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$reps",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        FilledTonalIconButton(onClick = { reps += 1 }) {
                            Icon(Icons.Default.Add, "+1")
                        }
                    }
                }

                // ИНТЕНСИВНОСТЬ
                Column {
                    Text(
                        text = "Ощущение / Интенсивность (необязательно):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        intensityOptions.forEach { opt ->
                            val isSelected = selectedIntensity == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedIntensity = if (isSelected) "" else opt
                                },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (opt) {
                                        "Отказ" -> FailureTagColor.copy(alpha = 0.3f)
                                        "Тяжело" -> TagColor.copy(alpha = 0.3f)
                                        "Легко" -> EasyTagColor.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                )
                            )
                        }
                    }
                }

                // ТЕГИ СЕТА
                Column {
                    Text(
                        text = "Теги (необязательно):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        defaultTags.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                // ФИШКА УПРАЖНЕНИЯ
                if (variations.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Фишка упражнения:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = selectedVariationId == null,
                                onClick = { selectedVariationId = null },
                                label = { Text("Стандарт") }
                            )
                            variations.forEach { v ->
                                FilterChip(
                                    selected = selectedVariationId == v.id,
                                    onClick = { selectedVariationId = v.id },
                                    label = { Text(v.name) }
                                )
                            }
                        }
                    }
                }

                // ЗАМЕТКА К СЕТУ
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметка к подходу (опционально)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(weight, reps, selectedTags.toList(), selectedIntensity, notes, selectedVariationId)
                    onDismiss()
                }
            ) {
                Text("Сохранить подход", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AddSetDialog(
    initialWeight: Float,
    initialReps: Int,
    onDismiss: () -> Unit,
    onConfirm: (weight: Float, reps: Int, tags: List<String>, intensity: String, notes: String) -> Unit
) {
    AddSetDialog(
        initialWeight = initialWeight,
        initialReps = initialReps,
        variations = emptyList(),
        initialVariationId = null,
        onDismiss = onDismiss,
        onConfirm = { w, r, tags, intensity, notes, _ ->
            onConfirm(w, r, tags, intensity, notes)
        }
    )
}
