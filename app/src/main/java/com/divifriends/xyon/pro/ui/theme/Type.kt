package com.divifriends.xyon.pro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.divifriends.xyon.pro.R

/*
 * Three voices, all bundled so the app stays 100 % offline:
 *  - Barlow (Black → Regular): punchy UI grotesk, uppercase headers.
 *  - Instrument Serif: editorial display for screen titles and the brand.
 *  - Space Mono: small numbers and "tech" labels. Big hero money uses
 *    Barlow Black with tabular figures, which reads better at 60sp+.
 */

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
    Font(R.font.barlow_black, FontWeight.Black)
)

val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)

val SpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold)
)

/** Tabular figures so animated amounts never jitter horizontally. */
val Tabular = TextStyle(fontFeatureSettings = "tnum")

/** Turns any style into the money/number voice (Space Mono Bold). */
fun TextStyle.mono(): TextStyle = copy(
    fontFamily = SpaceMono,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-0.4).sp
)

/** Huge uppercase wordmark (onboarding, brand moments). */
val WordmarkStyle = TextStyle(
    fontFamily = Barlow,
    fontWeight = FontWeight.Black,
    fontSize = 54.sp,
    lineHeight = 52.sp,
    letterSpacing = (-1.5).sp
)

val DiviTypography = Typography(
    // Money in the hero: one huge number per screen.
    displayLarge = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Black,
        fontSize = 68.sp,
        lineHeight = 68.sp,
        letterSpacing = (-2.5).sp,
        fontFeatureSettings = "tnum"
    ),
    // Editorial big titles.
    displayMedium = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1).sp
    ),
    // Screen titles in the hero.
    headlineMedium = TextStyle(
        fontFamily = InstrumentSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.4).sp
    ),
    // Sheet titles.
    headlineSmall = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp
    ),
    // Section headers (sentence case).
    titleLarge = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Barlow,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.1.sp
    ),
    // Captions, dates, small meta: monospaced "tech" voice. Never below 12sp.
    labelMedium = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = (-0.2).sp
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = (-0.1).sp
    )
)
