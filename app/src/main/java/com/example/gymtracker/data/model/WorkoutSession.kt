package com.example.gymtracker.data.model

data class WorkoutSession(
    val id: Long = 0,
    val title: String = "Тренировка",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val notes: String = ""
)
