package com.abhijit.footlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = FootlogColors.backgroundDark,
    surface = FootlogColors.surfaceDark,
    onBackground = FootlogColors.textPrimaryDark,
    onSurface = FootlogColors.textPrimaryDark,
    primary = FootlogColors.routeLineDark,
    onPrimary = FootlogColors.backgroundDark,
    secondary = FootlogColors.highlightAccentDark,
    onSecondary = FootlogColors.backgroundDark,
    error = FootlogColors.danger,
    outline = FootlogColors.borderDark,
    surfaceVariant = FootlogColors.surfaceDark,
    onSurfaceVariant = FootlogColors.textSecondaryDark,
)

private val LightColorScheme = lightColorScheme(
    background = FootlogColors.backgroundLight,
    surface = FootlogColors.surfaceLight,
    onBackground = FootlogColors.textPrimaryLight,
    onSurface = FootlogColors.textPrimaryLight,
    primary = FootlogColors.routeLineLight,
    onPrimary = FootlogColors.backgroundLight,
    secondary = FootlogColors.highlightAccentLight,
    onSecondary = FootlogColors.backgroundLight,
    error = FootlogColors.danger,
    outline = FootlogColors.borderLight,
    surfaceVariant = FootlogColors.surfaceLight,
    onSurfaceVariant = FootlogColors.textSecondaryLight,
)

@Composable
fun FootlogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FootlogTypography,
        content = content
    )
}
