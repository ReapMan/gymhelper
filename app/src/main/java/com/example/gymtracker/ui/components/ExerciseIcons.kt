package com.example.gymtracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AirlineSeatFlat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.ui.graphics.vector.ImageVector

data class ExerciseIconItem(
    val key: String,
    val title: String,
    val icon: ImageVector
)

object ExerciseIcons {
    val allIcons = listOf(
        ExerciseIconItem("dumbbell", "Гантель", Icons.Default.FitnessCenter),
        ExerciseIconItem("bench", "Скамья / Жим", Icons.Default.AirlineSeatFlat),
        ExerciseIconItem("pullup", "Турник / Гимнастика", Icons.Default.SportsGymnastics),
        ExerciseIconItem("squat", "Присед / Стойка", Icons.Default.AccessibilityNew),
        ExerciseIconItem("cable", "Блок / Рукоять", Icons.Default.SportsHandball),
        ExerciseIconItem("cardio", "Бег / Кардио", Icons.Default.DirectionsRun),
        ExerciseIconItem("stretch", "Растяжка / Йога", Icons.Default.SelfImprovement),
        ExerciseIconItem("martial", "Стойка / Борьба", Icons.Default.SportsMartialArts),
        ExerciseIconItem("power", "Взрывная мощь", Icons.Default.Bolt)
    )

    fun getIcon(key: String?): ImageVector {
        return allIcons.find { it.key == key }?.icon ?: Icons.Default.FitnessCenter
    }
}
