package com.divifriends.xyon.pro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun DiviTheme(
    dark: Boolean,
    content: @Composable () -> Unit
) {
    val p = diviPalette(dark)

    val scheme = if (dark) {
        darkColorScheme(
            primary = p.accent,
            onPrimary = p.onAccent,
            primaryContainer = p.accentSoft,
            onPrimaryContainer = p.accent,
            secondary = p.sun,
            onSecondary = p.onSun,
            tertiary = p.accentDeep,
            onTertiary = p.onAccent,
            error = p.negative,
            onError = Color.White,
            background = p.sheet,
            onBackground = Color(0xFFFBECEA),
            surface = p.sheet,
            onSurface = Color(0xFFFBECEA),
            surfaceVariant = p.raised,
            onSurfaceVariant = Color(0xFFB39391),
            surfaceContainer = p.raised,
            surfaceContainerHigh = p.raised,
            surfaceContainerLow = p.sheet,
            outline = p.hairline,
            outlineVariant = p.hairline,
            scrim = Color(0xCC000000)
        )
    } else {
        lightColorScheme(
            primary = p.accent,
            onPrimary = p.onAccent,
            primaryContainer = p.accentSoft,
            onPrimaryContainer = p.accentDeep,
            secondary = p.sun,
            onSecondary = p.onSun,
            tertiary = p.accentDeep,
            onTertiary = p.onAccent,
            error = p.negative,
            onError = Color.White,
            background = p.sheet,
            onBackground = Color(0xFF2A1215),
            surface = p.sheet,
            onSurface = Color(0xFF2A1215),
            surfaceVariant = p.raised,
            onSurfaceVariant = Color(0xFF8C6F6E),
            surfaceContainer = p.raised,
            surfaceContainerHigh = p.raised,
            surfaceContainerLow = p.sheet,
            outline = p.hairline,
            outlineVariant = p.hairline,
            scrim = Color(0x8C1E0508)
        )
    }

    CompositionLocalProvider(LocalDiviPalette provides p) {
        MaterialTheme(
            colorScheme = scheme,
            typography = DiviTypography,
            content = content
        )
    }
}
