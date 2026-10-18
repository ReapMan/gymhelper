package com.example.gymtracker.data.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val muscleGroupId: Long,
    val notes: String = "", // Памятка по упражнению: "Сиденье на 4", "Ноги ближе к краю"
    val icon: String = "dumbbell"
)
