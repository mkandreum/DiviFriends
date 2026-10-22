package com.divifriends.xyon.pro.ui.components

import kotlinx.coroutines.delay
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.border
import androidx.compose.animation.core.animateFloatAsState
import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Centered card dialog that springs in. */
@Composable
fun DiviDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) }
    LaunchedEffect(Unit) { alpha.animateTo(1f, tween(160)) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    .clip(DiviLargeCardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

/** Confirmation dialog with a hex icon on top. */
@Composable
fun DiviConfirmDialog(
    title: String,
    message: String,
    icon: ImageVector,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    destructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DiviDialog(onDismiss = onDismiss) {
        HexBadge(
            icon = icon,
            tone = if (destructive) Tone.Danger else Tone.Lime,
            size = 58.dp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        if (destructive) {
            SoftButton(text = confirmText, onClick = onConfirm, tone = Tone.Danger, heavy = true)
        } else {
            PrimaryButton(text = confirmText, onClick = onConfirm)
        }
        SoftButton(text = cancelText, onClick = onDismiss, tone = Tone.Plain)
    }
}

/** Bottom sheet styled for the app (rounded, sheet color, drag handle). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiviSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = DiviSheetShape,
        containerColor = LocalDiviPalette.current.sheet,
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            content = content
        )
    }
}

data class ToastItem(val id: Long, val text: String, val createdAt: Long = SystemClock.uptimeMillis())

/** How long each toast stays, counted from when it arrived. */
private const val TOAST_LIFETIME_MS = 2800L

/**
 * Toasts drop from the top with a springy bounce and stack: the newest sits in
 * front and up to two older ones peek out underneath, smaller and dimmer. Tap
 * the stack and it springs apart into a list (timers pause while it is open);
 * tap again to fold it back.
 */
@Composable
fun DiviToastHost(
    toasts: List<ToastItem>,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHaptics.current
    var expanded by remember { mutableStateOf(false) }
    if (toasts.size < 2) expanded = false
    val visible = toasts.takeLast(3).reversed()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        visible.forEachIndexed { depth, toast ->
            key(toast.id) {
                LaunchedEffect(toast.id, expanded) {
                    if (expanded) return@LaunchedEffect
                    val left = toast.createdAt + TOAST_LIFETIME_MS - SystemClock.uptimeMillis()
                    delay(left.coerceAtLeast(700L))
                    onDismiss(toast.id)
                }
                ToastPill(
                    text = toast.text,
                    depth = depth,
                    expanded = expanded,
                    onClick = {
                        if (toasts.size > 1) {
                            expanded = !expanded
                            haptics?.selection()
                        } else onDismiss(toast.id)
                    },
                    modifier = Modifier.zIndex(-depth.toFloat())
                )
            }
        }
    }
}

@Composable
private fun ToastPill(
    text: String,
    depth: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
    }
    val springy = spring<Float>(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
    // Collapsed: peek 9dp below the one in front. Expanded: a full list.
    val offset by animateFloatAsState(
        targetValue = if (expanded) depth * 62f else depth * 9f,
        animationSpec = springy,
        label = "toastOffset"
    )
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 1f - 0.06f * depth,
        animationSpec = springy,
        label = "toastScale"
    )
    val dim by animateFloatAsState(
        targetValue = if (expanded) 1f else 1f - 0.3f * depth,
        animationSpec = springy,
        label = "toastAlpha"
    )
    Row(
        modifier = modifier
            .graphicsLayer {
                translationY = offset.dp.toPx() + (1f - appear.value) * -90.dp.toPx()
                val s = scale * (0.8f + 0.2f * appear.value)
                scaleX = s
                scaleY = s
                alpha = dim * appear.value.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .bounceClick(pressedScale = 0.96f, feedback = { }, onClick = onClick)
            .floatingShadow(DiviPillShape, 16.dp, Color(0x40000000))
            .clip(DiviPillShape)
            .background(palette.raised)
            .border(1.dp, palette.hairline, DiviPillShape)
            .padding(start = 8.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(palette.sun),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Info, null, tint = palette.onSun, modifier = Modifier.size(16.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ------------------------------------------------------------------ *
 *  Confetti                                                          *
 * ------------------------------------------------------------------ */

private data class Particle(
    val angle: Float,
    val speed: Float,
    val spin: Float,
    val size: Float,
    val color: Color,
    val isHex: Boolean,
    val drift: Float
)

/** Physics-based confetti burst (ballistic arcs + gravity), keyed by [trigger]. */
@Composable
fun BoxScope.ConfettiBurst(trigger: Int) {
    if (trigger <= 0) return
    key(trigger) { Burst() }
}

@Composable
private fun BoxScope.Burst() {
    val palette = LocalDiviPalette.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2200, easing = LinearEasing)) }
    val colors = listOf(palette.sun, Color(0xFF8B5CF6), Color(0xFF22D3EE), palette.negative, Color.White, palette.onBlush)
    val particles = remember {
        val rnd = Random(System.nanoTime())
        List(90) {
            Particle(
                angle = (-90f + (rnd.nextFloat() - 0.5f) * 110f),
                speed = 0.9f + rnd.nextFloat() * 1.1f,
                spin = (rnd.nextFloat() - 0.5f) * 1440f,
                size = 6f + rnd.nextFloat() * 9f,
                color = colors[rnd.nextInt(colors.size)],
                isHex = rnd.nextFloat() < 0.35f,
                drift = (rnd.nextFloat() - 0.5f) * 0.6f
            )
        }
    }
    Canvas(Modifier.matchParentSize()) {
        val t = progress.value
        val origin = Offset(size.width / 2f, size.height * 0.42f)
        val v0 = size.height * 1.15f
        val g = size.height * 2.4f
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val vx = (cos(rad) * v0 * p.speed).toFloat() * 0.55f
            val vy = (sin(rad) * v0 * p.speed).toFloat()
            val x = origin.x + vx * t + p.drift * size.width * t * t
            val y = origin.y + vy * t + 0.5f * g * t * t
            val alpha = (1f - ((t - 0.65f) / 0.35f)).coerceIn(0f, 1f)
            val s = p.size.dp.toPx() * 0.6f
            rotate(p.spin * t, Offset(x, y)) {
                if (p.isHex) {
                    hexOutline(Offset(x, y), s * 0.7f, 0f, p.color.copy(alpha = alpha), strokeWidth = s * 0.45f)
                } else {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - s / 2f, y - s / 4f),
                        size = Size(s, s / 2f)
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  NFC radar                                                         *
 * ------------------------------------------------------------------ */

/** Expanding concentric rings around an icon: "searching / broadcasting". */
@Composable
fun PulseRings(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val palette = LocalDiviPalette.current
    val transition = rememberInfiniteTransition(label = "pulse")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseT"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            repeat(3) { i ->
                val local = (t + i / 3f) % 1f
                drawCircle(
                    color = palette.accent.copy(alpha = (1f - local) * 0.35f),
                    radius = this.size.minDimension / 2f * (0.35f + 0.65f * local)
                )
            }
        }
        Box(
            Modifier
                .size(size * 0.4f)
                .clip(Cookie)
                .background(Brush.linearGradient(listOf(palette.heroStart, palette.heroEnd))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.17f))
        }
    }
}
