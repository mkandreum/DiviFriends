package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.divifriends.xyon.pro.ui.theme.AppBackground
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlin.math.roundToInt

/**
 * Full screen neon backdrop: two soft radial glows (theme primary + secondary)
 * plus a slow "falling money" emoji layer, mirroring the original design without
 * any CDN dependency.
 */
@Composable
fun NeonBackground(
    modifier: Modifier = Modifier,
    showMoney: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalDiviPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(AppBackground)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.primary.copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.18f),
                        radius = size.maxDimension * 0.55f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.secondary.copy(alpha = 0.24f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.85f),
                        radius = size.maxDimension * 0.6f
                    )
                )
            }
    ) {
        if (showMoney) MoneyRain(color = palette.primary)
        content()
    }
}

private data class MoneyFlake(
    val x: Float,
    val startDelay: Float,
    val speed: Float,
    val sizeSp: Float,
    val glyph: String
)

@Composable
private fun MoneyRain(color: Color, modifier: Modifier = Modifier) {
    val flakes = remember {
        val glyphs = listOf("\uD83D\uDCB8", "\uD83D\uDCB6", "\uD83D\uDCB0")
        List(15) {
            MoneyFlake(
                x = Math.random().toFloat(),
                startDelay = Math.random().toFloat(),
                speed = 0.6f + Math.random().toFloat() * 0.8f,
                sizeSp = 14f + Math.random().toFloat() * 20f,
                glyph = glyphs.random()
            )
        }
    }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "money")
    // State is read inside the layout lambda below, so the animation only
    // re-runs layout each frame instead of recomposing the whole layer.
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "moneyProgress"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
    ) {
        if (boxSize.height == 0) return@Box
        flakes.forEach { flake ->
            Text(
                text = flake.glyph,
                color = color.copy(alpha = 0.18f),
                fontSize = flake.sizeSp.sp,
                modifier = Modifier.offset {
                    val fraction = (progress.value * flake.speed + flake.startDelay) % 1f
                    val y = (fraction * (boxSize.height + 200f)) - 100f
                    val x = flake.x * boxSize.width
                    IntOffset(x.roundToInt(), y.roundToInt())
                }
            )
        }
    }
}
