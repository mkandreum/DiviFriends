package com.divifriends.xyon.pro.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import com.divifriends.xyon.pro.ui.motion.EmphasizedDecelerate
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

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

/** Maps a balance to an aurora mood: about ±1 at ±50 €, eased in between. */
fun moodOf(balance: Double): Float = tanh(balance / 50.0).toFloat()

/**
 * Near-black hero with three "aurora" blobs (lime, violet, cyan) flowing on
 * slow Lissajous paths at different speeds, so the light never repeats the
 * same shape twice in a row.
 *
 * It reacts to the screen: [mood] (−1…1) grows the lime blob when you are
 * owed money and turns it orange when you owe, flowing to each new value with
 * a soft pulse; tilting the phone drifts the blobs like light under glass.
 */
@Composable
fun HeroGradient(modifier: Modifier = Modifier, animated: Boolean = true, mood: Float = 0f) {
    val palette = LocalDiviPalette.current
    val flowingMood by animateFloatAsState(
        targetValue = mood.coerceIn(-1f, 1f),
        animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessVeryLow),
        label = "auroraMood"
    )
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(mood) {
        pulse.snapTo(1f)
        pulse.animateTo(0f, tween(1400, easing = EmphasizedDecelerate))
    }
    val tilt = if (animated) rememberTilt() else null
    val owe = (-flowingMood).coerceAtLeast(0f)
    val owed = flowingMood.coerceAtLeast(0f)
    val mainGlow = lerp(palette.heroGlow, palette.negative.copy(alpha = palette.heroGlow.alpha), owe)
    val transition = rememberInfiniteTransition(label = "aurora")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
        label = "auroraT"
    )
    val phase = if (animated) t * 2f * Math.PI.toFloat() else 0f

    Canvas(modifier = modifier) {
        val drift = tilt?.value ?: Offset.Zero
        val dx = drift.x * size.width * 0.06f
        val dy = drift.y * size.height * 0.06f
        val boost = 1f + 0.12f * pulse.value
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.heroStart, palette.heroEnd),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.9f, size.height * 1.1f)
            )
        )
        // Back layers move less with tilt than the front one: cheap parallax.
        auroraBlob(
            color = palette.heroGlow2.copy(alpha = palette.heroGlow2.alpha * (1f - 0.35f * owed)),
            center = Offset(
                size.width * (0.82f + 0.16f * cos(phase)) - dx * 0.5f,
                size.height * (0.12f + 0.14f * sin(phase * 2f)) - dy * 0.5f
            ),
            radius = size.width * (0.78f + 0.08f * sin(phase * 3f))
        )
        auroraBlob(
            color = palette.heroGlow3.copy(alpha = palette.heroGlow3.alpha * (1f - 0.4f * (owe + owed))),
            center = Offset(
                size.width * (0.12f + 0.18f * sin(phase * 2f)) + dx * 0.7f,
                size.height * (0.78f + 0.12f * cos(phase)) + dy * 0.7f
            ),
            radius = size.width * (0.66f + 0.06f * cos(phase * 2f))
        )
        auroraBlob(
            color = mainGlow.copy(alpha = (mainGlow.alpha * (1f + 0.5f * (owe + owed))).coerceAtMost(1f)),
            center = Offset(
                size.width * (0.5f + 0.32f * sin(phase)) + dx,
                size.height * (0.5f + 0.22f * cos(phase * 3f)) + dy
            ),
            radius = size.width * (0.5f + 0.06f * sin(phase * 4f) + 0.18f * (owe + owed)) * boost
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

/**
 * Smoothed device tilt in −1…1 per axis (gravity sensor, accelerometer as a
 * fallback). Devices without either sensor just report zero.
 */
@Composable
private fun rememberTilt(): State<Offset> {
    val context = LocalContext.current
    val tilt = remember { mutableStateOf(Offset.Zero) }
    DisposableEffect(context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = (-event.values[0] / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
                val y = ((event.values[1] / SensorManager.GRAVITY_EARTH) - 0.6f).coerceIn(-1f, 1f)
                val old = tilt.value
                // Low-pass filter so the light glides instead of jittering.
                tilt.value = Offset(old.x + (x - old.x) * 0.08f, old.y + (y - old.y) * 0.08f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sensor != null) manager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { manager?.unregisterListener(listener) }
    }
    return tilt
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
