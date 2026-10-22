package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlin.math.cos
import kotlin.math.sin

/** Root canvas: the sheet color, so overscroll and gaps never flash white/black. */
@Composable
fun DiviBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalDiviPalette.current.sheet),
        content = content
    )
}

/**
 * The signature green → teal gradient with two light "aurora" blobs drifting
 * slowly and a pair of outlined hexagons, like the reference cards.
 */
@Composable
fun HeroGradient(modifier: Modifier = Modifier, animated: Boolean = true) {
    val palette = LocalDiviPalette.current
    val transition = rememberInfiniteTransition(label = "aurora")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "auroraT"
    )
    val phase = if (animated) t * 2f * Math.PI.toFloat() else 0f

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.heroStart, palette.heroEnd),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.9f, size.height * 1.1f)
            )
        )
        val r1 = size.width * 0.75f
        val c1 = Offset(
            size.width * (0.85f + 0.08f * cos(phase)),
            size.height * (0.05f + 0.08f * sin(phase))
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(palette.heroGlow, Color.Transparent), c1, r1),
            radius = r1,
            center = c1
        )
        val r2 = size.width * 0.6f
        val c2 = Offset(
            size.width * (0.1f + 0.1f * sin(phase * 0.8f)),
            size.height * (0.9f + 0.06f * cos(phase))
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(palette.heroGlow.copy(alpha = palette.heroGlow.alpha * 0.7f), Color.Transparent), c2, r2),
            radius = r2,
            center = c2
        )
        hexOutline(
            center = Offset(size.width * 0.92f, size.height * 0.62f),
            radius = size.width * 0.16f,
            rotation = phase * 4f,
            color = Color.White.copy(alpha = 0.07f)
        )
        hexOutline(
            center = Offset(size.width * 0.04f, size.height * 0.18f),
            radius = size.width * 0.1f,
            rotation = -phase * 4f,
            color = Color.White.copy(alpha = 0.06f)
        )
    }
}

internal fun DrawScope.hexOutline(center: Offset, radius: Float, rotation: Float, color: Color, strokeWidth: Float = 3f) {
    val path = Path()
    for (i in 0 until 6) {
        val a = Math.toRadians(60.0 * i - 90.0 + rotation)
        val p = Offset(center.x + radius * cos(a).toFloat(), center.y + radius * sin(a).toFloat())
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = strokeWidth))
}
