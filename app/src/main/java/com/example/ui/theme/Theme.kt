package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OranPrimaryDark,
    secondary = OranSecondaryDark,
    tertiary = OranTertiaryDark,
    background = OranBackgroundDark,
    surface = OranSurfaceDark,
    onPrimary = OranBackgroundDark,
    onSecondary = OranBackgroundDark,
    onBackground = OranOnBackgroundDark,
    onSurface = OranOnBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = OranPrimary,
    secondary = OranSecondary,
    tertiary = OranTertiary,
    background = OranBackground,
    surface = OranSurface,
    onPrimary = OranSurface,
    onSecondary = OranSurface,
    onBackground = OranOnBackground,
    onSurface = OranOnBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
