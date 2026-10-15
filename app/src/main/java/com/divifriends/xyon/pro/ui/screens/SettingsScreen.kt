package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.components.ButtonTone
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviDivider
import com.divifriends.xyon.pro.ui.components.DiviHeader
import com.divifriends.xyon.pro.ui.components.DiviScreen
import com.divifriends.xyon.pro.ui.components.GlassButton
import com.divifriends.xyon.pro.ui.components.GlassCard
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.ThemeSelector

@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onDeleteAll: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    DiviScreen {
        DiviHeader(subtitle = "Configuraci\u00f3n y M\u00e1s \u2699\uFE0F")

        GlassCard {
            SectionHeader("\u2699\uFE0F Ajustes")
            SectionHeader("\uD83C\uDFA8 Cambiar Tema")
            ThemeSelector(
                currentTheme = currentTheme,
                onSelect = onThemeChange,
                modifier = Modifier.padding(top = 12.dp)
            )
            DiviDivider(Modifier.padding(vertical = 20.dp))
            SectionHeader("\uD83D\uDCBE Gesti\u00f3n de Datos")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassButton(
                    text = "Respaldar \u2B06\uFE0F",
                    onClick = onBackup,
                    tone = ButtonTone.Safe,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Restaurar \u2B07\uFE0F",
                    onClick = onRestore,
                    tone = ButtonTone.Safe,
                    modifier = Modifier.weight(1f)
                )
            }
            GlassButton(
                text = "\uD83D\uDDD1\uFE0F Eliminar Todos los Datos",
                onClick = { showDeleteConfirm = true },
                tone = ButtonTone.Danger,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }

    if (showDeleteConfirm) {
        DiviConfirmDialog(
            title = "\u26A0\uFE0F Eliminar Todo",
            message = "\u00bfSeguro que quieres eliminar TODOS tus datos? Esta acci\u00f3n no se puede deshacer.",
            onConfirm = {
                showDeleteConfirm = false
                onDeleteAll()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
