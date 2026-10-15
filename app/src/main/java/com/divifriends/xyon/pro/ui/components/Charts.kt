package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextSecondary

@Composable
fun DoughnutChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    val colors = listOf(
        palette.primary,
        palette.secondary,
        palette.accent,
        Color(0xFF9D00FF),
        Color(0xFFFF073A),
        Color(0xFFFFD700),
        Color(0xFF00C2A8)
    )
    val total = data.sumOf { it.second }
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (total <= 0.0) return@Canvas
            val stroke = size.minDimension * 0.22f
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            var startAngle = -90f
            data.forEachIndexed { index, (_, value) ->
                val sweep = (value / total * 360f).toFloat()
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle + 1.5f,
                    sweepAngle = (sweep - 3f).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.forEachIndexed { index, (label, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                    )
                    Text(
                        text = formatCurrency(value),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(
    points: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    val sorted = points.sortedBy { it.first }
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                if (sorted.isEmpty()) return@Canvas
                val maxY = (sorted.maxOf { it.second } * 1.15).coerceAtLeast(1.0)
                val minX = sorted.first().first
                val maxX = sorted.last().first
                val spanX = (maxX - minX).coerceAtLeast(1L).toDouble()
                val left = 8f
                val right = size.width - 8f
                val top = 8f
                val bottom = size.height - 8f

                // horizontal grid
                for (i in 0..4) {
                    val y = top + (bottom - top) * (i / 4f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(left, y),
                        end = Offset(right, y),
                        strokeWidth = 1f
                    )
                }

                fun mapX(x: Long): Float =
                    left + ((x - minX) / spanX * (right - left)).toFloat()

                fun mapY(y: Double): Float =
                    bottom - (y / maxY * (bottom - top)).toFloat()

                val path = Path()
                sorted.forEachIndexed { index, (x, y) ->
                    val px = mapX(x)
                    val py = mapY(y)
                    if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }

                if (sorted.size > 1) {
                    val fill = Path().apply {
                        addPath(path)
                        lineTo(mapX(sorted.last().first), bottom)
                        lineTo(mapX(sorted.first().first), bottom)
                        close()
                    }
                    drawPath(
                        path = fill,
                        brush = Brush.verticalGradient(
                            listOf(palette.primary.copy(alpha = 0.35f), Color.Transparent),
                            startY = top,
                            endY = bottom
                        )
                    )
                }

                drawPath(
                    path = path,
                    color = palette.primary,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                sorted.forEach { (x, y) ->
                    drawCircle(
                        color = palette.primary,
                        radius = 6f,
                        center = Offset(mapX(x), mapY(y))
                    )
                    drawCircle(
                        color = Color(0xFF0A0A10),
                        radius = 3f,
                        center = Offset(mapX(x), mapY(y))
                    )
                }
            }
        }
    }
}
