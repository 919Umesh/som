package com.example.som.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SomTealDark,
    onPrimary = SomOnTealDark,
    primaryContainer = SomTealContainerDark,
    onPrimaryContainer = SomOnTealContainerDark,
    secondary = SomSlateDark,
    secondaryContainer = SomSlateContainerDark,
    tertiary = SomBlueDark,
    background = SomBackgroundDark,
    onBackground = SomOnSurfaceDark,
    surface = SomSurfaceDark,
    onSurface = SomOnSurfaceDark,
    surfaceContainer = SomSurfaceContainerDark,
    surfaceContainerHigh = SomSurfaceContainerHighDark,
    surfaceContainerHighest = SomSurfaceContainerHighestDark,
    onSurfaceVariant = SomOnSurfaceVariantDark,
    outline = SomOutlineDark,
    error = SomErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = SomTeal,
    onPrimary = SomOnTeal,
    primaryContainer = SomTealContainer,
    onPrimaryContainer = SomOnTealContainer,
    secondary = SomSlate,
    secondaryContainer = SomSlateContainer,
    tertiary = SomBlue,
    background = SomBackground,
    onBackground = SomOnSurface,
    surface = SomSurface,
    onSurface = SomOnSurface,
    surfaceContainer = SomSurfaceContainer,
    surfaceContainerHigh = SomSurfaceContainerHigh,
    surfaceContainerHighest = SomSurfaceContainerHighest,
    onSurfaceVariant = SomOnSurfaceVariant,
    outline = SomOutline,
    error = SomError
)

@Composable
fun SomTheme(
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
