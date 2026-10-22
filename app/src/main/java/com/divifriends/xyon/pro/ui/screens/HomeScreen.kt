package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.CATEGORY_SETTLEMENT
import com.divifriends.xyon.pro.ui.components.ActionTile
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.GroupCard
import com.divifriends.xyon.pro.ui.components.HeroIconButton
import com.divifriends.xyon.pro.ui.components.HeroPill
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.PrimaryButton
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.components.TransactionRow
import com.divifriends.xyon.pro.ui.components.WaveChart
import com.divifriends.xyon.pro.ui.components.bleed
import com.divifriends.xyon.pro.ui.components.signedCurrency
import com.divifriends.xyon.pro.ui.components.todayLabel
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.staggeredAppear
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

@Composable
fun HomeScreen(
    appData: AppData,
    onOpenGroup: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onOpenGroups: () -> Unit,
    onAddExpense: (String) -> Unit,
    onOpenReport: () -> Unit,
    onOpenSettings: () -> Unit,
    onNfcInvite: () -> Unit,
    onNfcScan: () -> Unit
) {
    val palette = LocalDiviPalette.current
    val user = appData.config.userName
    val groups = remember(appData) {
        appData.groups.values.sortedByDescending { g -> g.expenses.maxOfOrNull { it.date } ?: "" }
    }
    var range by rememberSaveable { mutableIntStateOf(6) }
    val months = remember(range) { lastMonths(range) }
    val totals = remember(appData, months) {
        monthlyTotals(appData.groups.values.flatMap { it.expenses }, months)
    }
    val change = remember(totals) { percentChange(totals) }
    val recent = remember(appData) { appData.recentExpenses(8) }

    HeroScaffold(
        compactTitle = if (user.isBlank()) "Mis cuentas" else "Hola, $user",
        hero = {
            HeroTopBar(
                title = if (user.isBlank()) "Mis cuentas" else "Hola, $user",
                caption = todayLabel(),
                actionIcon = Icons.Rounded.Settings,
                onAction = onOpenSettings,
                actionDescription = "Ajustes",
                modifier = Modifier.staggeredAppear(0)
            )

            Row(
                modifier = Modifier
                    .bleed(22.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroIconButton(
                    icon = Icons.Rounded.Add,
                    onClick = onCreateGroup,
                    size = 52.dp,
                    contentDescription = "Nuevo grupo",
                    modifier = Modifier.staggeredAppear(1)
                )
                if (groups.isEmpty()) {
                    GroupCard(
                        name = "Tu primer grupo",
                        amount = "¡Empieza!",
                        caption = "Toca para crear",
                        emoji = "✨",
                        tone = Tone.Sun,
                        onClick = onCreateGroup,
                        modifier = Modifier.staggeredAppear(2)
                    )
                }
                groups.forEachIndexed { index, group ->
                    val balance = group.balanceFor(user)
                    val tone = when {
                        balance < -0.005 -> Tone.Sun
                        balance > 0.005 -> Tone.Plain
                        else -> Tone.Mint
                    }
                    val last = group.expenses.maxByOrNull { it.date }
                    GroupCard(
                        name = group.name,
                        amount = when {
                            user.isBlank() -> formatCurrency(group.totalSpent())
                            kotlin.math.abs(balance) < 0.005 -> "En paz"
                            else -> signedCurrency(balance)
                        },
                        caption = last?.shortDateLabel()?.takeIf { it.isNotEmpty() } ?: "${group.people.size} personas",
                        emoji = group.signatureEmoji(),
                        tone = tone,
                        onClick = { onOpenGroup(group.id) },
                        modifier = Modifier.staggeredAppear(index + 2)
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredAppear(4),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .bounceClick(pressedScale = 0.97f, feedback = { it.selection() }, onClick = onOpenReport)
                ) {
                    Text(
                        text = change ?: "Gasto mensual",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.onHeroMuted
                    )
                    Text(
                        text = "Estadística",
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.onHero
                    )
                }
                HeroPill(
                    text = if (range == 6) "6 meses" else "12 meses",
                    trailingIcon = Icons.Rounded.ExpandMore,
                    onClick = { range = if (range == 6) 12 else 6 }
                )
            }
            Spacer(Modifier.height(8.dp))
            WaveChart(
                values = totals,
                labels = months.map { m -> if (range == 12) monthLabel(m).take(1) else monthLabel(m) },
                valueLabel = { formatCurrency(it) },
                modifier = Modifier
                    .bleed(8.dp)
                    .staggeredAppear(5)
            )
        },
        sheet = {
            SectionHeader(
                title = "Historial",
                actionLabel = if (groups.isNotEmpty()) "Grupos" else null,
                onAction = onOpenGroups
            )
            if (recent.isEmpty()) {
                EmptyState(
                    emoji = "🧾",
                    title = "Todavía no hay movimientos",
                    message = if (groups.isEmpty()) "Crea un grupo e invita a tus amigos para empezar a repartir."
                    else "Añade el primer gasto y aparecerá aquí.",
                    action = {
                        PrimaryButton(
                            text = if (groups.isEmpty()) "Crear grupo" else "Añadir gasto",
                            onClick = { if (groups.isEmpty()) onCreateGroup() else onAddExpense(groups.first().id) },
                            leadingIcon = Icons.Rounded.Add,
                            modifier = Modifier.width(220.dp)
                        )
                    }
                )
            } else {
                recent.forEachIndexed { index, entry ->
                    val e = entry.expense
                    val effect = e.effectFor(user)
                    val settlement = e.isSettlement || e.category == CATEGORY_SETTLEMENT
                    TransactionRow(
                        name = e.paidBy,
                        emoji = if (settlement) CATEGORY_SETTLEMENT else null,
                        title = if (settlement) "${e.paidBy} → ${e.participants.firstOrNull().orEmpty()}" else e.paidBy,
                        subtitle = if (settlement) "Liquidación · ${entry.group.name}" else "${e.category} ${e.description} · ${entry.group.name}",
                        amount = when {
                            settlement || kotlin.math.abs(effect) < 0.005 -> formatCurrency(e.amount)
                            else -> signedCurrency(effect)
                        },
                        amountColor = when {
                            settlement || kotlin.math.abs(effect) < 0.005 -> MaterialTheme.colorScheme.onSurface
                            effect > 0 -> palette.positive
                            else -> palette.negative
                        },
                        amountCaption = e.shortDateLabel(),
                        onClick = { onOpenGroup(entry.group.id) },
                        modifier = Modifier.staggeredAppear(index)
                    )
                }
            }

            SectionHeader(title = "Con tus amigos")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionTile(
                    label = "Invitar",
                    caption = "Comparte un grupo por NFC",
                    icon = Icons.Rounded.Nfc,
                    tone = Tone.Aqua,
                    onClick = onNfcInvite,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Unirme",
                    caption = "Acerca los móviles",
                    icon = Icons.Rounded.Contactless,
                    tone = Tone.Mint,
                    onClick = onNfcScan,
                    phase = 0.5f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}
