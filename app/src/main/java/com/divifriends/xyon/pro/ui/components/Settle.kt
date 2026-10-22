package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.EmphasizedDecelerate
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wraps a debt row so settling it is a small moment instead of a jump: a lime
 * line strikes through the row, a ring of particles pops where the action was,
 * then the row folds away and only then [onSettled] commits the change.
 *
 * [content] receives the `settle` callback to wire into its button or swipe.
 */
@Composable
fun SettleOut(
    onSettled: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (settle: () -> Unit) -> Unit
) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    var settling by remember { mutableStateOf(false) }
    val strike = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val fold = remember { Animatable(1f) }

    LaunchedEffect(settling) {
        if (!settling) return@LaunchedEffect
        strike.animateTo(1f, tween(300, easing = EmphasizedDecelerate))
        haptics?.tick()
        launch { burst.animateTo(1f, tween(650, easing = LinearEasing)) }
        delay(320)
        fold.animateTo(0f, tween(300, easing = EmphasizedDecelerate))
        onSettled()
    }

    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val height = (placeable.height * fold.value).roundToInt()
                layout(placeable.width, height) { placeable.place(0, 0) }
            }
            .graphicsLayer { alpha = fold.value }
            .drawWithContent {
                drawContent()
                if (strike.value > 0f) {
                    val y = size.height / 2f
                    val inset = 18.dp.toPx()
                    drawLine(
                        color = palette.positive,
                        start = Offset(inset, y),
                        end = Offset(inset + (size.width - inset * 2) * strike.value, y),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                val b = burst.value
                if (b > 0f && b < 1f) {
                    val center = Offset(size.width - 52.dp.toPx(), size.height / 2f)
                    val reach = 46.dp.toPx() * b
                    val fade = 1f - b
                    drawCircle(
                        color = palette.positive.copy(alpha = fade * 0.8f),
                        radius = reach * 0.9f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx() * fade)
                    )
                    for (i in 0 until 12) {
                        val a = i * (Math.PI * 2 / 12).toFloat() + 0.3f
                        val r = reach * (if (i % 2 == 0) 1.15f else 0.8f)
                        drawCircle(
                            color = (if (i % 3 == 0) palette.onBlush else palette.positive).copy(alpha = fade),
                            radius = 3.dp.toPx() * (0.4f + fade),
                            center = Offset(center.x + cos(a) * r, center.y + sin(a) * r)
                        )
                    }
                }
            }
    ) {
        content { if (!settling) settling = true }
    }
}
