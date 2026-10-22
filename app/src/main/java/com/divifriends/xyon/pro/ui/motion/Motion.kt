package com.divifriends.xyon.pro.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.haptics.DiviHaptics
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import kotlinx.coroutines.delay

/** Emphasized decelerate curve (Material 3 motion). */
val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** Bouncy-but-controlled spring used for most micro-interactions. */
fun <T> softSpring(stiffness: Float = Spring.StiffnessMediumLow) =
    spring<T>(dampingRatio = 0.72f, stiffness = stiffness)

/* ------------------------------------------------------------------ *
 *  Micro-interactions                                                *
 * ------------------------------------------------------------------ */

/**
 * Springs the receiver down while pressed. Use on any tappable surface for the
 * signature "press" feel.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.95f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = if (pressed) 1f else 0.45f,
            stiffness = if (pressed) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * The one-stop tappable modifier: spring press-scale + haptic + no ripple.
 * [feedback] picks which haptic plays on tap.
 */
@Composable
fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    feedback: (DiviHaptics) -> Unit = { it.press() },
    onClick: () -> Unit
): Modifier {
    val haptics = LocalHaptics.current
    val source = remember { MutableInteractionSource() }
    return this
        .pressScale(source, pressedScale)
        .clickable(
            enabled = enabled,
            interactionSource = source,
            indication = null
        ) {
            haptics?.let(feedback)
            onClick()
        }
}

/**
 * Fade + rise + slight scale entrance, staggered by [index]. Plays once per
 * composition (not on every recomposition).
 */
@Composable
fun Modifier.staggeredAppear(
    index: Int,
    enabled: Boolean = true,
    stepMillis: Int = 55,
    maxSteps: Int = 10
): Modifier {
    val step = index.coerceIn(0, maxSteps)
    var visible by rememberSaveable { mutableStateOf(!enabled) }
    LaunchedEffect(Unit) {
        if (!visible) {
            delay((step * stepMillis).toLong())
            visible = true
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "appear"
    )
    return this.graphicsLayer {
        alpha = progress.coerceIn(0f, 1f)
        translationY = (1f - progress) * 40.dp.toPx()
        val s = 0.96f + 0.04f * progress
        scaleX = s
        scaleY = s
    }
}

/**
 * Damped horizontal shake, restarted whenever [trigger] changes (non-zero).
 * Use to flag invalid input.
 */
@Composable
fun Modifier.shakeOnError(trigger: Int): Modifier {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            val amplitudes = listOf(22f, -18f, 14f, -10f, 6f, -3f, 0f)
            for (a in amplitudes) offset.animateTo(a, tween(42, easing = LinearEasing))
        }
    }
    return this.graphicsLayer { translationX = offset.value }
}

/* ------------------------------------------------------------------ *
 *  Value animation                                                   *
 * ------------------------------------------------------------------ */

private val DoubleConverter = TwoWayConverter<Double, AnimationVector1D>(
    convertToVector = { AnimationVector1D(it.toFloat()) },
    convertFromVector = { it.value.toDouble() }
)

/**
 * Counts up from zero the first time it's shown, then glides between values.
 * Returns the current animated value.
 */
@Composable
fun animatedDouble(value: Double, durationMillis: Int = 1100): Double {
    val anim = remember { Animatable(0.0, DoubleConverter) }
    LaunchedEffect(value) {
        anim.animateTo(value, tween(durationMillis, easing = EmphasizedDecelerate))
    }
    return anim.value
}

/** 0 → 1 progress that plays once on first composition (charts, rings). */
@Composable
fun rememberRevealProgress(durationMillis: Int = 1200, delayMillis: Int = 0, key: Any? = Unit): Float {
    val anim = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        delay(delayMillis.toLong())
        anim.animateTo(1f, tween(durationMillis, easing = EmphasizedDecelerate))
    }
    return anim.value
}

/* ------------------------------------------------------------------ *
 *  Ambient loops                                                     *
 * ------------------------------------------------------------------ */

/** Slow up/down float, for illustrations and decorative hexagons. */
@Composable
fun Modifier.floating(amplitudeDp: Float = 6f, periodMillis: Int = 3200, phase: Float = 0f): Modifier {
    val transition = rememberInfiniteTransition(label = "float")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "floatT"
    )
    return this.graphicsLayer {
        val angle = (t + phase) * 2f * Math.PI.toFloat()
        translationY = kotlin.math.sin(angle) * amplitudeDp.dp.toPx()
        rotationZ = kotlin.math.cos(angle) * 3f
    }
}

/** Gentle breathing scale for attention-grabbing CTAs. */
@Composable
fun Modifier.breathing(min: Float = 0.97f, max: Float = 1.03f, periodMillis: Int = 1800): Modifier {
    val transition = rememberInfiniteTransition(label = "breath")
    val s by transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathS"
    )
    return this.graphicsLayer {
        scaleX = s
        scaleY = s
    }
}

/** Sweeping highlight for CTAs and skeletons. */
@Composable
fun Modifier.shimmer(
    highlight: Color = Color(0x40FFFFFF),
    periodMillis: Int = 2400
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    return this.drawWithContent {
        drawContent()
        val sweep = size.width * 0.6f
        val start = -sweep + progress * (size.width + sweep) * 1.6f
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, highlight, Color.Transparent),
                start = Offset(start, 0f),
                end = Offset(start + sweep, size.height)
            )
        )
    }
}
