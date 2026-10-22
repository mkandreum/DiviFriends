package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.animatedDouble
import com.divifriends.xyon.pro.ui.theme.Tabular
import com.divifriends.xyon.pro.ui.theme.mono
import kotlin.math.abs
import kotlin.math.floor
import kotlinx.coroutines.delay

/** "24.450,00 €" → ("24.450", ",00 €"). */
fun splitAmount(value: Double): Pair<String, String> {
    val text = formatCurrency(value)
    val idx = text.lastIndexOf(',')
    return if (idx < 0) text to "" else text.substring(0, idx) to text.substring(idx)
}

/** "+12,00 €" / "−12,00 €" (true minus sign), or plain when zero. */
fun signedCurrency(value: Double): String = when {
    value > 0.005 -> "+" + formatCurrency(value)
    value < -0.005 -> "−" + formatCurrency(abs(value))
    else -> formatCurrency(0.0)
}

/**
 * Big hero amount with small cents. Every digit is its own odometer wheel:
 * wheels roll (up when the value grows, down when it shrinks) and settle one
 * after another from right to left with a springy overshoot, each landing
 * with a light haptic tick that spaces out like a slot machine slowing down.
 */
@Composable
fun AnimatedAmount(
    value: Double,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    signed: Boolean = false,
    haptic: Boolean = true
) {
    val haptics = LocalHaptics.current
    val sign = when {
        !signed -> ""
        value > 0.005 -> "+"
        value < -0.005 -> "−"
        else -> ""
    }
    val (main, tail) = splitAmount(if (signed) abs(value) else value)
    var previous by remember { mutableDoubleStateOf(0.0) }
    val rollUp = value >= previous
    SideEffect { previous = value }
    val onSettled: (() -> Unit)? = if (haptic && haptics != null) ({ haptics.tick() }) else null

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        RollingText(
            text = sign + main,
            style = style.merge(Tabular),
            color = color,
            rollUp = rollUp,
            delayOffset = tail.count { it.isDigit() },
            onDigitSettled = onSettled,
            modifier = Modifier.alignByBaseline()
        )
        RollingText(
            text = tail,
            style = style.merge(Tabular).copy(
                fontSize = style.fontSize * 0.42f,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            ),
            color = color.copy(alpha = 0.6f),
            rollUp = rollUp,
            onDigitSettled = onSettled,
            modifier = Modifier
                .alignByBaseline()
                .padding(start = 2.dp)
        )
    }
}

/**
 * Text whose digits roll like odometer wheels. Characters are keyed by their
 * position from the right, so "999" → "1.000" keeps the units wheel in place
 * and only grows new wheels on the left.
 */
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    rollUp: Boolean = true,
    delayOffset: Int = 0,
    onDigitSettled: (() -> Unit)? = null
) {
    Row(modifier = modifier) {
        var digitIndex = text.count { it.isDigit() } + delayOffset
        text.forEachIndexed { index, char ->
            key(text.length - index) {
                if (char.isDigit()) {
                    digitIndex--
                    RollingDigit(
                        digit = char - '0',
                        style = style,
                        color = color,
                        rollUp = rollUp,
                        order = digitIndex,
                        onSettled = onDigitSettled
                    )
                } else {
                    Text(text = char.toString(), style = style, color = color, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun RollingDigit(
    digit: Int,
    style: TextStyle,
    color: Color,
    rollUp: Boolean,
    order: Int,
    onSettled: (() -> Unit)?
) {
    // Continuous wheel position: the shown digit is position mod 10.
    val wheel = remember { Animatable(0f) }
    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(digit) {
        val current = wheel.value
        val base = floor(current)
        val currentDigit = base.toInt().mod(10)
        var target = base + (digit - currentDigit)
        if (rollUp && target < current - 0.001f) target += 10f
        if (!rollUp && target > current + 0.001f) target -= 10f
        // First appearance: every wheel spins one full turn before landing.
        if (firstRun) {
            target += 10f
            firstRun = false
        }
        if (abs(target - current) < 0.001f) return@LaunchedEffect
        // Right-most wheels land first; gaps grow so the ticks slow down.
        delay((order * 45L + order * order * 9L).coerceAtMost(600L))
        wheel.animateTo(target, spring(dampingRatio = 0.62f, stiffness = 120f))
        onSettled?.invoke()
    }

    Box(Modifier.clipToBounds()) {
        // Invisible "0" fixes the wheel size; tabular figures share one width.
        Text(text = "0", style = style, color = Color.Transparent, maxLines = 1)
        Text(
            text = wheelDigit(wheel.value, 0).toString(),
            style = style,
            color = color,
            maxLines = 1,
            modifier = Modifier.graphicsLayer {
                val frac = wheel.value - floor(wheel.value)
                translationY = -frac * size.height
                alpha = 1f - frac * 0.6f
            }
        )
        Text(
            text = wheelDigit(wheel.value, 1).toString(),
            style = style,
            color = color,
            maxLines = 1,
            modifier = Modifier.graphicsLayer {
                val frac = wheel.value - floor(wheel.value)
                translationY = (1f - frac) * size.height
                alpha = 0.4f + frac * 0.6f
            }
        )
    }
}

private fun wheelDigit(position: Float, ahead: Int): Int = (floor(position).toInt() + ahead).mod(10)

/** Small counted-up amount for cards and tiles. */
@Composable
fun AnimatedMoneyText(
    value: Double,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    signed: Boolean = false
) {
    val animated = animatedDouble(value, durationMillis = 900)
    Text(
        text = if (signed) signedCurrency(animated) else formatCurrency(animated),
        style = style.mono(),
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}
