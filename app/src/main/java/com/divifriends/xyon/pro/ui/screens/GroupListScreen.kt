package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.BalanceCalculator
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.ui.components.DiviHeader
import com.divifriends.xyon.pro.ui.components.DiviScreen
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.GlassButton
import com.divifriends.xyon.pro.ui.components.GlassCard
import com.divifriends.xyon.pro.ui.components.MainButton
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextSecondary

@Composable
fun GroupListScreen(
    appData: AppData,
    onOpenGroup: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onNfcSend: () -> Unit,
    onNfcReceive: () -> Unit
) {
    val groups = appData.groups.values.toList()
    DiviScreen {
        DiviHeader(subtitle = "Tus Grupos de Gasto \uD83D\uDC65")

        if (groups.isEmpty()) {
            EmptyState("\uD83E\uDD37", "A\u00fan no tienes grupos. \u00a1Crea uno para empezar!")
        } else {
            groups.forEach { group ->
                GroupCard(
                    name = group.name,
                    peopleCount = group.people.size,
                    total = BalanceCalculator.totalSpent(group.expenses),
                    onClick = { onOpenGroup(group.id) }
                )
            }
        }

        GlassCard {
            MainButton(text = "\u2795 Crear Nuevo Grupo", onClick = onCreateGroup)
        }

        GlassCard {
            Text(
                text = "Invitar a Amigos por NFC \uD83E\uDD1D",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassButton(
                    text = "Enviar \uD83D\uDCE4",
                    onClick = onNfcSend,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Recibir \uD83D\uDCE5",
                    onClick = onNfcReceive,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GroupCard(
    name: String,
    peopleCount: Int,
    total: Double,
    onClick: () -> Unit
) {
    val palette = LocalDiviPalette.current
    GlassCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\uD83D\uDC65 $peopleCount personas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Text(
                text = formatCurrency(total),
                style = MaterialTheme.typography.titleLarge,
                color = palette.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
