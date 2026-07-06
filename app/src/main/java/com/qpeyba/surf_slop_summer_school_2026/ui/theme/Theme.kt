package com.qpeyba.surf_slop_summer_school_2026.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ChefTableColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Card,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = TextPrimary,
    secondary = Olive,
    onSecondary = Card,
    secondaryContainer = OliveLight,
    onSecondaryContainer = TextPrimary,
    tertiary = Cream,
    background = Surface,
    onBackground = TextPrimary,
    surface = Card,
    onSurface = TextPrimary,
    surfaceVariant = Surface,
    onSurfaceVariant = TextSecondary,
    error = DestructiveRed,
    onError = Card,
    outline = TextSecondary
)

@Composable
fun ChefTableTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChefTableColorScheme,
        shapes = ChefTableShapes,
        typography = ChefTableTypography,
        content = content
    )
}
