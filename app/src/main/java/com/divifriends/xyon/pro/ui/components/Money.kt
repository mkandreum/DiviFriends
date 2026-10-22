package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.motion.animatedDouble
import com.divifriends.xyon.pro.ui.theme.Tabular
import kotlin.math.abs

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
 * Big hero amount with small cents, counting up from zero on first show and
 * gliding to new values afterwards.
 */
@Composable
fun AnimatedAmount(
    value: Double,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    signed: Boolean = false
) {
    val animated = animatedDouble(value)
    val sign = when {
        !signed -> ""
        animated > 0.005 -> "+"
        animated < -0.005 -> "−"
        else -> ""
    }
    val (main, tail) = splitAmount(if (signed) abs(animated) else animated)
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = sign + main,
            style = style.merge(Tabular),
            color = color,
            maxLines = 1
        )
        Text(
            text = tail,
            style = style.merge(Tabular).copy(
                fontSize = style.fontSize * 0.45f,
                fontWeight = FontWeight.SemiBold
            ),
            color = color.copy(alpha = 0.85f),
            maxLines = 1,
            modifier = Modifier.padding(bottom = 6.dp, start = 1.dp)
        )
    }
}

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
        style = style.merge(Tabular),
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}
