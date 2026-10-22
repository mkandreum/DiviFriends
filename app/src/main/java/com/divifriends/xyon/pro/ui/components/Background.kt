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
 * Near-black hero with three "aurora" blobs (lime, violet, cyan) flowing on
 * slow Lissajous paths at different speeds, so the light never repeats the
 * same shape twice in a row.
 */
@Composable
fun HeroGradient(modifier: Modifier = Modifier, animated: Boolean = true) {
    val palette = LocalDiviPalette.current
    val transition = rememberInfiniteTransition(label = "aurora")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
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
        auroraBlob(
            color = palette.heroGlow2,
            center = Offset(
                size.width * (0.82f + 0.16f * cos(phase)),
                size.height * (0.12f + 0.14f * sin(phase * 2f))
            ),
            radius = size.width * (0.78f + 0.08f * sin(phase * 3f))
        )
        auroraBlob(
            color = palette.heroGlow3,
            center = Offset(
                size.width * (0.12f + 0.18f * sin(phase * 2f)),
                size.height * (0.78f + 0.12f * cos(phase))
            ),
            radius = size.width * (0.66f + 0.06f * cos(phase * 2f))
        )
        auroraBlob(
            color = palette.heroGlow,
            center = Offset(
                size.width * (0.5f + 0.32f * sin(phase)),
                size.height * (0.5f + 0.22f * cos(phase * 3f))
            ),
            radius = size.width * (0.5f + 0.06f * sin(phase * 4f))
        )
        // Fade the bottom into the hero base so the sheet edge stays crisp.
        drawRect(
            brush = Brush.verticalGradient(
                0.6f to Color.Transparent,
                1f to palette.heroEnd.copy(alpha = 0.7f)
            )
        )
    }
}

private fun DrawScope.auroraBlob(color: Color, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to color,
            0.45f to color.copy(alpha = color.alpha * 0.45f),
            1f to Color.Transparent,
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
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
