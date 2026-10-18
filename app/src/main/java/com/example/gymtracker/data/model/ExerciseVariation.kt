package com.example.gymtracker.data.model

data class ExerciseVariation(
    val id: Long = 0,
    val exerciseId: Long,
    val name: String // Например: "Широкий хват", "V-рукоять", "Параллельный", "В Смите", "Одной рукой"
)
