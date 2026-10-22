package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.rememberRevealProgress
import com.divifriends.xyon.pro.ui.theme.Barlow
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.mono
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min

/**
 * Smooth wave line chart with a self-drawing reveal and finger scrubbing:
 * dragging snaps to each point with a haptic tick and shows a value bubble.
 */
@Composable
fun WaveChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    initialSelected: Int = values.lastIndex,
    lineColor: Color = LocalDiviPalette.current.sun,
    labelColor: Color = Color.White.copy(alpha = 0.7f),
    selectedLabelColor: Color = Color.White,
    bubbleColor: Color = LocalDiviPalette.current.sun,
    bubbleTextColor: Color = LocalDiviPalette.current.onSun,
    chartHeight: Dp = 150.dp,
    valueLabel: (Double) -> String
) {
    val haptics = LocalHaptics.current
    var selected by remember(values.size) { mutableIntStateOf(initialSelected.coerceIn(0, (values.size - 1).coerceAtLeast(0))) }
    val animIndex by animateFloatAsState(
        selected.toFloat(),
        spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "scrub"
    )
    val reveal = rememberRevealProgress(durationMillis = 1400, key = values)
    val measurer = rememberTextMeasurer()
    val bubbleStyle = TextStyle(fontFamily = com.divifriends.xyon.pro.ui.theme.SpaceMono, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.labelLarge.fontSize, color = bubbleTextColor)
        .mono()

    fun select(x: Float, width: Float) {
        if (values.isEmpty()) return
        val cell = width / values.size
        val index = floor(x / cell).toInt().coerceIn(0, values.lastIndex)
        if (index != selected) {
            selected = index
            haptics?.tick()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .pointerInput(values) {
                    detectTapGestures { select(it.x, size.width.toFloat()) }
                }
                .pointerInput(values) {
                    detectHorizontalDragGestures { change, _ ->
                        select(change.position.x, size.width.toFloat())
                    }
                }
        ) {
            if (values.isEmpty()) return@Canvas
            val top = 46.dp.toPx()
            val bottom = size.height - 8.dp.toPx()
            val max = values.max().coerceAtLeast(1.0)
            val min = values.min().coerceAtMost(max * 0.2)
            val range = (max - min).coerceAtLeast(1.0)
            val cell = size.width / values.size
            val pts = values.mapIndexed { i, v ->
                Offset(cell * (i + 0.5f), bottom - ((v - min) / range).toFloat() * (bottom - top))
            }

            val path = Path().apply {
                moveTo(0f, pts.first().y)
                lineTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) {
                    val p = pts[i - 1]
                    val c = pts[i]
                    val mid = (p.x + c.x) / 2f
                    cubicTo(mid, p.y, mid, c.y, c.x, c.y)
                }
                lineTo(size.width, pts.last().y)
            }
            val measure = PathMeasure().apply { setPath(path, false) }
            val partial = Path()
            measure.getSegment(0f, measure.length * reveal, partial, true)

            val fill = Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            clipRect(right = size.width * reveal) {
                drawPath(fill, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.22f), Color.Transparent), startY = top, endY = size.height))
            }
            drawPath(partial, lineColor, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

            if (reveal > 0.98f) {
                val lo = floor(animIndex).toInt().coerceIn(0, pts.lastIndex)
                val hi = (lo + 1).coerceAtMost(pts.lastIndex)
                val f = animIndex - lo
                val dot = Offset(pts[lo].x + (pts[hi].x - pts[lo].x) * f, pts[lo].y + (pts[hi].y - pts[lo].y) * f)

                drawLine(
                    color = lineColor.copy(alpha = 0.45f),
                    start = Offset(dot.x, dot.y + 10.dp.toPx()),
                    end = Offset(dot.x, size.height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
                drawCircle(lineColor.copy(alpha = 0.25f), radius = 12.dp.toPx(), center = dot)
                drawCircle(lineColor, radius = 6.dp.toPx(), center = dot)
                drawCircle(bubbleTextColor, radius = 3.dp.toPx(), center = dot)

                val layout = measurer.measure(valueLabel(values[selected]), bubbleStyle)
                val padH = 10.dp.toPx()
                val padV = 6.dp.toPx()
                val bw = layout.size.width + padH * 2
                val bh = layout.size.height + padV * 2
                val bx = (dot.x - bw / 2f).coerceIn(0f, size.width - bw)
                val by = (dot.y - bh - 16.dp.toPx()).coerceAtLeast(0f)
                drawRoundRect(bubbleColor, Offset(bx, by), Size(bw, bh), CornerRadius(bh / 2f))
                drawText(layout, topLeft = Offset(bx + padH, by + padV))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                val isSel = index == selected
                val color by animateColorAsState(if (isSel) selectedLabelColor else labelColor, label = "waveLabel")
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium),
                    color = color,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Wavy circular progress (M3 Expressive): lime squiggle over a soft track. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    stroke: Dp = 6.dp,
    trackColor: Color = Color.White.copy(alpha = 0.22f),
    progressColor: Color = LocalDiviPalette.current.sun,
    label: String? = null,
    labelColor: Color = Color.White
) {
    val reveal = rememberRevealProgress(durationMillis = 1300, delayMillis = 250)
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), spring(stiffness = Spring.StiffnessLow), label = "ring")
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val s = stroke.toPx()
            val base = (this.size.minDimension - s) / 2f - s * 0.6f
            val amp = s * 0.55f
            val waves = 10
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val path = Path()
            val steps = 240
            for (i in 0..steps) {
                val t = i / steps.toFloat() * 2f * Math.PI.toFloat()
                val r = base + amp * kotlin.math.sin(waves * t)
                val x = c.x + r * kotlin.math.cos(t - Math.PI.toFloat() / 2f)
                val y = c.y + r * kotlin.math.sin(t - Math.PI.toFloat() / 2f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, trackColor, style = Stroke(s, cap = StrokeCap.Round))
            val measure = PathMeasure().apply { setPath(path, false) }
            val part = Path()
            measure.getSegment(0f, measure.length * animated * reveal, part, true)
            drawPath(part, progressColor, style = Stroke(s, cap = StrokeCap.Round))
        }
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelLarge.mono(), color = labelColor)
        }
    }
}

