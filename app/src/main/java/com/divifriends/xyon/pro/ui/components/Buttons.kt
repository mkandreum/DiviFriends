package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

enum class ButtonTone { Default, Danger, Safe }

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalDiviPalette.current
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.primary.copy(alpha = if (enabled) 1f else 0.4f),
                        palette.secondary.copy(alpha = if (enabled) 1f else 0.4f)
                    )
                )
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF111111),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Default,
    enabled: Boolean = true
) {
    val palette = LocalDiviPalette.current
    val contentColor = when (tone) {
        ButtonTone.Default -> MaterialTheme.colorScheme.onSurface
        ButtonTone.Danger -> palette.danger
        ButtonTone.Safe -> palette.secondary
    }.copy(alpha = if (enabled) 1f else 0.4f)
    val borderColor = when (tone) {
        ButtonTone.Default -> palette.border
        ButtonTone.Danger -> palette.danger.copy(alpha = if (enabled) 1f else 0.4f)
        ButtonTone.Safe -> palette.secondary.copy(alpha = if (enabled) 1f else 0.4f)
    }
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glass(shape = shape, borderColor = borderColor, elevation = 4.dp)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
