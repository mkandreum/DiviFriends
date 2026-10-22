package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.launch

/** Vertical peek of each card behind the front one. */
private val PeekStep = 12.dp

/**
 * A 3D stack of cards in depth. Fling the front card sideways (or drag it past
 * a third of its width) and it slides out, drops to the back of the stack and
 * slips in behind the others while every other card steps forward on a spring.
 * A tick marks the moment a drag will commit, a click when it does.
 *
 * [card] gets the width the stack can give it and whether it is the front one.
 */
@Composable
fun <T> CardStack(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    card: @Composable (item: T, front: Boolean, width: Dp) -> Unit
) {
    val keys = items.map(key)
    var order by remember(keys) { mutableStateOf(items.indices.toList()) }
    val haptics = LocalHaptics.current

    BoxWithConstraints(modifier.padding(top = PeekStep * (maxVisible - 1).coerceAtMost(items.size - 1).coerceAtLeast(0))) {
        val width = maxWidth
        order.forEachIndexed { depth, itemIndex ->
            val item = items.getOrNull(itemIndex) ?: return@forEachIndexed
            key(key(item)) {
                StackLayer(
                    depth = depth,
                    maxVisible = maxVisible,
                    enabled = depth == 0 && items.size > 1,
                    onCommit = {
                        order = order.drop(1) + order.first()
                        haptics?.threshold()
                    },
                    onArmed = { haptics?.tick() }
                ) {
                    card(item, depth == 0, width)
                }
            }
        }
    }
}

@Composable
private fun StackLayer(
    depth: Int,
    maxVisible: Int,
    enabled: Boolean,
    onCommit: () -> Unit,
    onArmed: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val drag = remember { Animatable(0f) }
    var width by remember { mutableStateOf(1f) }
    var armed by remember { mutableStateOf(false) }
    val animatedDepth by animateFloatAsState(
        targetValue = depth.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "stackDepth"
    )

    val gestures = if (!enabled) Modifier else Modifier.pointerInput(Unit) {
        val tracker = VelocityTracker()
        detectHorizontalDragGestures(
            onDragStart = { tracker.resetTracking() },
            onHorizontalDrag = { change, amount ->
                tracker.addPosition(change.uptimeMillis, change.position)
                change.consume()
                scope.launch { drag.snapTo(drag.value + amount) }
                val nowArmed = abs(drag.value) > width * 0.3f
                if (nowArmed && !armed) onArmed()
                armed = nowArmed
            },
            onDragEnd = {
                val velocity = tracker.calculateVelocity().x
                val flung = abs(velocity) > with(density) { 900.dp.toPx() }
                val direction = if (drag.value != 0f) sign(drag.value) else sign(velocity)
                armed = false
                scope.launch {
                    if ((abs(drag.value) > width * 0.3f || flung) && direction != 0f) {
                        drag.animateTo(direction * width * 1.1f, tween(170))
                        onCommit()
                        // Now at the back: slide in underneath the stack.
                        drag.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
                    } else {
                        drag.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
                    }
                }
            },
            onDragCancel = {
                armed = false
                scope.launch { drag.animateTo(0f, spring(dampingRatio = 0.55f)) }
            }
        )
    }

    Box(
        modifier = Modifier
            .zIndex(-depth.toFloat())
            .graphicsLayer {
                width = size.width.coerceAtLeast(1f)
                val d = animatedDepth
                transformOrigin = TransformOrigin(0.5f, 0f)
                translationY = -d * PeekStep.toPx()
                translationX = drag.value
                rotationZ = drag.value / size.width.coerceAtLeast(1f) * 10f
                val scale = 1f - 0.07f * d
                scaleX = scale
                scaleY = scale
                alpha = if (d > maxVisible - 0.5f) 0f else (1f - 0.28f * d).coerceIn(0f, 1f)
            }
            .then(gestures)
    ) {
        content()
    }
}
