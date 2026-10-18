package com.example.gymtracker.data.model

data class ProgramFolder(
    val id: Long = 0,
    val name: String, // Например: "3-дневный сплит", "Фулбоди", "Верх / Низ"
    val description: String = ""
)
