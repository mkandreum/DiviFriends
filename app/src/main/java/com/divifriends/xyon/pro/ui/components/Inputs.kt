package com.divifriends.xyon.pro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.mono
import kotlinx.coroutines.launch

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

/** Filled text field whose border glows in the accent color while focused. */
@Composable
fun DiviTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    onHero: Boolean = false
) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    LaunchedEffect(focused) { if (focused) haptics?.selection() }

    val container = if (onHero) Color.White.copy(alpha = 0.18f) else palette.raised
    val textColor = if (onHero) palette.onHero else MaterialTheme.colorScheme.onSurface
    val hint = if (onHero) palette.onHeroMuted else MaterialTheme.colorScheme.onSurfaceVariant
    val border by animateColorAsState(
        if (focused) (if (onHero) Color.White else palette.accent) else Color.Transparent,
        label = "fieldBorder"
    )
    val borderWidth by animateDpAsState(if (focused) 2.dp else 0.dp, label = "fieldBorderW")

    Column(modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (onHero) palette.onHeroMuted else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            interactionSource = source,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(if (onHero) Color.White else palette.accent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction?.invoke() }),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(DiviInnerShape)
                        .background(container)
                        .border(borderWidth, border, DiviInnerShape)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, null, tint = if (focused) (if (onHero) Color.White else palette.accent) else hint, modifier = Modifier.size(20.dp))
                    }
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = hint, maxLines = 1)
                        }
                        inner()
                    }
                }
            }
        )
    }
}

