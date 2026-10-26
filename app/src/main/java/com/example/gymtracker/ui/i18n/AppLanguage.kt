package com.example.gymtracker.ui.i18n

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
) {
    RUSSIAN("ru", "Русский", "Русский", "🇷🇺"),
    UKRAINIAN("uk", "Украинский", "Українська", "🇺🇦"),
    GEORGIAN("ka", "Грузинский", "ქართული", "🇬🇪");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: RUSSIAN
        }
    }
}
