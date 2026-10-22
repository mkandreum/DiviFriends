package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.ES_LOCALE
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.theme.DiviPalette
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val heroDateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", ES_LOCALE)

fun todayLabel(): String = LocalDate.now().format(heroDateFormatter)
    .replaceFirstChar { it.titlecase(ES_LOCALE) }

/** Translucent white hexagon button that lives on the hero gradient. */
@Composable
fun HeroIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    contentDescription: String? = null,
    solid: Boolean = false,
    badge: Boolean = false
) {
    val palette = LocalDiviPalette.current
    Box(
        modifier = modifier
            .size(size)
            .bounceClick(pressedScale = 0.86f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(Hexagon)
                .background(if (solid) Color.White else palette.heroChip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (solid) palette.accentDeep else palette.onHero,
                modifier = Modifier.size(size * 0.42f)
            )
        }
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = size * 0.08f, end = size * 0.1f)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(palette.sun)
            )
        }
    }
}

/**
 * Hero header: small caption (date) + bold title on the left, optional
 * leading back button and trailing action.
 */
@Composable
fun HeroTopBar(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    actionDescription: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineMedium
) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            HeroIconButton(
                icon = Icons.Rounded.ArrowBackIosNew,
                onClick = onBack,
                size = 42.dp,
                contentDescription = "Atrás"
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.onHeroMuted
                )
            }
            Text(
                text = title,
                style = titleStyle,
                color = palette.onHero,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (actionIcon != null && onAction != null) {
            HeroIconButton(icon = actionIcon, onClick = onAction, contentDescription = actionDescription)
        }
    }
}

/** Initials for a display name: "Philip Lahm" → "PL", "ana" → "AN". */
fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

/** Stable pastel tone per name, so every friend keeps "their" color. */
fun DiviPalette.avatarColors(name: String): Pair<Color, Color> {
    val options = listOf(lime to onLime, blush to onBlush, sun to onSun, ink to onInk)
    return options[(name.hashCode() and 0x7fffffff) % options.size]
}

/** Hexagonal initials avatar (the reference's "PL" / "AA" badges). */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shape: Shape = Hexagon,
    emoji: String? = null,
    highlight: Boolean = false
) {
    val palette = LocalDiviPalette.current
    val (bg, fg) = if (highlight) palette.accent to palette.onAccent else palette.avatarColors(name)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = TextUnit(size.value * 0.42f, TextUnitType.Sp))
        } else {
            Text(
                text = initialsOf(name),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(size.value * 0.32f, TextUnitType.Sp)
                ),
                color = fg
            )
        }
    }
}

/**
 * Overlapping round avatars + a "+N" bubble, as in "Friends Connected".
 * [ringColor] should match whatever is behind the stack.
 */
@Composable
fun AvatarStack(
    names: List<String>,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    max: Int = 3,
    ringColor: Color = LocalDiviPalette.current.sheet
) {
    val palette = LocalDiviPalette.current
    val shown = names.take(max)
    val extra = names.size - shown.size
    val step = size * 0.68f
    val count = shown.size + if (extra > 0) 1 else 0
    Box(modifier = modifier.size(width = step * (count - 1).coerceAtLeast(0) + size, height = size)) {
        shown.forEachIndexed { index, name ->
            Avatar(
                name = name,
                size = size,
                shape = CircleShape,
                modifier = Modifier
                    .offset(x = step * index)
                    .border(2.5.dp, ringColor, CircleShape)
            )
        }
        if (extra > 0) {
            Box(
                modifier = Modifier
                    .offset(x = step * shown.size)
                    .size(size)
                    .border(2.5.dp, ringColor, CircleShape)
                    .clip(CircleShape)
                    .background(palette.accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$extra+",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.onAccent
                )
            }
        }
    }
}
