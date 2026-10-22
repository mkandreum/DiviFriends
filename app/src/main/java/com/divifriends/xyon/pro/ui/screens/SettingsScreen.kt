package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.components.Avatar
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviDivider
import com.divifriends.xyon.pro.ui.components.DiviSwitch
import com.divifriends.xyon.pro.ui.components.HeroPill
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.SettingRow
import com.divifriends.xyon.pro.ui.components.SurfaceCard
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.floating
import com.divifriends.xyon.pro.ui.motion.staggeredAppear
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

@Composable
fun SettingsScreen(
    userName: String,
    groupCount: Int,
    expenseCount: Int,
    darkMode: Boolean,
    hapticsEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    var showConfirm by remember { mutableStateOf(false) }

    HeroScaffold(
        compactTitle = "Ajustes",
        onBack = onBack,
        hero = {
            HeroTopBar(title = "Ajustes", onBack = onBack)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(
                    name = userName.ifBlank { "?" },
                    size = 92.dp,
                    modifier = Modifier
                        .floating(amplitudeDp = 5f)
                        .staggeredAppear(0)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = userName.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.onHero,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                HeroPill(text = "$groupCount grupos · $expenseCount gastos")
            }
        },
        sheet = {
            SectionHeader("Preferencias")
            SurfaceCard(modifier = Modifier.staggeredAppear(0), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                SettingRow(
                    icon = Icons.Rounded.DarkMode,
                    label = "Modo oscuro",
                    caption = if (darkMode) "Activado" else "Desactivado",
                    tone = Tone.Aqua,
                    onClick = { onDarkModeChange(!darkMode) },
                    trailing = { DiviSwitch(checked = darkMode, onCheckedChange = onDarkModeChange) }
                )
                DiviDivider()
                SettingRow(
                    icon = Icons.Rounded.Vibration,
                    label = "Vibración háptica",
                    caption = "Respuesta táctil en toques y gestos",
                    tone = Tone.Mint,
                    onClick = { onHapticsChange(!hapticsEnabled) },
                    trailing = {
                        DiviSwitch(checked = hapticsEnabled, onCheckedChange = { enabled ->
                            onHapticsChange(enabled)
                            if (enabled) {
                                haptics?.userEnabled = true
                                haptics?.success()
                            }
                        })
                    }
                )
            }

            SectionHeader("Tus datos")
            SurfaceCard(modifier = Modifier.staggeredAppear(1), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                SettingRow(
                    icon = Icons.Rounded.Download,
                    label = "Guardar copia de seguridad",
                    caption = "Exporta todo a un archivo JSON",
                    tone = Tone.Mint,
                    onClick = onBackup
                )
                DiviDivider()
                SettingRow(
                    icon = Icons.Rounded.Upload,
                    label = "Restaurar copia",
                    caption = "Importa un archivo de respaldo",
                    tone = Tone.Aqua,
                    onClick = onRestore
                )
                DiviDivider()
                SettingRow(
                    icon = Icons.Rounded.DeleteForever,
                    label = "Eliminar todos los datos",
                    caption = "Grupos, gastos y ajustes",
                    tone = Tone.Danger,
                    onClick = {
                        haptics?.warning()
                        showConfirm = true
                    }
                )
            }

            SectionHeader("Acerca de")
            SurfaceCard(tone = Tone.Mint, modifier = Modifier.staggeredAppear(2)) {
                Text("DiviFriends 2.0", style = MaterialTheme.typography.titleMedium, color = palette.onMint)
                Text(
                    text = "Reparte gastos sin cuentas ni servidores. 100 % offline, tus datos no salen del móvil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onMint.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(10.dp))
                SettingRow(icon = Icons.Rounded.Favorite, label = "Hecho por Xyon Platforms", tone = Tone.Plain)
            }
        }
    )

    if (showConfirm) {
        DiviConfirmDialog(
            title = "¿Eliminar todo?",
            message = "Se borrarán todos tus grupos, gastos y ajustes. Esta acción no se puede deshacer.",
            icon = Icons.Rounded.DeleteForever,
            confirmText = "Eliminar todo",
            onConfirm = {
                showConfirm = false
                onDeleteAll()
            },
            onDismiss = { showConfirm = false }
        )
    }
}
