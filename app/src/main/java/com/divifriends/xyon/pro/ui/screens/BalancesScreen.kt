package com.divifriends.xyon.pro.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.Debt
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.ui.components.ActionRow
import com.divifriends.xyon.pro.ui.components.ActionTile
import com.divifriends.xyon.pro.ui.components.AnimatedAmount
import com.divifriends.xyon.pro.ui.components.Avatar
import com.divifriends.xyon.pro.ui.components.AvatarStack
import com.divifriends.xyon.pro.ui.components.DiviCardShape
import com.divifriends.xyon.pro.ui.components.DiviPillShape
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.HeroPill
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.ProgressRing
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.SwipeAction
import com.divifriends.xyon.pro.ui.components.SwipeRow
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.staggeredAppear
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.Tabular

private data class PendingDebt(val group: Group, val debt: Debt)

@Composable
fun BalancesScreen(
    appData: AppData,
    onOpenGroup: (String) -> Unit,
    onAddExpense: () -> Unit,
    onSettle: (groupId: String, from: String, to: String, amount: Double) -> Unit
) {
    val palette = LocalDiviPalette.current
    val user = appData.config.userName
    val groups = remember(appData) { appData.groups.values.sortedBy { it.name.lowercase() } }
    val userBalances = remember(appData) { groups.map { it.balanceFor(user) } }
    val net = userBalances.sum()
    val owed = userBalances.filter { it > 0 }.sum()
    val owes = -userBalances.filter { it < 0 }.sum()
    val settledGroups = remember(appData) { groups.count { it.expenses.isNotEmpty() && it.debts().isEmpty() } }
    val activeGroups = groups.count { it.expenses.isNotEmpty() }
    val ratio = remember(appData) {
        val ratios = groups.filter { it.expenses.isNotEmpty() }.map { it.settledRatio() }
        if (ratios.isEmpty()) 0f else ratios.average().toFloat()
    }
    val friends = remember(appData) { appData.allPeople() }
    val pending = remember(appData) {
        groups.flatMap { g -> g.debts().map { PendingDebt(g, it) } }
    }
    // 0 = todos, 1 = te deben, 2 = debes
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val shown = when {
        user.isBlank() || filter == 0 -> pending
        filter == 1 -> pending.filter { it.debt.to == user }
        else -> pending.filter { it.debt.from == user }
    }

    HeroScaffold(
        compactTitle = "Saldos",
        hero = {
            HeroTopBar(title = "Saldos", caption = "Tu balance neto", modifier = Modifier.staggeredAppear(0))
            AnimatedAmount(
                value = net,
                color = palette.onHero,
                signed = true,
                modifier = Modifier.staggeredAppear(1)
            )
            Spacer(Modifier.height(18.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.heroChip)
            )
            Spacer(Modifier.height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.staggeredAppear(2)
            ) {
                ProgressRing(
                    progress = ratio,
                    label = "${(ratio * 100).toInt()}%",
                    size = 58.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Saldado", style = MaterialTheme.typography.titleMedium, color = palette.onHero)
                    Text(
                        text = if (activeGroups == 0) "Sin movimientos" else "$settledGroups de $activeGroups grupos en paz",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.onHeroMuted
                    )
                }
                HeroPill(
                    text = formatCurrency(owed),
                    icon = Icons.AutoMirrored.Rounded.CallReceived,
                    solid = owed > 0
                )
            }
        },
        sheet = {
            Spacer(Modifier.height(12.dp))
            ActionRow(
                title = "Añadir gasto",
                caption = "Apúntalo y lo repartimos al momento",
                onClick = onAddExpense,
                modifier = Modifier.staggeredAppear(0)
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredAppear(1),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FilterTile(
                    selected = filter == 1,
                    modifier = Modifier.weight(1f)
                ) {
                    ActionTile(
                        label = "Te deben",
                        caption = formatCurrency(owed),
                        icon = Icons.AutoMirrored.Rounded.CallReceived,
                        tone = Tone.Mint,
                        onClick = { filter = if (filter == 1) 0 else 1 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FilterTile(
                    selected = filter == 2,
                    modifier = Modifier.weight(1f)
                ) {
                    ActionTile(
                        label = "Debes",
                        caption = formatCurrency(owes),
                        icon = Icons.AutoMirrored.Rounded.CallMade,
                        tone = Tone.Aqua,
                        phase = 0.5f,
                        onClick = { filter = if (filter == 2) 0 else 2 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (friends.isNotEmpty()) {
                SectionHeader(title = "Amigos conectados")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredAppear(2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Tus amigos", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = buildString {
                                append("Tienes ")
                                append(friends.size)
                                append(if (friends.size == 1) " amigo" else " amigos")
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AvatarStack(names = friends, size = 40.dp, max = 3)
                }
            }

            SectionHeader(
                title = when (filter) {
                    1 -> "Te deben"
                    2 -> "Debes"
                    else -> "Pagos pendientes"
                }
            )
            Column(
                modifier = Modifier.animateContentSize(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (shown.isEmpty()) {
                    EmptyState(
                        emoji = "🤝",
                        title = if (groups.isEmpty()) "Sin saldos" else "Todo en paz",
                        message = if (groups.isEmpty()) "Crea un grupo para empezar." else "No hay pagos pendientes aquí."
                    )
                } else {
                    Text(
                        text = "Desliza a la derecha para saldar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    shown.forEachIndexed { index, item ->
                        DebtRow(
                            item = item,
                            user = user,
                            onOpen = { onOpenGroup(item.group.id) },
                            onSettle = { onSettle(item.group.id, item.debt.from, item.debt.to, item.debt.amount) },
                            modifier = Modifier.staggeredAppear(index)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun FilterTile(selected: Boolean, modifier: Modifier, content: @Composable () -> Unit) {
    val palette = LocalDiviPalette.current
    Column(
        modifier = modifier
            .border(2.dp, if (selected) palette.accent else Color.Transparent, DiviCardShape)
            .padding(if (selected) 3.dp else 0.dp)
    ) { content() }
}

@Composable
private fun DebtRow(
    item: PendingDebt,
    user: String,
    onOpen: () -> Unit,
    onSettle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDiviPalette.current
    val debt = item.debt
    val mine = debt.from == user
    SwipeRow(
        modifier = modifier,
        startAction = SwipeAction(
            icon = Icons.Rounded.Handshake,
            label = "Saldar",
            color = palette.positive,
            contentColor = Color.White,
            onTrigger = onSettle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(pressedScale = 0.98f, feedback = { it.selection() }, onClick = onOpen)
                .clip(DiviCardShape)
                .background(palette.raised)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar(name = debt.from, size = 42.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${debt.from} → ${debt.to}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.group.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(debt.amount),
                    style = MaterialTheme.typography.titleSmall.merge(Tabular).copy(fontWeight = FontWeight.Bold),
                    color = if (mine) palette.negative else if (debt.to == user) palette.positive else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .bounceClick(pressedScale = 0.88f, feedback = { it.success() }, onClick = onSettle)
                        .clip(DiviPillShape)
                        .background(palette.accentSoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Check, null, tint = palette.accentDeep, modifier = Modifier.size(14.dp))
                    Text("Saldar", style = MaterialTheme.typography.labelSmall, color = palette.accentDeep)
                }
            }
        }
    }
}
