package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divifriends.xyon.pro.ui.theme.DiviPalettes
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextPrimary
import com.divifriends.xyon.pro.ui.theme.TextSecondary

data class DiviNavItem(val route: String, val label: String, val emoji: String)

@Composable
fun DiviBottomBar(
    items: List<DiviNavItem>,
    currentRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        val itemWidth = maxWidth / items.size
        val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
        val offsetX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            label = "navIndicator"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .glass(shape = RoundedCornerShape(35.dp), elevation = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .padding(5.dp)
                    .width(itemWidth - 10.dp)
                    .fillMaxHeight()
                    .glass(
                        shape = RoundedCornerShape(31.dp),
                        borderColor = palette.border,
                        elevation = 0.dp
                    )
            )
            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, item ->
                    val active = index == selectedIndex
                    val color by animateColorAsState(
                        if (active) TextPrimary else TextSecondary,
                        label = "navColor"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelect(item.route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(item.emoji, fontSize = if (active) 24.sp else 20.sp)
                        Text(
                            text = item.label,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSelector(
    currentTheme: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    GlassCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DiviPalettes.forEach { option ->
                val active = option.key == currentTheme
                Box(
                    modifier = Modifier
                        .glass(
                            shape = RoundedCornerShape(16.dp),
                            borderColor = if (active) option.primary else palette.border,
                            elevation = if (active) 8.dp else 0.dp
                        )
                        .clickable { onSelect(option.key) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(12.dp)
                                .glass(
                                    shape = RoundedCornerShape(6.dp),
                                    borderColor = option.primary,
                                    fill = androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(option.primary, option.secondary)
                                    ),
                                    elevation = 0.dp
                                )
                        )
                        Text(
                            text = option.label,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
