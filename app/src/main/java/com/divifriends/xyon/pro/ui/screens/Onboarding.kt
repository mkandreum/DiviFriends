package com.divifriends.xyon.pro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.divifriends.xyon.pro.ui.components.DiviPillShape
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.Hexagon
import com.divifriends.xyon.pro.ui.components.HeroGradient
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.breathing
import com.divifriends.xyon.pro.ui.motion.floating
import com.divifriends.xyon.pro.ui.motion.shakeOnError
import com.divifriends.xyon.pro.ui.motion.shimmer
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import kotlinx.coroutines.delay

@Composable
fun OnboardingDialog(onConfirm: (String) -> Unit) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    var name by remember { mutableStateOf("") }
    var shake by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        revealed = true
    }

    fun submit() {
        if (name.isBlank()) {
            haptics?.error()
            shake++
        } else {
            haptics?.celebrate()
            onConfirm(name.trim())
        }
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            HeroGradient(Modifier.fillMaxSize())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 26.dp)
                    .padding(top = 24.dp, bottom = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Illustration()
                }

                AnimatedVisibility(
                    visible = revealed,
                    enter = slideInVertically(spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                ) {
                    Column {
                        Text(
                            text = "Divide gastos\nsin dramas",
                            style = MaterialTheme.typography.displayLarge,
                            color = palette.onHero
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Apunta quién paga qué y DiviFriends calcula el mínimo de pagos para quedar en paz.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.onHeroMuted
                        )
                        Spacer(Modifier.height(26.dp))
                        DiviTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "¿Cómo te llamas?",
                            leadingIcon = Icons.Rounded.Person,
                            imeAction = ImeAction.Go,
                            onImeAction = ::submit,
                            onHero = true,
                            modifier = Modifier.shakeOnError(shake)
                        )
                        Spacer(Modifier.height(22.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Omitir",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.onHeroMuted,
                                modifier = Modifier
                                    .bounceClick(feedback = { it.selection() }) { onConfirm("") }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            )
                            val ready = name.isNotBlank()
                            val alpha by animateFloatAsState(if (ready) 1f else 0.6f, label = "ctaAlpha")
                            Row(
                                modifier = Modifier
                                    .then(if (ready) Modifier.breathing(0.98f, 1.03f) else Modifier)
                                    .bounceClick(pressedScale = 0.92f, feedback = { }, onClick = ::submit)
                                    .alpha(alpha)
                                    .clip(DiviPillShape)
                                    .background(Color.White)
                                    .then(if (ready) Modifier.shimmer(Color(0x3317A589)) else Modifier)
                                    .padding(start = 24.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("Empezar", style = MaterialTheme.typography.labelLarge, color = palette.accentDeep)
                                Box(
                                    Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(palette.heroStart, palette.heroEnd))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Floating cluster of hexagons and money emoji. */
@Composable
private fun Illustration() {
    val palette = LocalDiviPalette.current
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .offset(x = (-70).dp, y = (-60).dp)
                .floating(amplitudeDp = 8f, periodMillis = 3600, phase = 0.1f)
                .size(70.dp)
                .clip(Hexagon)
                .background(palette.sun),
            contentAlignment = Alignment.Center
        ) { Text("💸", fontSize = 30.sp) }
        Box(
            Modifier
                .offset(x = 80.dp, y = (-30).dp)
                .floating(amplitudeDp = 7f, periodMillis = 4200, phase = 0.6f)
                .size(56.dp)
                .clip(Hexagon)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) { Text("🍕", fontSize = 24.sp) }
        Box(
            Modifier
                .offset(x = 60.dp, y = 80.dp)
                .floating(amplitudeDp = 6f, periodMillis = 3000, phase = 0.3f)
                .size(48.dp)
                .clip(Hexagon)
                .background(palette.aqua),
            contentAlignment = Alignment.Center
        ) { Text("✈️", fontSize = 20.sp) }
        Box(
            Modifier
                .floating(amplitudeDp = 10f, periodMillis = 4800)
                .size(130.dp)
                .clip(Hexagon)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) { Text("🤝", fontSize = 58.sp) }
        Box(
            Modifier
                .offset(x = (-80).dp, y = 70.dp)
                .floating(amplitudeDp = 5f, periodMillis = 3300, phase = 0.8f)
                .size(34.dp)
                .clip(Hexagon)
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}
