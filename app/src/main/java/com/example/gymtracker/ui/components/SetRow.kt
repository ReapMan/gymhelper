package com.example.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.data.model.WorkoutSet
import com.example.gymtracker.ui.i18n.LocalAppStrings
import com.example.gymtracker.ui.theme.EasyTagColor
import com.example.gymtracker.ui.theme.FailureTagColor
import com.example.gymtracker.ui.theme.SurfaceVariantDark
import com.example.gymtracker.ui.theme.TagColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetRow(
    set: WorkoutSet,
    variationName: String?,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable { onEdit() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceVariantDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Номер подхода в кружке
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${set.setNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Вес и повторения
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${if (set.weightKg % 1 == 0f) set.weightKg.toInt() else set.weightKg} ${strings.kgUnit}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "×",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${set.reps} ${strings.repsUnit}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Название фишки, если есть
                    if (!variationName.isNullOrBlank()) {
                        Text(
                            text = variationName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Кнопки управления: Редактировать (карандаш) + Удалить (корзина)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEdit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = strings.editSetTitle,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = strings.deleteBtn,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Статус интенсивности и теги
            val tagsList = set.tags.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
            if (set.intensityStatus.isNotBlank() || tagsList.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (set.intensityStatus.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (set.intensityStatus.lowercase()) {
                                "отказ", "відмова", "ჩავარდნა" -> FailureTagColor.copy(alpha = 0.2f)
                                "разминка", "розминка", "გახურება" -> TagColor.copy(alpha = 0.2f)
                                "дропсет", "дроп", "დროპსეტი" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = set.intensityStatus,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (set.intensityStatus.lowercase()) {
                                    "отказ", "відмова", "ჩავარდნა" -> FailureTagColor
                                    "разминка", "розминка", "გახურება" -> TagColor
                                    "дропсет", "дроп", "დროპსეტი" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }

                    tagsList.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (tag.lowercase()) {
                                "отказ", "відмова", "ჩავარდნა" -> FailureTagColor.copy(alpha = 0.15f)
                                "легко", "მარტივი" -> EasyTagColor.copy(alpha = 0.15f)
                                else -> TagColor.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = when (tag.lowercase()) {
                                    "отказ", "відмова", "ჩავარდნა" -> FailureTagColor
                                    "легко", "მარტივი" -> EasyTagColor
                                    else -> TagColor
                                }
                            )
                        }
                    }
                }
            }

            // Заметка к сету
            if (set.notes.isNotBlank()) {
                Text(
                    text = set.notes,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
