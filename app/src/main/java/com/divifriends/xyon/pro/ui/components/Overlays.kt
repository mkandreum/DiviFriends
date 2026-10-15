package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.divifriends.xyon.pro.ui.theme.GlassFillStrong
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextPrimary
import com.divifriends.xyon.pro.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun DiviDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp),
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
fun DiviConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DiviDialog(onDismiss = onDismiss) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = com.divifriends.xyon.pro.ui.theme.TextSecondary,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassButton(
                text = cancelText,
                onClick = onDismiss,
                tone = ButtonTone.Danger,
                modifier = Modifier.weight(1f)
            )
            GlassButton(
                text = confirmText,
                onClick = onConfirm,
                tone = ButtonTone.Safe,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DiviToastHost(message: String?, modifier: Modifier = Modifier) {
    val palette = LocalDiviPalette.current
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GlassFillStrong)
                .then(Modifier.glass(shape = RoundedCornerShape(16.dp), borderColor = palette.border, elevation = 8.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text = message ?: "",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val delay: Float,
    val glyph: String
)

/** Celebratory emoji rain shown briefly after creating a group or adding an expense. */
@Composable
fun BoxScope.MoneyConfetti(trigger: Int) {
    if (trigger <= 0) return
    val pieces = remember(trigger) {
        val glyphs = listOf("\uD83D\uDCB5", "\uD83D\uDCB8", "\uD83D\uDCB0", "\uD83E\uDE99")
        List(30) {
            ConfettiPiece(
                x = Math.random().toFloat(),
                delay = Math.random().toFloat(),
                glyph = glyphs.random()
            )
        }
    }
    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 1900, easing = LinearEasing))
    }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
    ) {
        if (boxSize.height == 0) return@Box
        val p = progress.value
        pieces.forEach { piece ->
            val local = ((p - piece.delay * 0.3f) / 0.9f).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val alpha = (1f - local * local).coerceIn(0f, 1f)
            Text(
                text = piece.glyph,
                fontSize = 26.sp,
                color = Color.White.copy(alpha = alpha),
                modifier = Modifier.offset {
                    IntOffset(
                        x = (piece.x * boxSize.width).roundToInt(),
                        y = (local * (boxSize.height + 80) - 40).roundToInt()
                    )
                }
            )
        }
    }
}
