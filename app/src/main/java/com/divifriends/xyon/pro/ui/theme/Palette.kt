package com.divifriends.xyon.pro.ui.theme

import androidx.compose.ui.graphics.Color

/* ------------------------------------------------------------------ *
 *  Rojo · Lima design system                                          *
 *                                                                     *
 *  A saturated red gradient "hero" sits on top of every screen with   *
 *  an electric lime accent; content lives on a warm sheet that slides *
 *  over it. Lime and blush tiles, plus a dark "ink" for high-contrast *
 *  blocks (Material 3 Expressive style).                              *
 * ------------------------------------------------------------------ */

data class DiviPalette(
    val isDark: Boolean,
    /** Gradient used by the hero area, top-left → bottom-right. */
    val heroStart: Color,
    val heroEnd: Color,
    /** Soft blob drifting over the hero gradient (lime-tinted). */
    val heroGlow: Color,
    /** Content on the hero gradient. */
    val onHero: Color,
    val onHeroMuted: Color,
    /** Translucent white chips/pills placed on the hero. */
    val heroChip: Color,
    /** Primary brand accent (red). */
    val accent: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val onAccent: Color,
    /** Soft lime tile. */
    val lime: Color,
    val onLime: Color,
    /** Soft red/pink tile. */
    val blush: Color,
    val onBlush: Color,
    /** Electric lime highlight (CTAs on red, money you're owed, rings). */
    val sun: Color,
    val sunSoft: Color,
    val onSun: Color,
    /** Near-black block for contrast tiles and the toast. */
    val ink: Color,
    val onInk: Color,
    /** Sheet that slides over the hero. */
    val sheet: Color,
    /** Raised elements on the sheet (rows, inputs). */
    val raised: Color,
    /** Floating navigation container. */
    val nav: Color,
    /** Money you are owed / money you owe. */
    val positive: Color,
    val negative: Color,
    val negativeSoft: Color,
    /** Hairlines. */
    val hairline: Color,
    /** Soft drop shadow color for floating elements. */
    val shadow: Color
)

val RedLimeLight = DiviPalette(
    isDark = false,
    heroStart = Color(0xFFF2313C),
    heroEnd = Color(0xFFA30F22),
    heroGlow = Color(0x40D4FF3A),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xD9FFFFFF),
    heroChip = Color(0x2EFFFFFF),
    accent = Color(0xFFE3202D),
    accentDeep = Color(0xFFA8101F),
    accentSoft = Color(0xFFFFE4E2),
    onAccent = Color(0xFFFFFFFF),
    lime = Color(0xFFEDFFC2),
    onLime = Color(0xFF2E4400),
    blush = Color(0xFFFFE4E2),
    onBlush = Color(0xFF9A0E1E),
    sun = Color(0xFFD4FF3A),
    sunSoft = Color(0xFFF4FFD6),
    onSun = Color(0xFF1C2600),
    ink = Color(0xFF1E0B0E),
    onInk = Color(0xFFD4FF3A),
    sheet = Color(0xFFFFF8F6),
    raised = Color(0xFFFBEDEA),
    nav = Color(0xFFFFFFFF),
    positive = Color(0xFF4A8A00),
    negative = Color(0xFFE3202D),
    negativeSoft = Color(0xFFFFE4E2),
    hairline = Color(0xFFF2E1DE),
    shadow = Color(0x40E3202D)
)

val RedLimeDark = DiviPalette(
    isDark = true,
    heroStart = Color(0xFFB3141F),
    heroEnd = Color(0xFF4A0710),
    heroGlow = Color(0x2ED4FF3A),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xBFFFFFFF),
    heroChip = Color(0x24FFFFFF),
    accent = Color(0xFFFF4D57),
    accentDeep = Color(0xFFFF7A82),
    accentSoft = Color(0xFF3B1418),
    onAccent = Color(0xFF2A0005),
    lime = Color(0xFF26300F),
    onLime = Color(0xFFD4FF3A),
    blush = Color(0xFF3B1418),
    onBlush = Color(0xFFFF9AA0),
    sun = Color(0xFFD4FF3A),
    sunSoft = Color(0xFF2A3110),
    onSun = Color(0xFF1C2600),
    ink = Color(0xFFD4FF3A),
    onInk = Color(0xFF1C2600),
    sheet = Color(0xFF150C0E),
    raised = Color(0xFF221416),
    nav = Color(0xFF241517),
    positive = Color(0xFFB8F03A),
    negative = Color(0xFFFF5C66),
    negativeSoft = Color(0xFF3B1418),
    hairline = Color(0xFF33201F),
    shadow = Color(0x80000000)
)

fun diviPalette(dark: Boolean): DiviPalette = if (dark) RedLimeDark else RedLimeLight
