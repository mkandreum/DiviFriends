package com.divifriends.xyon.pro.ui.theme

import androidx.compose.ui.graphics.Color

/* ------------------------------------------------------------------ *
 *  Emerald design system                                              *
 *                                                                     *
 *  A green → teal gradient "hero" sits on top of every screen; the    *
 *  content lives on a rounded sheet that slides over it. Pastel mint  *
 *  and aqua tiles plus a single warm "sun" accent for money you owe.  *
 * ------------------------------------------------------------------ */

data class DiviPalette(
    val isDark: Boolean,
    /** Gradient used by the hero area, top-left → bottom-right. */
    val heroStart: Color,
    val heroEnd: Color,
    /** Soft light blob drifting over the hero gradient. */
    val heroGlow: Color,
    /** Content on the hero gradient. */
    val onHero: Color,
    val onHeroMuted: Color,
    /** Translucent white chips/pills placed on the hero. */
    val heroChip: Color,
    /** Primary brand accent (teal-emerald). */
    val accent: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val onAccent: Color,
    /** Pastel tiles. */
    val mint: Color,
    val onMint: Color,
    val aqua: Color,
    val onAqua: Color,
    /** Warm highlight (cards where you owe money, ring progress). */
    val sun: Color,
    val sunSoft: Color,
    val onSun: Color,
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

val EmeraldLight = DiviPalette(
    isDark = false,
    heroStart = Color(0xFF2FBF71),
    heroEnd = Color(0xFF138A85),
    heroGlow = Color(0x33FFFFFF),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xCCFFFFFF),
    heroChip = Color(0x29FFFFFF),
    accent = Color(0xFF17A589),
    accentDeep = Color(0xFF0E7C72),
    accentSoft = Color(0xFFDDF5EC),
    onAccent = Color(0xFFFFFFFF),
    mint = Color(0xFFE4F8EF),
    onMint = Color(0xFF0E6B52),
    aqua = Color(0xFFDDF6F7),
    onAqua = Color(0xFF0D6670),
    sun = Color(0xFFFFC747),
    sunSoft = Color(0xFFFFF3D3),
    onSun = Color(0xFF4A3300),
    sheet = Color(0xFFFFFFFF),
    raised = Color(0xFFF3F7F6),
    nav = Color(0xFFFFFFFF),
    positive = Color(0xFF13A36B),
    negative = Color(0xFFE5484D),
    negativeSoft = Color(0xFFFDE7E8),
    hairline = Color(0xFFE6EEEC),
    shadow = Color(0x3317A589)
)

val EmeraldDark = DiviPalette(
    isDark = true,
    heroStart = Color(0xFF168A55),
    heroEnd = Color(0xFF0B5753),
    heroGlow = Color(0x1FFFFFFF),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xB3FFFFFF),
    heroChip = Color(0x1FFFFFFF),
    accent = Color(0xFF3DD6AE),
    accentDeep = Color(0xFF1FAE8E),
    accentSoft = Color(0xFF15332D),
    onAccent = Color(0xFF04201A),
    mint = Color(0xFF16302A),
    onMint = Color(0xFF7FE8C6),
    aqua = Color(0xFF132E31),
    onAqua = Color(0xFF7ADCE3),
    sun = Color(0xFFFFC747),
    sunSoft = Color(0xFF3A3017),
    onSun = Color(0xFF3A2800),
    sheet = Color(0xFF0E1918),
    raised = Color(0xFF162423),
    nav = Color(0xFF172625),
    positive = Color(0xFF3DD68C),
    negative = Color(0xFFFF6B70),
    negativeSoft = Color(0xFF3A1D1F),
    hairline = Color(0xFF1F302E),
    shadow = Color(0x66000000)
)

fun diviPalette(dark: Boolean): DiviPalette = if (dark) EmeraldDark else EmeraldLight
