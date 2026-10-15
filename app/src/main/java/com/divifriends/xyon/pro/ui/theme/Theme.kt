package com.divifriends.xyon.pro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun DiviTheme(
    paletteKey: String,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(paletteKey)
    val scheme = darkColorScheme(
        primary = palette.primary,
        onPrimary = Color(0xFF111111),
        secondary = palette.secondary,
        onSecondary = Color(0xFF111111),
        tertiary = palette.accent,
        onTertiary = Color(0xFF111111),
        error = palette.danger,
        onError = Color(0xFF111111),
        background = AppBackground,
        onBackground = TextPrimary,
        surface = Color(0xFF15151F),
        onSurface = TextPrimary,
        surfaceVariant = GlassFill,
        onSurfaceVariant = TextSecondary,
        outline = palette.border
    )

    CompositionLocalProvider(LocalDiviPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = DiviTypography,
            content = content
        )
    }
}
