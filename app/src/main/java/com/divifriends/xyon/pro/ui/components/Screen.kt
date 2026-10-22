package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.floating
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

/**
 * The app's page layout: an animated aurora hero that parallaxes, shrinks
 * towards the top-left and fades as you scroll, and a rounded sheet sliding
 * over it. The compact title bar is driven by the scroll itself: its title
 * grows in from a larger size as the hero title shrinks away, with a light
 * tick the moment the bar locks in. [mood] (−1…1) tints the aurora: lime when
 * you're owed money, orange when you owe.
 */
@Composable
fun HeroScaffold(
    modifier: Modifier = Modifier,
    compactTitle: String? = null,
    onBack: (() -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    bottomPadding: Dp = 120.dp,
    mood: Float = 0f,
    hero: @Composable ColumnScope.() -> Unit,
    sheet: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalDiviPalette.current
    val density = LocalDensity.current
    var heroHeight by remember { mutableIntStateOf(1) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val collapseStart = with(density) { 170.dp.toPx() }
    val collapseRange = with(density) { 80.dp.toPx() }
    // 0 → hero fully shown, 1 → compact bar fully in. Read in draw lambdas only.
    val barProgress: () -> Float = {
        ((scrollState.value - (heroHeight - collapseStart)) / collapseRange).coerceIn(0f, 1f)
    }
    val barVisible by remember { derivedStateOf { barProgress() > 0f } }
    val collapsed by remember { derivedStateOf { barProgress() >= 1f } }
    val haptics = LocalHaptics.current
    LaunchedEffect(collapsed) { if (collapsed) haptics?.tick() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.sheet)
    ) {
        HeroGradient(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { heroHeight.toDp() } + 80.dp)
                .graphicsLayer { translationY = -scrollState.value * 0.5f },
            mood = mood
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { heroHeight = it.height.coerceAtLeast(1) }
                    .graphicsLayer {
                        val s = scrollState.value.toFloat()
                        val f = (s / heroHeight).coerceIn(0f, 1f)
                        translationY = s * 0.45f
                        alpha = (1f - f * 1.3f).coerceIn(0f, 1f)
                        // Shrink towards the top-left, where the compact title lands.
                        transformOrigin = TransformOrigin(0f, 0f)
                        val scale = 1f - 0.18f * f
                        scaleX = scale
                        scaleY = scale
                    }
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = 8.dp, bottom = 56.dp),
                content = hero
            )
            Column(
                modifier = Modifier
                    .offset(y = (-30).dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = screenHeight * 0.7f)
                    .clip(DiviSheetShape)
                    .background(palette.sheet)
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .padding(bottom = bottomPadding)
            ) {
                SheetHandle(Modifier.align(Alignment.CenterHorizontally))
                sheet()
            }
        }

        if (barVisible && compactTitle != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = barProgress() }
            ) {
                HeroGradient(Modifier.matchParentSize(), animated = false, mood = mood)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (onBack != null) HeroIconButton(Icons.Rounded.ArrowBackIosNew, onBack, size = 36.dp, contentDescription = "Atrás")
                    Text(
                        text = compactTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.onHero,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            val p = barProgress()
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            val scale = 1.45f - 0.45f * p
                            scaleX = scale
                            scaleY = scale
                            translationY = (1f - p) * 22.dp.toPx()
                        }
                    )
                }
            }
        }
    }
}

/** Lets a child ignore its parent's horizontal padding (edge-to-edge carousels). */
fun Modifier.bleed(horizontal: Dp): Modifier = this.layout { measurable, constraints ->
    val extra = (horizontal * 2).roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minWidth + extra,
            maxWidth = constraints.maxWidth + extra
        )
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-horizontal.roundToPx(), 0)
    }
}

@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 6.dp)
            .width(42.dp)
            .height(5.dp)
            .clip(DiviPillShape)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (trailing != null) {
            trailing()
        } else if (actionLabel != null && onAction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .bounceClick(feedback = { it.selection() }, onClick = onAction)
                    .clip(DiviPillShape)
                    .background(LocalDiviPalette.current.accentSoft)
                    .padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = LocalDiviPalette.current.accentDeep
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = LocalDiviPalette.current.accentDeep,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** Floating hexagon illustration + copy for empty lists. */
@Composable
fun EmptyState(
    emoji: String,
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    val palette = LocalDiviPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.floating()) {
            Box(
                Modifier
                    .size(104.dp)
                    .clip(Cookie)
                    .background(palette.lime)
            )
            Box(
                Modifier
                    .size(70.dp)
                    .clip(Hexagon)
                    .background(palette.blush),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            }
        }
        Spacer(Modifier.height(4.dp))
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (action != null) {
            Spacer(Modifier.height(6.dp))
            action()
        }
    }
}

@Composable
fun DiviDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

/** Small icon inside a tinted hexagon, used by rows and tiles. */
@Composable
fun HexBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    tone: Tone = Tone.Lime
) {
    val palette = LocalDiviPalette.current
    Box(
        modifier = modifier
            .size(size)
            .clip(Hexagon)
            .background(palette.container(tone)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.content(tone, MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.size(size * 0.46f)
        )
    }
}
