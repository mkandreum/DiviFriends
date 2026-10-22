package com.divifriends.xyon.pro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divifriends.xyon.pro.ui.components.Cookie
import com.divifriends.xyon.pro.ui.components.DiviPillShape
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.HeroGradient
import com.divifriends.xyon.pro.ui.components.Hexagon
import com.divifriends.xyon.pro.ui.components.clickableNoRipple
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.EmphasizedDecelerate
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.breathing
import com.divifriends.xyon.pro.ui.motion.floating
import com.divifriends.xyon.pro.ui.motion.shakeOnError
import com.divifriends.xyon.pro.ui.motion.shimmer
import com.divifriends.xyon.pro.ui.theme.InstrumentSerif
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.WordmarkStyle
import kotlinx.coroutines.delay

/**
 * Full-screen welcome. Brand first (big DIVI/FRIENDS wordmark at the top),
 * then the pitch and the name field, all top-aligned so nothing sinks to
 * the bottom of the screen.
 */
@Composable
fun OnboardingScreen(onConfirm: (String) -> Unit) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    var name by remember { mutableStateOf("") }
    var shake by remember { mutableIntStateOf(0) }

    // Swallow back so the app isn't left half-configured.
    BackHandler { }

    fun submit() {
        if (name.isBlank()) {
            haptics?.error()
            shake++
        } else {
            haptics?.celebrate()
            onConfirm(name.trim())
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .clickableNoRipple { }
    ) {
        HeroGradient(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp)
                .padding(top = 18.dp, bottom = 28.dp)
        ) {
            Reveal(delayMillis = 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(Cookie)
                            .background(palette.sun),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤝", fontSize = 15.sp)
                    }
                    Text(
                        text = "  XYON PLATFORMS · v2.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.onHeroMuted
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Reveal(delayMillis = 120) {
                    Text("DIVI", style = WordmarkStyle.copy(fontSize = 68.sp, lineHeight = 64.sp), color = palette.onHero)
                }
                Spacer(Modifier.weight(1f))
                Illustration(Modifier.offset(x = 6.dp))
            }
            Reveal(delayMillis = 240) {
                Text("FRIENDS", style = WordmarkStyle.copy(fontSize = 68.sp, lineHeight = 64.sp), color = palette.sun, maxLines = 1)
            }
            Spacer(Modifier.height(22.dp))
            Reveal(delayMillis = 380) {
                Column {
                    Text(
                        text = "Divide gastos\nsin dramas.",
                        style = MaterialTheme.typography.displayMedium.copy(fontStyle = FontStyle.Italic, fontFamily = InstrumentSerif),
                        color = palette.onHero
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Apunta quién paga qué y calculamos el mínimo de pagos para quedar en paz. Sin cuentas, sin servidores.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.onHeroMuted
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
            Reveal(delayMillis = 500) {
                Column {
                    Text(
                        text = "¿CÓMO TE LLAMAS?",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.sun,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    DiviTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Tu nombre",
                        leadingIcon = Icons.Rounded.Person,
                        imeAction = ImeAction.Go,
                        onImeAction = ::submit,
                        onHero = true,
                        modifier = Modifier.shakeOnError(shake)
                    )
                    Spacer(Modifier.height(20.dp))
                    val ready = name.isNotBlank()
                    val alpha by animateFloatAsState(if (ready) 1f else 0.7f, label = "ctaAlpha")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (ready) Modifier.breathing(0.985f, 1.015f) else Modifier)
                            .bounceClick(pressedScale = 0.95f, feedback = { }, onClick = ::submit)
                            .alpha(alpha)
                            .clip(DiviPillShape)
                            .background(palette.sun)
                            .then(if (ready) Modifier.shimmer(Color(0x66FFFFFF)) else Modifier)
                            .padding(start = 26.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("EMPEZAR", style = MaterialTheme.typography.titleLarge, color = palette.onSun)
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(palette.onSun),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = palette.sun, modifier = Modifier.size(22.dp))
                        }
                    }
                    Text(
                        text = "Omitir por ahora",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.onHeroMuted,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                            .bounceClick(feedback = { it.selection() }) { onConfirm("") }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

/** Masked slide-up reveal for each block. */
@Composable
private fun Reveal(delayMillis: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        progress.animateTo(1f, tween(700, easing = EmphasizedDecelerate))
    }
    Box(Modifier.clipToBounds()) {
        Box(
            Modifier.graphicsLayer {
                translationY = (1f - progress.value) * size.height
                alpha = progress.value
            }
        ) { content() }
    }
}

/** Small floating cluster of shapes next to the wordmark. */
@Composable
private fun Illustration(modifier: Modifier = Modifier) {
    val palette = LocalDiviPalette.current
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(450)
        pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
    }
    Box(
        modifier
            .size(104.dp)
            .graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            }
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .floating(amplitudeDp = 6f, periodMillis = 4200)
                .size(74.dp)
                .clip(Cookie)
                .background(palette.sun),
            contentAlignment = Alignment.Center
        ) { Text("💸", fontSize = 38.sp) }
        Box(
            Modifier
                .align(Alignment.TopStart)
                .floating(amplitudeDp = 5f, periodMillis = 3300, phase = 0.4f)
                .size(34.dp)
                .clip(Hexagon)
                .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) { Text("🍕", fontSize = 16.sp) }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .floating(amplitudeDp = 4f, periodMillis = 3800, phase = 0.7f)
                .size(22.dp)
                .clip(CircleShape)
                .background(palette.onSun)
        )
    }
}
