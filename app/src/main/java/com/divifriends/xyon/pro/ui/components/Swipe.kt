package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import kotlinx.coroutines.launch
import kotlin.math.abs

data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val contentColor: Color,
    val onTrigger: () -> Unit
)

/**
 * Row that can be dragged sideways to reveal an action. Crossing the commit
 * threshold "clicks" (haptic + icon pop); releasing past it triggers.
 * [startAction] is revealed when dragging right, [endAction] when dragging left.
 */
@Composable
fun SwipeRow(
    modifier: Modifier = Modifier,
    shape: Shape = DiviCardShape,
    startAction: SwipeAction? = null,
    endAction: SwipeAction? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptics = LocalHaptics.current
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    var width by remember { mutableIntStateOf(1) }
    var armed by remember { mutableStateOf(false) }
    val start by rememberUpdatedState(startAction)
    val end by rememberUpdatedState(endAction)
    val threshold = 0.32f

    val active = if (offset.value > 0) startAction else endAction
    val progress = (abs(offset.value) / (width * threshold)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { width = it.width.coerceAtLeast(1) }
            .clip(shape)
    ) {
        if (active != null && offset.value != 0f) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(active.color.copy(alpha = 0.35f + 0.65f * progress))
                    .padding(horizontal = 22.dp),
                horizontalArrangement = if (offset.value > 0) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pop = if (armed) 1.15f else 0.6f + 0.4f * progress
                Icon(
                    active.icon,
                    contentDescription = null,
                    tint = active.contentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = pop
                            scaleY = pop
                        }
                )
                Text(
                    text = active.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = active.contentColor.copy(alpha = progress),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .graphicsLayer { translationX = offset.value }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val trigger = if (armed) (if (offset.value > 0) start else end) else null
                            armed = false
                            trigger?.let {
                                haptics?.success()
                                it.onTrigger()
                            }
                            scope.launch {
                                offset.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                        onDragCancel = {
                            armed = false
                            scope.launch { offset.animateTo(0f, spring()) }
                        }
                    ) { change, delta ->
                        val raw = offset.value + delta
                        val allowed = when {
                            raw > 0 && start == null -> 0f
                            raw < 0 && end == null -> 0f
                            else -> raw
                        }
                        val limit = size.width * 0.6f
                        val damped = if (abs(allowed) > limit) {
                            val over = abs(allowed) - limit
                            (limit + over * 0.25f) * if (allowed > 0) 1 else -1
                        } else allowed
                        if (damped != offset.value) change.consume()
                        scope.launch { offset.snapTo(damped) }
                        val nowArmed = abs(damped) > size.width * threshold
                        if (nowArmed != armed) {
                            armed = nowArmed
                            if (nowArmed) haptics?.threshold() else haptics?.selection()
                        }
                    }
                },
            content = content
        )
    }
}
