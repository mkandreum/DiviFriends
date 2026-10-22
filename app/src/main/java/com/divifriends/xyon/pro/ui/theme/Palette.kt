package com.divifriends.xyon.pro.ui.theme

import androidx.compose.ui.graphics.Color

/* ------------------------------------------------------------------ *
 *  Noir · Aurora design system                                        *
 *                                                                     *
 *  Dark-only. A near-black hero with lime, violet and cyan "aurora"   *
 *  blobs flowing behind it; content lives on a pure black sheet.      *
 *  Electric lime is the single accent (CTAs, money you're owed),      *
 *  violet the secondary tile, and warm orange marks what you owe.    *
 *  No red anywhere.                                                   *
 * ------------------------------------------------------------------ */

data class DiviPalette(
    val isDark: Boolean,
    /** Gradient used by the hero area, top-left → bottom-right. */
    val heroStart: Color,
    val heroEnd: Color,
    /** Fluid blobs drifting over the hero (lime, violet, cyan). */
    val heroGlow: Color,
    val heroGlow2: Color,
    val heroGlow3: Color,
    /** Content on the hero gradient. */
    val onHero: Color,
    val onHeroMuted: Color,
    /** Translucent white chips/pills placed on the hero. */
    val heroChip: Color,
    /** Primary brand accent (lime). */
    val accent: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val onAccent: Color,
    /** Soft lime tile. */
    val lime: Color,
    val onLime: Color,
    /** Soft violet tile (secondary tone). */
    val blush: Color,
    val onBlush: Color,
    /** Electric lime highlight (CTAs, money you're owed, rings). */
    val sun: Color,
    val sunSoft: Color,
    val onSun: Color,
    /** High-contrast block for tiles and the toast. */
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

val NoirAurora = DiviPalette(
    isDark = true,
    heroStart = Color(0xFF0E0E14),
    heroEnd = Color(0xFF050507),
    heroGlow = Color(0x66C6FF3D),
    heroGlow2 = Color(0x738B5CF6),
    heroGlow3 = Color(0x5522D3EE),
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xA6FFFFFF),
    heroChip = Color(0x1FFFFFFF),
    accent = Color(0xFFC6FF3D),
    accentDeep = Color(0xFFDDFF85),
    accentSoft = Color(0xFF1B220C),
    onAccent = Color(0xFF0A0F00),
    lime = Color(0xFF1B220C),
    onLime = Color(0xFFC6FF3D),
    blush = Color(0xFF1D1630),
    onBlush = Color(0xFFC4B5FF),
    sun = Color(0xFFC6FF3D),
    sunSoft = Color(0xFF1B220C),
    onSun = Color(0xFF0A0F00),
    ink = Color(0xFFF4F4F5),
    onInk = Color(0xFF0A0A0B),
    sheet = Color(0xFF050506),
    raised = Color(0xFF111115),
    nav = Color(0xFF16161B),
    positive = Color(0xFFC6FF3D),
    negative = Color(0xFFFF9447),
    negativeSoft = Color(0xFF2A1708),
    hairline = Color(0xFF1F1F26),
    shadow = Color(0x99000000)
)