/** Horizontally scrolling emoji category chips. */
@Composable
fun CategoryChips(
    categories: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (emoji, label) ->
            val isSelected = emoji == selected
            val bg by animateColorAsState(if (isSelected) palette.accent else palette.raised, label = "chipBg")
            val fg by animateColorAsState(
                if (isSelected) palette.onAccent else MaterialTheme.colorScheme.onSurface,
                label = "chipFg"
            )
            val scale by animateFloatAsState(
                if (isSelected) 1.06f else 1f,
                spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
                label = "chipScale"
            )
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .bounceClick(pressedScale = 0.9f, feedback = { it.toggle() }) { onSelect(emoji) }
                    .clip(DiviPillShape)
                    .background(bg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}

/**
 * Row of avatars to pick people. Single mode (who paid) or multi mode (who
 * shares); selected avatars grow and get a check badge that pops in.
 */
@Composable
fun PeoplePicker(
    people: List<String>,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 52.dp
) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        people.forEach { person ->
            val selected = isSelected(person)
            val scale by animateFloatAsState(
                if (selected) 1f else 0.86f,
                spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
                label = "personScale"
            )
            val alpha by animateFloatAsState(if (selected) 1f else 0.5f, label = "personAlpha")
            Column(
                modifier = Modifier
                    .width(avatarSize + 14.dp)
                    .bounceClick(pressedScale = 0.88f, feedback = { it.toggle() }) { onToggle(person) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Avatar(
                        name = person,
                        size = avatarSize,
                        highlight = selected,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                    )
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selected,
                        enter = scaleIn(spring(dampingRatio = 0.4f)),
                        exit = scaleOut(),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Box(
                            Modifier
                                .size(20.dp)
                                .border(2.dp, palette.sheet, CircleShape)
                                .clip(CircleShape)
                                .background(palette.sun),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Check, null, tint = palette.onSun, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = person,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Custom switch: springy thumb, color crossfade, haptic. */
@Composable
fun DiviSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalDiviPalette.current
    val track by animateColorAsState(if (checked) palette.accent else MaterialTheme.colorScheme.outline, label = "track")
    val offset by animateDpAsState(
        if (checked) 22.dp else 0.dp,
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "thumb"
    )
    Box(
        modifier = modifier
            .width(52.dp)
            .height(30.dp)
            .bounceClick(pressedScale = 0.92f, feedback = { it.toggle() }) { onCheckedChange(!checked) }
            .clip(DiviPillShape)
            .background(track)
            .padding(3.dp)
    ) {
        Box(
            Modifier
                .offset(x = offset)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** Settings-style row: hex icon, label + caption, trailing slot. */
@Composable
fun SettingRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    tone: Tone = Tone.Lime,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.bounceClick(pressedScale = 0.97f, feedback = { it.selection() }, onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HexBadge(icon = icon, tone = tone, size = 44.dp)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            if (caption != null) {
                Text(caption, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

/**
 * Segmented control with a sliding pill indicator. [onHero] renders the
 * translucent variant for gradient backgrounds.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onHero: Boolean = false
) {
    val palette = LocalDiviPalette.current
    val track = if (onHero) palette.heroChip else palette.raised
    val indicator = if (onHero) Color.White else palette.sheet
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(DiviPillShape)
            .background(track)
            .padding(4.dp)
    ) {
        val segment = maxWidth / options.size
        val offset by animateDpAsState(
            targetValue = segment * selectedIndex,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
            label = "segment"
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(segment)
                .fillMaxHeight()
                .floatingShadow(DiviPillShape, if (onHero) 0.dp else 4.dp, palette.shadow)
                .clip(DiviPillShape)
                .background(indicator)
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val color by animateColorAsState(
                    when {
                        selected -> palette.accentDeep
                        onHero -> palette.onHero
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "segmentText"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .bounceClick(pressedScale = 0.92f, feedback = { it.toggle() }) {
                            if (index != selectedIndex) onSelect(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(option, style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1)
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Amount keypad                                                      *
 * ------------------------------------------------------------------ */

const val KEY_BACKSPACE = "<"
const val KEY_CLEAR = "C"

/** Applies a keypad key to an amount string ("12,5"), enforcing sane limits. */
fun applyAmountKey(current: String, key: String): String {
    return when (key) {
        KEY_CLEAR -> ""
        KEY_BACKSPACE -> current.dropLast(1)
        "," -> when {
            current.contains(',') -> current
            current.isEmpty() -> "0,"
            else -> "$current,"
        }
        else -> {
            val comma = current.indexOf(',')
            when {
                comma >= 0 && current.length - comma > 2 -> current
                comma < 0 && current.length >= 7 -> current
                current == "0" -> key
                else -> current + key
            }
        }
    }
}

fun parseAmount(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

/** Big amount readout that pops on every change. */
@Composable
fun AmountDisplay(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(text) {
        scale.snapTo(1.06f)
        scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
    }
    val shown = text.ifEmpty { "0" }
    Row(
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = shown,
            style = MaterialTheme.typography.displayLarge.mono().copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.15f),
            color = if (text.isEmpty()) color.copy(alpha = 0.35f) else color,
            maxLines = 1
        )
        Text(
            text = " €",
            style = MaterialTheme.typography.headlineMedium,
            color = color.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/** 3×4 numeric keypad; every key has its own spring + tick. Long-press ⌫ clears. */
@Composable
fun AmountKeypad(onKey: (String) -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(",", "0", KEY_BACKSPACE)
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    KeypadKey(key = key, onKey = onKey, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(key: String, onKey: (String) -> Unit, modifier: Modifier) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.88f else 1f,
        spring(dampingRatio = if (pressed) 1f else 0.4f, stiffness = Spring.StiffnessHigh),
        label = "keyScale"
    )
    val bg by animateColorAsState(if (pressed) palette.accentSoft else palette.raised, label = "keyBg")
    Box(
        modifier = modifier
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(DiviInnerShape)
            .background(bg)
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        haptics?.selection()
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onKey(key) },
                    onLongPress = {
                        if (key == KEY_BACKSPACE) {
                            haptics?.heavy()
                            scope.launch { onKey(KEY_CLEAR) }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (key == KEY_BACKSPACE) {
            Icon(Icons.AutoMirrored.Rounded.Backspace, "Borrar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
        } else {
            Text(key, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
