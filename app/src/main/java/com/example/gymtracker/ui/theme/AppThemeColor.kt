package com.example.gymtracker.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeColor(
    val id: String,
    val title: String,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color = Color(0xFF0F0F12)
) {
    NEON_GREEN(
        id = "neon_green",
        title = "Зеленый Неон",
        primary = Color(0xFF00E676),
        primaryContainer = Color(0xFF00381B),
        onPrimary = Color(0xFF00210E)
    ),
    CYBER_BLUE(
        id = "cyber_blue",
        title = "Кибер-синий",
        primary = Color(0xFF00E5FF),
        primaryContainer = Color(0xFF003844),
        onPrimary = Color(0xFF002026)
    ),
    AMBER_FLAME(
        id = "amber_flame",
        title = "Огненный",
        primary = Color(0xFFFF9100),
        primaryContainer = Color(0xFF422200),
        onPrimary = Color(0xFF261200)
    ),
    ELECTRIC_PURPLE(
        id = "electric_purple",
        title = "Фиолетовый Неон",
        primary = Color(0xFFD500F9),
        primaryContainer = Color(0xFF380042),
        onPrimary = Color(0xFF200026)
    ),
    CRIMSON_RED(
        id = "crimson_red",
        title = "Красный Рубин",
        primary = Color(0xFFFF1744),
        primaryContainer = Color(0xFF42000E),
        onPrimary = Color(0xFF260007)
    ),
    TITANIUM_GOLD(
        id = "titanium_gold",
        title = "Золотой Титан",
        primary = Color(0xFFFFD600),
        primaryContainer = Color(0xFF3E3400),
        onPrimary = Color(0xFF241E00)
    );

    companion object {
        fun fromId(id: String?): AppThemeColor {
            return entries.firstOrNull { it.id == id } ?: NEON_GREEN
        }
    }
}
