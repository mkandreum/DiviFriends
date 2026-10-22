package com.divifriends.xyon.pro.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.theme.DiviPalette
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

val DiviCardShape = RoundedCornerShape(24.dp)
val DiviLargeCardShape = RoundedCornerShape(30.dp)
val DiviInnerShape = RoundedCornerShape(18.dp)
val DiviSmallShape = RoundedCornerShape(12.dp)
val DiviPillShape = RoundedCornerShape(percent = 50)
val DiviSheetShape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)

/**
 * Pointy-top hexagon with softly rounded vertices: the brand motif used for
 * avatars, icon buttons and decorations.
 */
class HexagonShape(private val cornerFraction: Float = 0.2f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = min(size.width, size.height) / 2f
        val points = List(6) { i ->
            val a = Math.toRadians((60.0 * i) - 90.0)
            Offset(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
        }
        val corner = r * cornerFraction
        val path = Path()
        for (i in 0 until 6) {
            val prev = points[(i + 5) % 6]
            val cur = points[i]
            val next = points[(i + 1) % 6]
            val a = cur + (prev - cur).normalized() * corner
            val b = cur + (next - cur).normalized() * corner
            if (i == 0) path.moveTo(a.x, a.y) else path.lineTo(a.x, a.y)
            path.quadraticTo(cur.x, cur.y, b.x, b.y)
        }
        path.close()
        return Outline.Generic(path)
    }

    private fun Offset.normalized(): Offset {
        val len = sqrt(x * x + y * y)
        return if (len == 0f) this else Offset(x / len, y / len)
    }
}

val Hexagon = HexagonShape()

/** Soft colored shadow reserved for floating elements (navigation, CTAs). */
fun Modifier.floatingShadow(
    shape: Shape = DiviPillShape,
    elevation: Dp = 18.dp,
    color: Color = Color(0x3317A589)
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = color,
    spotColor = color
)

/** Tonal variants for cards and tiles. */
enum class Tone { Plain, Raised, Mint, Aqua, Sun, Accent, Danger }

fun DiviPalette.container(tone: Tone): Color = when (tone) {
    Tone.Plain -> sheet
    Tone.Raised -> raised
    Tone.Mint -> mint
    Tone.Aqua -> aqua
    Tone.Sun -> sun
    Tone.Accent -> accent
    Tone.Danger -> negativeSoft
}

fun DiviPalette.content(tone: Tone, fallback: Color): Color = when (tone) {
    Tone.Plain, Tone.Raised -> fallback
    Tone.Mint -> onMint
    Tone.Aqua -> onAqua
    Tone.Sun -> onSun
    Tone.Accent -> onAccent
    Tone.Danger -> negative
}

/** Rounded content card. Pass [onClick] to make it springy and tappable. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Raised,
    shape: Shape = DiviCardShape,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalDiviPalette.current
    val clickable = if (onClick != null) {
        Modifier.bounceClick(pressedScale = 0.97f, feedback = { it.selection() }, onClick = onClick)
    } else Modifier
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable)
            .clip(shape)
            .background(palette.container(tone))
            .padding(contentPadding),
        content = content
    )
}
