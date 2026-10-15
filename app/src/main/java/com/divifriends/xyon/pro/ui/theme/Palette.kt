package com.divifriends.xyon.pro.ui.theme

import androidx.compose.ui.graphics.Color
import com.divifriends.xyon.pro.data.model.THEME_GREEN
import com.divifriends.xyon.pro.data.model.THEME_ORANGE
import com.divifriends.xyon.pro.data.model.THEME_PURPLE
import com.divifriends.xyon.pro.data.model.THEME_RED
import com.divifriends.xyon.pro.data.model.THEME_TEAL
import com.divifriends.xyon.pro.data.model.THEME_YELLOW

/** Static, theme-independent colors from the original palette. */
val AppBackground = Color(0xFF0A0A10)
val TextPrimary = Color(0xFAFFFFFF)
val TextSecondary = Color(0xB3FFFFFF)
val GlassFill = Color(0x33232332)
val GlassFillStrong = Color(0x66141422)
val GlassBorder = Color(0x1AFFFFFF)
val ScrimColor = Color(0xB3000000)

data class DiviPalette(
    val key: String,
    val label: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val danger: Color,
    val border: Color
)

val DiviPalettes: List<DiviPalette> = listOf(
    DiviPalette(THEME_TEAL, "Teal \uD83D\uDCA7", Color(0xFF00F2FF), Color(0xFF39FF14), Color(0xFFE8FF00), Color(0xFFFF073A), Color(0x4000F2FF)),
    DiviPalette(THEME_ORANGE, "Orange \uD83D\uDD25", Color(0xFFFFA500), Color(0xFFFFD700), Color(0xFF39FF14), Color(0xFFFF4500), Color(0x4DFFA500)),
    DiviPalette(THEME_PURPLE, "Purple \uD83D\uDD2E", Color(0xFF9D00FF), Color(0xFFFF00FF), Color(0xFF00F2FF), Color(0xFFFF007F), Color(0x409D00FF)),
    DiviPalette(THEME_RED, "Red \uD83C\uDF36\uFE0F", Color(0xFFFF073A), Color(0xFFFF4500), Color(0xFFFFA500), Color(0xFFFF007F), Color(0x4DFF073A)),
    DiviPalette(THEME_GREEN, "Green \uD83C\uDF33", Color(0xFF39FF14), Color(0xFFADFF2F), Color(0xFFE8FF00), Color(0xFFFF3131), Color(0x4D39FF14)),
    DiviPalette(THEME_YELLOW, "Yellow \u26A1\uFE0F", Color(0xFFE8FF00), Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFF4500), Color(0x40E8FF00))
)

fun paletteFor(key: String?): DiviPalette =
    DiviPalettes.firstOrNull { it.key == key } ?: DiviPalettes.first()
