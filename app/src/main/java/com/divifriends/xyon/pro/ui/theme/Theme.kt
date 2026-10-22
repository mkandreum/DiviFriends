package com.divifriends.xyon.pro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/** Dark-only theme: everything is drawn on the Noir · Aurora palette. */
@Composable
fun DiviTheme(content: @Composable () -> Unit) {
    val p = NoirAurora

    val scheme = darkColorScheme(
        primary = p.accent,
        onPrimary = p.onAccent,
        primaryContainer = p.accentSoft,
        onPrimaryContainer = p.accent,
        secondary = p.onBlush,
        onSecondary = p.blush,
        tertiary = p.accentDeep,
        onTertiary = p.onAccent,
        error = p.negative,
        onError = Color.Black,
        background = p.sheet,
        onBackground = Color(0xFFF4F4F5),
        surface = p.sheet,
        onSurface = Color(0xFFF4F4F5),
        surfaceVariant = p.raised,
        onSurfaceVariant = Color(0xFF8E8E99),
        surfaceContainer = p.raised,
        surfaceContainerHigh = p.raised,
        surfaceContainerLow = p.sheet,
        outline = p.hairline,
        outlineVariant = p.hairline,
        scrim = Color(0xD9000000)
    )

    CompositionLocalProvider(LocalDiviPalette provides p) {
        MaterialTheme(
            colorScheme = scheme,
            typography = DiviTypography,
            content = content
        )
    }
}
