package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.theme.GlassBorder

val DiviCardShape = RoundedCornerShape(28.dp)
val DiviInnerShape = RoundedCornerShape(18.dp)

/** Translucent "glass" surface used across the whole UI. */
fun Modifier.glass(
    shape: Shape = DiviCardShape,
    borderColor: Color = GlassBorder,
    fill: Brush = Brush.linearGradient(
        listOf(Color(0x42181A2C), Color(0x1F0D0D16))
    ),
    elevation: Dp = 12.dp
): Modifier = this
    .shadow(elevation = elevation, shape = shape, clip = false)
    .clip(shape)
    .background(fill)
    .border(width = 1.dp, color = borderColor, shape = shape)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = DiviCardShape,
    borderColor: Color = GlassBorder,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(shape = shape, borderColor = borderColor)
            .padding(contentPadding),
        content = content
    )
}
