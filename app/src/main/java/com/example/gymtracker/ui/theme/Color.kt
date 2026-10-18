package com.example.gymtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Чистая глубокая темная тема (AMOLED Dark) для качалки
val BackgroundDark = Color(0xFF0F0F12)
val SurfaceDark = Color(0xFF18181D)
val SurfaceVariantDark = Color(0xFF22222B)
val SurfaceHighlightDark = Color(0xFF2C2C37)

val DefaultPrimaryNeon = Color(0xFF00E676) // Яркий спортивный зеленый акцент по умолчанию

val PrimaryNeon: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val PrimaryContainerDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val OnPrimaryDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val SecondaryAccent = Color(0xFF2979FF) // Синий акцент для тегов и вариаций
val SecondaryContainerDark = Color(0xFF002266)

val TagColor = Color(0xFFFF9100) // Оранжевый для дропсетов
val FailureTagColor = Color(0xFFFF1744) // Красный для отказа
val EasyTagColor = Color(0xFF00E5FF) // Голубой для "Легко"

val TextPrimary = Color(0xFFF1F1F4)
val TextSecondary = Color(0xFFA0A0AB)
val TextMuted = Color(0xFF6B6B78)

val BorderDark = Color(0xFF2E2E38)
val ErrorRed = Color(0xFFFF5252)