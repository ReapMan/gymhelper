package com.example.gymtracker.ui.i18n

fun getStringsForLanguage(language: AppLanguage): AppStrings {
    return when (language) {
        AppLanguage.RUSSIAN -> StringsRu
        AppLanguage.UKRAINIAN -> StringsUk
        AppLanguage.GEORGIAN -> StringsKa
    }
}

fun localizeMuscleGroupName(originalName: String, strings: AppStrings): String {
    val lower = originalName.lowercase().trim()
    return when {
        lower.contains("груд") || lower.contains("chest") -> strings.mgChest
        lower.contains("спин") || lower.contains("back") -> strings.mgBack
        lower.contains("ног") || lower.contains("leg") || lower.contains("квадрицепс") || lower.contains("бицепс бедра") -> strings.mgLegs
        lower.contains("плеч") || lower.contains("дельта") || lower.contains("shoulder") -> strings.mgShoulders
        lower.contains("бицепс") || lower.contains("biceps") -> strings.mgBiceps
        lower.contains("трицепс") || lower.contains("triceps") -> strings.mgTriceps
        lower.contains("пресс") || lower.contains("кор") || lower.contains("abs") -> strings.mgAbs
        else -> originalName
    }
}
