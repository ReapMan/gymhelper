package com.example.gymtracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

fun getDarkColorScheme(themeColor: AppThemeColor): ColorScheme {
    return darkColorScheme(
        primary = themeColor.primary,
        onPrimary = themeColor.onPrimary,
        primaryContainer = themeColor.primaryContainer,
        onPrimaryContainer = themeColor.primary,
        secondary = SecondaryAccent,
        onSecondary = TextPrimary,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = TextPrimary,
        background = BackgroundDark,
        onBackground = TextPrimary,
        surface = SurfaceDark,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = TextSecondary,
        outline = BorderDark,
        error = ErrorRed
    )
}

@Composable
fun GymTrackerTheme(
    themeColor: AppThemeColor = AppThemeColor.NEON_GREEN,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getDarkColorScheme(themeColor),
        typography = Typography,
        content = content
    )
}