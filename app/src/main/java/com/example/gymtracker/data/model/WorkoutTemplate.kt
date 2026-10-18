package com.example.gymtracker.data.model

data class WorkoutTemplate(
    val id: Long = 0,
    val folderId: Long? = null, // ID папки/сплита (например: "3-дневный сплит")
    val name: String,           // Например: "День 1: Грудь и Трицепс"
    val description: String = ""
)

data class WorkoutTemplateItem(
    val id: Long = 0,
    val templateId: Long,
    val exerciseId: Long,
    val variationId: Long? = null,
    val targetSets: Int = 3,
    val orderIndex: Int = 0
)