/** Palette used by categorical charts. */
@Composable
fun chartColors(): List<Color> {
    val p = LocalDiviPalette.current
    return listOf(p.accent, Color(0xFF8B5CF6), Color(0xFF22D3EE), p.negative, p.onBlush, p.accentDeep, Color(0xFFE4E4E7))
}

/**
 * Donut with springy segments. Tap a segment to lift it (and feel a tick);
 * the center shows what's selected.
 */
@Composable
fun DonutChart(
    data: List<Pair<String, Double>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    centerTitle: String,
    centerValue: String
) {
    val colors = chartColors()
    val haptics = LocalHaptics.current
    val track = MaterialTheme.colorScheme.outline
    val total = data.sumOf { it.second }.coerceAtLeast(0.0001)
    val reveal = rememberRevealProgress(durationMillis = 1200, key = data)
    val lifts = data.indices.map { i ->
        animateFloatAsState(if (i == selected) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), label = "lift$i").value
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .size(size)
                .pointerInput(data) {
                    detectTapGestures { pos ->
                        val c = Offset(this.size.width / 2f, this.size.height / 2f)
                        val dist = hypot(pos.x - c.x, pos.y - c.y)
                        if (dist < this.size.width * 0.22f || dist > this.size.width * 0.52f) return@detectTapGestures
                        var angle = Math.toDegrees(atan2((pos.y - c.y).toDouble(), (pos.x - c.x).toDouble())).toFloat() + 90f
                        if (angle < 0) angle += 360f
                        var acc = 0f
                        data.forEachIndexed { i, (_, v) ->
                            val sweep = (v / total).toFloat() * 360f
                            if (angle >= acc && angle < acc + sweep) {
                                haptics?.toggle()
                                onSelect(if (i == selected) -1 else i)
                                return@detectTapGestures
                            }
                            acc += sweep
                        }
                    }
                }
        ) {
            val base = min(this.size.width, this.size.height)
            val stroke = base * 0.13f
            val diameter = base * 0.78f
            val tl = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
            drawArc(track, 0f, 360f, false, tl, Size(diameter, diameter), style = Stroke(stroke))
            var start = -90f
            data.forEachIndexed { i, (_, v) ->
                val sweep = (v / total).toFloat() * 360f * reveal
                val extra = stroke * 0.35f * lifts.getOrElse(i) { 0f }
                val d = diameter + extra
                val off = Offset((this.size.width - d) / 2f, (this.size.height - d) / 2f)
                drawArc(
                    color = colors[i % colors.size],
                    startAngle = start + 1.5f,
                    sweepAngle = (sweep - 3f).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = off,
                    size = Size(d, d),
                    style = Stroke(stroke + extra, cap = StrokeCap.Round)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerValue, style = MaterialTheme.typography.titleMedium.mono(), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                centerTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(size * 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Labelled horizontal bar that fills in with a spring after [delayMillis]. */
@Composable
fun FillBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    delayMillis: Int = 0,
    track: Color = MaterialTheme.colorScheme.outline
) {
    val reveal = rememberRevealProgress(durationMillis = 1000, delayMillis = delayMillis)
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), spring(stiffness = Spring.StiffnessLow), label = "fill")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(DiviPillShape)
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth((animated * reveal).coerceIn(0f, 1f))
                .height(height)
                .clip(DiviPillShape)
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.75f), color)))
        )
    }
}

/**
 * Diverging bars per person: green to the right when owed, coral to the left
 * when they owe.
 */
@Composable
fun BalanceBars(balances: List<Pair<String, Double>>, modifier: Modifier = Modifier, format: (Double) -> String) {
    val palette = LocalDiviPalette.current
    val max = balances.maxOfOrNull { kotlin.math.abs(it.second) }?.coerceAtLeast(0.01) ?: 1.0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        balances.forEachIndexed { index, (name, value) ->
            val reveal = rememberRevealProgress(durationMillis = 900, delayMillis = 80 * index)
            val f = (kotlin.math.abs(value) / max).toFloat() * reveal
            val positive = value >= 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Avatar(name = name, size = 34.dp)
                Column(Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(
                            format(value),
                            style = MaterialTheme.typography.labelLarge.mono(),
                            color = when {
                                kotlin.math.abs(value) < 0.005 -> MaterialTheme.colorScheme.onSurfaceVariant
                                positive -> palette.positive
                                else -> palette.negative
                            }
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth().height(8.dp)) {
                        Box(Modifier.weight(1f).height(8.dp), contentAlignment = Alignment.CenterEnd) {
                            if (!positive) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(f.coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .clip(DiviPillShape)
                                        .background(palette.negative)
                                )
                            }
                        }
                        Box(Modifier.width(2.dp).height(8.dp).background(MaterialTheme.colorScheme.outline))
                        Box(Modifier.weight(1f).height(8.dp), contentAlignment = Alignment.CenterStart) {
                            if (positive) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(f.coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .clip(DiviPillShape)
                                        .background(palette.positive)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
