package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

data class DiviNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isPrimary: Boolean = false
)

/**
 * Floating pill navigation. The selected tab expands into a tinted capsule
 * with its label; the primary "+" is a raised gradient button that rotates
 * into an "×" while its sheet is open.
 */
@Composable
fun DiviFloatingNav(
    items: List<DiviNavItem>,
    currentRoute: String,
    onSelect: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    primaryOpen: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = modifier
            .floatingShadow(DiviPillShape, elevation = 24.dp, color = if (palette.isDark) Color(0x99000000) else Color(0x3010231F))
            .clip(DiviPillShape)
            .background(palette.nav)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEach { item ->
            if (item.isPrimary) {
                PrimaryNavButton(icon = item.icon, label = item.label, open = primaryOpen, onClick = onPrimaryAction)
            } else {
                NavButton(
                    icon = item.icon,
                    label = item.label,
                    selected = currentRoute == item.route,
                    onClick = { onSelect(item.route) }
                )
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalDiviPalette.current
    val bg by animateColorAsState(if (selected) palette.accentSoft else Color.Transparent, label = "navBg")
    val tint by animateColorAsState(
        if (selected) palette.accentDeep.takeIf { !palette.isDark } ?: palette.accent
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navTint"
    )
    val pop = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            pop.snapTo(0.6f)
            pop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        }
    }
    Row(
        modifier = Modifier
            .height(46.dp)
            .bounceClick(pressedScale = 0.88f, feedback = { it.selection() }, onClick = onClick)
            .clip(DiviPillShape)
            .background(bg)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(23.dp)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = tint,
                maxLines = 1,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun PrimaryNavButton(
    icon: ImageVector,
    label: String,
    open: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalDiviPalette.current
    val rotation by animateFloatAsState(
        if (open) 135f else 0f,
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "fabRotation"
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(54.dp)
            .bounceClick(pressedScale = 0.85f, onClick = onClick)
            .floatingShadow(CircleShape, 12.dp, palette.shadow)
            .graphicsLayer { rotationZ = rotation / 3f }
            .clip(Cookie)
            .background(Brush.linearGradient(listOf(palette.heroStart, palette.heroEnd))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}
