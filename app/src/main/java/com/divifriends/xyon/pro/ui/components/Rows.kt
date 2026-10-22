package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.Tabular
import com.divifriends.xyon.pro.ui.theme.mono

/**
 * History row (reference: "Philip Lahm · Transfer · −$300"): hex avatar or
 * emoji badge, two lines of text, amount + optional caption on the right.
 */
@Composable
fun TransactionRow(
    name: String,
    title: String,
    subtitle: String,
    amount: String,
    amountColor: Color,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    amountCaption: String? = null,
    background: Color = Color.Transparent,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.bounceClick(pressedScale = 0.97f, feedback = { it.selection() }, onClick = onClick) else Modifier)
            .clip(DiviInnerShape)
            .background(background)
            .padding(vertical = 10.dp, horizontal = if (background == Color.Transparent) 0.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Avatar(name = name, emoji = emoji, size = 46.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall.mono(),
                color = amountColor,
                maxLines = 1
            )
            if (amountCaption != null) {
                Text(
                    text = amountCaption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Card used by the horizontal "Mis grupos" carousel (reference: the yellow
 * "−$300 Transfer" and white "+$1200 Master Card" cards).
 */
@Composable
fun GroupCard(
    name: String,
    amount: String,
    caption: String,
    emoji: String,
    tone: Tone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 148.dp
) {
    val palette = LocalDiviPalette.current
    val fg = palette.content(tone, MaterialTheme.colorScheme.onSurface)
    Column(
        modifier = modifier
            .width(width)
            .bounceClick(pressedScale = 0.94f, feedback = { it.selection() }, onClick = onClick)
            .floatingShadow(DiviCardShape, 10.dp, Color(0x22000000))
            .clip(DiviCardShape)
            .background(palette.container(tone))
            .padding(16.dp)
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(DiviSmallShape)
                .background(fg.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge.merge(Tabular),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            color = fg.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
