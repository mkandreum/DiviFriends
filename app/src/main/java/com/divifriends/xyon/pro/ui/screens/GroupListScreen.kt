package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.ui.components.ActionRow
import com.divifriends.xyon.pro.ui.components.ActionTile
import com.divifriends.xyon.pro.ui.components.AnimatedAmount
import com.divifriends.xyon.pro.ui.components.Avatar
import com.divifriends.xyon.pro.ui.components.AvatarStack
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.FillBar
import com.divifriends.xyon.pro.ui.components.HeroPill
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.SurfaceCard
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.motion.staggeredAppear
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.mono

@Composable
fun GroupListScreen(
    appData: AppData,
    onOpenGroup: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onNfcSend: () -> Unit,
    onNfcReceive: () -> Unit
) {
    val palette = LocalDiviPalette.current
    val groups = remember(appData) { appData.groups.values.sortedBy { it.name.lowercase() } }
    val total = remember(appData) { groups.sumOf { it.totalSpent() } }
    val people = remember(appData) { appData.allPeople() }
    val expenseCount = remember(appData) { groups.sumOf { g -> g.expenses.count { !it.isSettlement } } }

    HeroScaffold(
        compactTitle = "Grupos",
        hero = {
            HeroTopBar(
                title = "Grupos",
                caption = if (groups.isEmpty()) "Empieza creando uno" else "${groups.size} activos",
                modifier = Modifier.staggeredAppear(0)
            )
            Text(
                text = "Gastado entre todos",
                style = MaterialTheme.typography.labelLarge,
                color = palette.onHeroMuted,
                modifier = Modifier.staggeredAppear(1)
            )
            AnimatedAmount(value = total, color = palette.onHero, modifier = Modifier.staggeredAppear(1))
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.staggeredAppear(2)
            ) {
                HeroPill(text = "${people.size} amigos", icon = Icons.Rounded.Groups)
                HeroPill(text = "$expenseCount gastos", icon = Icons.AutoMirrored.Rounded.ReceiptLong)
            }
        },
        sheet = {
            Spacer(Modifier.height(12.dp))
            ActionRow(
                title = "Crear grupo",
                caption = "Viajes, cenas, el piso…",
                onClick = onCreateGroup
            )

            SectionHeader(title = "Tus grupos")
            if (groups.isEmpty()) {
                EmptyState(
                    emoji = "👥",
                    title = "Sin grupos todavía",
                    message = "Crea tu primer grupo para empezar a repartir gastos."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    groups.forEachIndexed { index, group ->
                        val ratio = group.settledRatio()
                        val pending = group.debts().size
                        SurfaceCard(
                            onClick = { onOpenGroup(group.id) },
                            modifier = Modifier.staggeredAppear(index)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Avatar(name = group.name, emoji = group.signatureEmoji(), size = 50.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (pending == 0) "Todo en paz ✨" else "$pending pagos pendientes",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (pending == 0) palette.positive else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatCurrency(group.totalSpent()),
                                    style = MaterialTheme.typography.titleSmall.mono(),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarStack(
                                    names = group.people,
                                    size = 30.dp,
                                    max = 4,
                                    ringColor = palette.raised
                                )
                                Spacer(Modifier.weight(1f))
                                Column(Modifier.fillMaxWidth(0.5f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${(ratio * 100).toInt()} % saldado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    FillBar(fraction = ratio, color = palette.accent, delayMillis = 200 + index * 70)
                                }
                            }
                        }
                    }
                }
            }

            SectionHeader(title = "Compartir")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionTile(
                    label = "Invitar",
                    caption = "Envía un grupo",
                    icon = Icons.Rounded.Nfc,
                    tone = Tone.Lime,
                    onClick = onNfcSend,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Unirme",
                    caption = "Recibe un grupo",
                    icon = Icons.Rounded.Contactless,
                    tone = Tone.Blush,
                    onClick = onNfcReceive,
                    phase = 0.5f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}
