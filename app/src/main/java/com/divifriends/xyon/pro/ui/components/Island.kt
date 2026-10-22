package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlinx.coroutines.delay

/** What the island is showing; [key] restarts its expand → collapse cycle. */
data class IslandActivity(
    val key: Any,
    val icon: ImageVector,
    /** Short text for the collapsed pill. */
    val label: String,
    val title: String,
    val message: String,
    val accent: Color,
    /** Pulsing dot while something is in progress. */
    val live: Boolean = true,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/**
 * Dynamic-island style live activity under the status bar (NFC sharing,
 * waiting for an invitation, success). It drops in expanded, folds into a
 * small black pill after a few seconds so the app stays usable, and grows
 * back into the full activity on one spring when tapped.
 */
@Composable
fun DiviIsland(activity: IslandActivity?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = activity != null,
        enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.3f) + fadeIn(),
        exit = scaleOut(tween(220), targetScale = 0.3f) + fadeOut(tween(180)),
        modifier = modifier
    ) {
        // Keep showing the last activity while the exit animation plays.
        var shown by remember { mutableStateOf(activity) }
        if (activity != null) shown = activity
        shown?.let { Island(it) }
    }
}

@Composable
private fun Island(activity: IslandActivity) {
    val palette = LocalDiviPalette.current
    var expanded by remember(activity.key) { mutableStateOf(true) }
    LaunchedEffect(activity.key, expanded) {
        if (expanded) {
            delay(4200)
            expanded = false
        }
    }
    val radius by animateDpAsState(
        targetValue = if (expanded) 34.dp else 22.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "islandRadius"
    )
    val shape = RoundedCornerShape(radius)

    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .bounceClick(
                    pressedScale = 0.96f,
                    feedback = { if (expanded) it.selection() else it.swell() },
                    onClick = { expanded = !expanded }
                )
                .clip(shape)
                .background(Color.Black)
                .border(1.dp, palette.heroChip, shape)
                .animateContentSize(spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow))
        ) {
            AnimatedContent(
                targetState = expanded,
                transitionSpec = {
                    (fadeIn(tween(220, delayMillis = 90)) + scaleIn(tween(260), initialScale = 0.92f))
                        .togetherWith(fadeOut(tween(90)))
                },
                label = "island"
            ) { open ->
                if (open) IslandExpanded(activity) else IslandPill(activity)
            }
        }
    }
}

@Composable
private fun IslandPill(activity: IslandActivity) {
    Row(
        modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IslandIcon(activity, size = 28)
        Text(
            text = activity.label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (activity.live) LiveDot(activity.accent)
    }
}

@Composable
private fun IslandExpanded(activity: IslandActivity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (activity.live) {
            PulseRings(icon = activity.icon, size = 120.dp)
        } else {
            IslandIcon(activity, size = 56)
        }
        Text(
            text = activity.title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = activity.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        if (activity.actionLabel != null && activity.onAction != null) {
            SoftButton(text = activity.actionLabel, onClick = activity.onAction, tone = Tone.Raised)
        }
    }
}

@Composable
private fun IslandIcon(activity: IslandActivity, size: Int) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(activity.accent),
        contentAlignment = Alignment.Center
    ) {
        Icon(activity.icon, null, tint = Color.Black, modifier = Modifier.size((size * 0.55f).dp))
    }
}

@Composable
private fun LiveDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "liveDot")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "liveDotAlpha"
    )
    Box(
        Modifier
            .size(8.dp)
            .graphicsLayer {
                alpha = pulse
                scaleX = 0.8f + 0.2f * pulse
                scaleY = 0.8f + 0.2f * pulse
            }
            .clip(CircleShape)
            .background(color)
    )
}
