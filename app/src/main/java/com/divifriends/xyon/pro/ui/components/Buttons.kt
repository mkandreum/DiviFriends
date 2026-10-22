package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.floating
import com.divifriends.xyon.pro.ui.motion.shimmer
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

/** Click with no ripple and no haptics (for purely decorative hit areas). */
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** Gradient pill CTA with a slow light sweep. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .bounceClick(enabled = enabled, pressedScale = 0.97f, onClick = onClick)
            .floatingShadow(DiviPillShape, elevation = if (enabled) 14.dp else 0.dp, color = palette.shadow)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(DiviPillShape)
            .background(Brush.horizontalGradient(listOf(palette.heroStart, palette.heroEnd)))
            .then(if (enabled) Modifier.shimmer() else Modifier)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

/** Tonal pill button. */
@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Raised,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    heavy: Boolean = false
) {
    val palette = LocalDiviPalette.current
    val container = palette.container(tone)
    val content = palette.content(tone, MaterialTheme.colorScheme.onSurface)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .bounceClick(
                enabled = enabled,
                pressedScale = 0.97f,
                feedback = { if (heavy) it.heavy() else it.press() },
                onClick = onClick
            )
            .alpha(if (enabled) 1f else 0.45f)
            .clip(DiviPillShape)
            .background(container)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = content, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Translucent chip that sits on the hero ("Mes ⌄", "+2,50 %"…). */
@Composable
fun HeroPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    solid: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val palette = LocalDiviPalette.current
    val bg by animateColorAsState(if (solid) Color.White else palette.heroChip, label = "pillBg")
    val fg = if (solid) palette.accentDeep else palette.onHero
    Row(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.bounceClick(pressedScale = 0.92f, feedback = { it.toggle() }, onClick = onClick)
                else Modifier
            )
            .clip(DiviPillShape)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
        if (trailingIcon != null) Icon(trailingIcon, null, tint = fg, modifier = Modifier.size(16.dp))
    }
}

/**
 * Square-ish pastel tile with a little layered-hexagon illustration, like
 * "Transfer" / "Request" in the reference.
 */
@Composable
fun ActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Mint,
    caption: String? = null,
    phase: Float = 0f
) {
    val palette = LocalDiviPalette.current
    val fg = palette.content(tone, MaterialTheme.colorScheme.onSurface)
    Column(
        modifier = modifier
            .bounceClick(pressedScale = 0.95f, feedback = { it.toggle() }, onClick = onClick)
            .clip(DiviCardShape)
            .background(palette.container(tone))
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TileIllustration(icon = icon, tone = tone, phase = phase)
        Spacer(Modifier.height(14.dp))
        Text(text = label, style = MaterialTheme.typography.titleSmall, color = fg, maxLines = 1)
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = fg.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TileIllustration(icon: ImageVector, tone: Tone, phase: Float = 0f) {
    val palette = LocalDiviPalette.current
    val main = if (tone == Tone.Aqua) palette.accentDeep else palette.accent
    Box(modifier = Modifier.size(78.dp).floating(amplitudeDp = 4f, phase = phase)) {
        Box(
            Modifier
                .offset(x = 4.dp, y = 30.dp)
                .size(26.dp)
                .clip(Hexagon)
                .background(palette.sun)
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .size(54.dp)
                .clip(Hexagon)
                .background(Brush.linearGradient(listOf(palette.heroStart, main))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 6.dp)
                .size(16.dp)
                .clip(Hexagon)
                .background(palette.aqua.copy(alpha = 0.9f))
        )
    }
}

/**
 * Wide row with title, caption and a round accent "+" (the reference's
 * "Top Up Balance" card).
 */
@Composable
fun ActionRow(
    title: String,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Add
) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(pressedScale = 0.97f, onClick = onClick)
            .clip(DiviCardShape)
            .background(palette.raised)
            .padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(caption, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            Modifier
                .floatingShadow(CircleShape, 10.dp, palette.shadow)
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(palette.heroStart, palette.heroEnd))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

/** Round icon button for the sheet (remove participant, etc.). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Raised,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    val palette = LocalDiviPalette.current
    Box(
        modifier = modifier
            .size(40.dp)
            .bounceClick(enabled = enabled, pressedScale = 0.85f, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(palette.container(tone)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.content(tone, MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.size(19.dp)
        )
    }
}
