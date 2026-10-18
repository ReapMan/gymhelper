package com.example.gymtracker.data.model

data class WorkoutSet(
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val variationId: Long? = null,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val tags: String = "",            // Необязательные теги сета: "Дропсет", "Другой тренажер", "Бинты"
    val intensityStatus: String = "", // Необязательный статус повтора: "Легко", "Нормально", "Тяжело", "Отказ"
    val notes: String = "",           // Необязательный коммент: "Шло на удивление бодро"
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
