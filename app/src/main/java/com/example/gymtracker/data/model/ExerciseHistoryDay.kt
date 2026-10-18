package com.example.gymtracker.data.model

data class ExerciseHistoryDay(
    val sessionId: Long,
    val sessionTitle: String,
    val timestamp: Long,
    val sets: List<WorkoutSet>
)
