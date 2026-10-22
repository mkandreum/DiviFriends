package com.divifriends.xyon.pro.ui.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import com.divifriends.xyon.pro.ui.components.DiviCardShape

/*
 * Container transform between a group card and its detail screen. The nav host
 * provides both scopes; outside of it (previews, dialogs) the modifiers are
 * no-ops, so components never need to know whether they are inside a
 * transition.
 */

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

fun groupSharedKey(groupId: String) = "group-$groupId"

/** Springy bounds so the card stretches into the screen with a little give. */
@OptIn(ExperimentalSharedTransitionApi::class)
private val ExpandBounds = BoundsTransform { _: Rect, _: Rect ->
    spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
}

/**
 * Marks this element as one end of the "group" container transform: the card
 * on Home / Grupos and the whole detail screen share [key].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedContainer(key: String, clip: Shape = DiviCardShape): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedScope.current ?: return this
    return with(shared) {
        this@sharedContainer.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animated,
            boundsTransform = ExpandBounds,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(clip)
        )
    }
}
