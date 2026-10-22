package com.divifriends.xyon.pro.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.divifriends.xyon.pro.data.model.CATEGORY_OTHER
import com.divifriends.xyon.pro.data.model.EXPENSE_CATEGORIES
import com.divifriends.xyon.pro.data.model.Expense
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.data.model.categoryLabel
import com.divifriends.xyon.pro.ui.components.AmountDisplay
import com.divifriends.xyon.pro.ui.components.AmountKeypad
import com.divifriends.xyon.pro.ui.components.AnimatedAmount
import com.divifriends.xyon.pro.ui.components.Avatar
import com.divifriends.xyon.pro.ui.components.AvatarStack
import com.divifriends.xyon.pro.ui.components.BalanceBars
import com.divifriends.xyon.pro.ui.components.CategoryChips
import com.divifriends.xyon.pro.ui.components.DiviCardShape
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviPillShape
import com.divifriends.xyon.pro.ui.components.DiviSheet
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.FieldLabel
import com.divifriends.xyon.pro.ui.components.HeroPill
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.PeoplePicker
import com.divifriends.xyon.pro.ui.components.PrimaryButton
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.SegmentedControl
import com.divifriends.xyon.pro.ui.components.SoftButton
import com.divifriends.xyon.pro.ui.components.SwipeAction
import com.divifriends.xyon.pro.ui.components.SwipeRow
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.components.TransactionRow
import com.divifriends.xyon.pro.ui.components.applyAmountKey
import com.divifriends.xyon.pro.ui.components.parseAmount
import com.divifriends.xyon.pro.ui.components.signedCurrency
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.shakeOnError
import com.divifriends.xyon.pro.ui.motion.staggeredAppear
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.mono

@Composable
fun GroupDetailScreen(
    group: Group,
    userName: String,
    openAddOnStart: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddExpense: (String, Double, String, List<String>, String) -> Unit,
    onUpdateExpense: (String, String, Double, String, List<String>, String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onSettle: (String, String, Double) -> Unit,
    onExportText: () -> Unit,
    onExportCsv: () -> Unit
) {
    val palette = LocalDiviPalette.current
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var consumedStartAdd by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Expense?>(null) }
    var deleting by remember { mutableStateOf<Expense?>(null) }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(openAddOnStart) {
        if (openAddOnStart && !consumedStartAdd) {
            consumedStartAdd = true
            showAdd = true
        }
    }

    val balances = remember(group.expenses, group.people) { group.balances() }
    val debts = remember(balances) { group.debts() }
    val total = remember(group.expenses) { group.totalSpent() }
    val byDay = remember(group.expenses) {
        group.expenses.sortedByDescending { it.date }.groupBy { it.localDate() }
    }

    HeroScaffold(
        compactTitle = group.name,
        onBack = onBack,
        hero = {
            HeroTopBar(
                title = group.name,
                caption = "${group.people.size} personas · ${group.expenses.count { !it.isSettlement }} gastos",
                onBack = onBack,
                actionIcon = Icons.Rounded.Edit,
                onAction = onEdit,
                actionDescription = "Editar grupo"
            )
            Text("Total del grupo", style = MaterialTheme.typography.labelLarge, color = palette.onHeroMuted)
            AnimatedAmount(value = total, color = palette.onHero)
            if (userName in group.people) {
                val mine = balances[userName] ?: 0.0
                Text(
                    text = when {
                        mine > 0.005 -> "Te deben ${formatCurrency(mine)}"
                        mine < -0.005 -> "Debes ${formatCurrency(-mine)}"
                        else -> "Estás en paz ✨"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (mine < -0.005) palette.sun else palette.onHero
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(names = group.people, size = 36.dp, max = 4, ringColor = palette.heroEnd)
                Spacer(Modifier.weight(1f))
                HeroPill(text = "Gasto", icon = Icons.Rounded.Add, solid = true, onClick = { showAdd = true })
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroPill(text = "Compartir", icon = Icons.AutoMirrored.Rounded.Send, onClick = onExportText)
                HeroPill(text = "CSV", icon = Icons.Rounded.Download, onClick = onExportCsv)
            }
        },
        sheet = {
            Spacer(Modifier.height(12.dp))
            SegmentedControl(
                options = listOf("Gastos", if (debts.isEmpty()) "Saldos" else "Saldos · ${debts.size}"),
                selectedIndex = tab,
                onSelect = { tab = it }
            )
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 3 * dir } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(180)) { -it / 3 * dir } + fadeOut(tween(150)))
                },
                label = "groupTab"
            ) { current ->
                Column(Modifier.fillMaxWidth()) {
                    if (current == 0) {
                        ExpensesTab(
                            byDay = byDay,
                            user = userName,
                            onEdit = { editing = it },
                            onDelete = { deleting = it },
                            onAdd = { showAdd = true }
                        )
                    } else {
                        BalancesTab(
                            balances = group.people.map { it to (balances[it] ?: 0.0) }.sortedByDescending { it.second },
                            debts = debts,
                            user = userName,
                            onSettle = onSettle
                        )
                    }
                }
            }
        }
    )

    if (showAdd) {
        ExpenseSheet(
            group = group,
            expense = null,
            userName = userName,
            onSave = { description, amount, paidBy, participants, category ->
                onAddExpense(description, amount, paidBy, participants, category)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }

    editing?.let { expense ->
        ExpenseSheet(
            group = group,
            expense = expense,
            userName = userName,
            onSave = { description, amount, paidBy, participants, category ->
                onUpdateExpense(expense.id, description, amount, paidBy, participants, category)
                editing = null
            },
            onDelete = {
                editing = null
                deleting = expense
            },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { expense ->
        DiviConfirmDialog(
            title = if (expense.isSettlement) "Eliminar pago" else "Eliminar gasto",
            message = "¿Seguro que quieres eliminar \"${expense.description}\" (${formatCurrency(expense.amount)})?",
            icon = Icons.Rounded.DeleteOutline,
            confirmText = "Eliminar",
            onConfirm = {
                onDeleteExpense(expense.id)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun ExpensesTab(
    byDay: Map<java.time.LocalDate?, List<Expense>>,
    user: String,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    onAdd: () -> Unit
) {
    val palette = LocalDiviPalette.current
    if (byDay.isEmpty()) {
        EmptyState(
            emoji = "🧾",
            title = "Sin gastos",
            message = "Añade el primero y lo repartimos al céntimo.",
            action = { SoftButton(text = "Añadir gasto", onClick = onAdd, tone = Tone.Lime, leadingIcon = Icons.Rounded.Add) }
        )
        return
    }
    var index = 0
    byDay.forEach { (day, expenses) ->
        Text(
            text = dayHeader(day),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
        )
        expenses.forEach { e ->
            val effect = e.effectFor(user)
            SwipeRow(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .staggeredAppear(index++),
                endAction = SwipeAction(
                    icon = Icons.Rounded.DeleteOutline,
                    label = "Eliminar",
                    color = palette.negative,
                    contentColor = Color.White,
                    onTrigger = { onDelete(e) }
                )
            ) {
                TransactionRow(
                    name = e.paidBy,
                    emoji = e.category,
                    title = e.description,
                    subtitle = if (e.isSettlement) "${e.paidBy} → ${e.participants.firstOrNull().orEmpty()}"
                    else "Pagó ${e.paidBy} · ${e.participants.size} personas",
                    amount = formatCurrency(e.amount),
                    amountColor = MaterialTheme.colorScheme.onSurface,
                    amountCaption = when {
                        e.isSettlement || kotlin.math.abs(effect) < 0.005 -> categoryLabel(e.category)
                        else -> signedCurrency(effect)
                    },
                    background = palette.sheet,
                    onClick = { onEdit(e) }
                )
            }
        }
    }
    Text(
        text = "Desliza a la izquierda para eliminar",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
    )
}

@Composable
private fun BalancesTab(
    balances: List<Pair<String, Double>>,
    debts: List<com.divifriends.xyon.pro.data.model.Debt>,
    user: String,
    onSettle: (String, String, Double) -> Unit
) {
    val palette = LocalDiviPalette.current
    SectionHeader("Balance por persona")
    BalanceBars(balances = balances, format = { signedCurrency(it) })

    SectionHeader("Para quedar en paz")
    if (debts.isEmpty()) {
        EmptyState(emoji = "🤝", title = "Todos en paz", message = "Nadie debe nada a nadie. ¡Así da gusto!")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        debts.forEachIndexed { index, debt ->
            SwipeRow(
                modifier = Modifier.staggeredAppear(index),
                startAction = SwipeAction(
                    icon = Icons.Rounded.Handshake,
                    label = "Saldar",
                    color = palette.positive,
                    contentColor = Color.White,
                    onTrigger = { onSettle(debt.from, debt.to, debt.amount) }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DiviCardShape)
                        .background(palette.raised)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Avatar(name = debt.from, size = 38.dp)
                    Icon(Icons.AutoMirrored.Rounded.Send, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Avatar(name = debt.to, size = 38.dp)
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${debt.from} → ${debt.to}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatCurrency(debt.amount),
                            style = MaterialTheme.typography.labelLarge.mono().copy(fontWeight = FontWeight.Bold),
                            color = when (user) {
                                debt.from -> palette.negative
                                debt.to -> palette.positive
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .bounceClick(pressedScale = 0.88f, feedback = { it.success() }) {
                                onSettle(debt.from, debt.to, debt.amount)
                            }
                            .clip(DiviPillShape)
                            .background(palette.accent)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = palette.onAccent, modifier = Modifier.size(16.dp))
                        Text("Saldar", style = MaterialTheme.typography.labelMedium, color = palette.onAccent)
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Expense sheet: step 1 amount keypad, step 2 details                *
 * ------------------------------------------------------------------ */

@Composable
fun ExpenseSheet(
    group: Group,
    expense: Expense?,
    userName: String,
    onSave: (String, Double, String, List<String>, String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current

    if (expense != null && expense.isSettlement) {
        DiviSheet(onDismiss = onDismiss) {
            Text("Pago de liquidación", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(name = expense.paidBy, size = 48.dp)
                Icon(Icons.AutoMirrored.Rounded.Send, null, tint = palette.accent)
                Avatar(name = expense.participants.firstOrNull().orEmpty(), size = 48.dp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${expense.paidBy} pagó ${formatCurrency(expense.amount)} a ${expense.participants.firstOrNull().orEmpty()}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            if (onDelete != null) {
                SoftButton(text = "Eliminar pago", onClick = onDelete, tone = Tone.Danger, leadingIcon = Icons.Rounded.DeleteOutline, heavy = true)
            }
        }
        return
    }

    var step by rememberSaveable { mutableIntStateOf(if (expense != null) 1 else 0) }
    var amountText by rememberSaveable { mutableStateOf(expense?.amount?.let { formatAmountInput(it) } ?: "") }
    var description by rememberSaveable { mutableStateOf(expense?.description ?: "") }
    var paidBy by rememberSaveable {
        mutableStateOf(expense?.paidBy ?: userName.takeIf { it in group.people } ?: group.people.firstOrNull().orEmpty())
    }
    var participants by remember { mutableStateOf(expense?.participants ?: group.people) }
    var category by rememberSaveable { mutableStateOf(expense?.category ?: CATEGORY_OTHER) }
    var shake by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    fun fail(message: String) {
        haptics?.error()
        shake++
        error = message
    }

    DiviSheet(onDismiss = onDismiss) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it * dir } + fadeIn())
                    .togetherWith(slideOutHorizontally(tween(200)) { -it * dir } + fadeOut(tween(150)))
            },
            label = "expenseStep"
        ) { current ->
            Column(Modifier.fillMaxWidth()) {
                if (current == 0) {
                    Text(
                        text = if (expense != null) "Editar importe" else "¿Cuánto ha costado?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountDisplay(
                        text = amountText,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 22.dp)
                            .shakeOnError(shake)
                    )
                    AmountKeypad(onKey = {
                        amountText = applyAmountKey(amountText, it)
                        error = null
                    })
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(
                        text = "Continuar",
                        onClick = {
                            val amount = parseAmount(amountText)
                            if (amount == null || amount <= 0.0) fail("Introduce un importe mayor que 0")
                            else {
                                error = null
                                step = 1
                            }
                        }
                    )
                } else {
                    val amount = parseAmount(amountText) ?: 0.0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (expense != null) "Editar gasto" else "Nuevo gasto",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(group.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier
                                .bounceClick(feedback = { it.selection() }) { step = 0 }
                                .clip(DiviPillShape)
                                .background(palette.accentSoft)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                formatCurrency(amount),
                                style = MaterialTheme.typography.titleMedium.mono(),
                                color = palette.accentDeep
                            )
                            Icon(Icons.Rounded.Edit, null, tint = palette.accentDeep, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    DiviTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            error = null
                        },
                        label = "Concepto",
                        placeholder = "Ej. Cena en el puerto",
                        modifier = Modifier.shakeOnError(shake)
                    )
                    Spacer(Modifier.height(18.dp))
                    FieldLabel("Categoría")
                    CategoryChips(categories = EXPENSE_CATEGORIES, selected = category, onSelect = { category = it })
                    Spacer(Modifier.height(18.dp))
                    FieldLabel("Pagó")
                    PeoplePicker(
                        people = group.people,
                        isSelected = { it == paidBy },
                        onToggle = { paidBy = it }
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FieldLabel("Entre quiénes", modifier = Modifier.weight(1f))
                        val all = participants.size == group.people.size
                        Text(
                            text = if (all) "Ninguno" else "Todos",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accent,
                            modifier = Modifier
                                .bounceClick(feedback = { it.toggle() }) {
                                    participants = if (all) emptyList() else group.people
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    PeoplePicker(
                        people = group.people,
                        isSelected = { it in participants },
                        onToggle = { p ->
                            participants = if (p in participants) participants - p else participants + p
                            error = null
                        },
                        modifier = Modifier.shakeOnError(shake)
                    )
                    if (participants.isNotEmpty() && amount > 0) {
                        Text(
                            text = "${formatCurrency(amount / participants.size)} por persona",
                            style = MaterialTheme.typography.labelLarge.mono(),
                            color = palette.accent,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.negative,
                            modifier = Modifier.padding(top = 10.dp, start = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = if (expense != null) "Guardar cambios" else "Añadir gasto",
                        leadingIcon = Icons.Rounded.Check,
                        onClick = {
                            when {
                                description.isBlank() -> fail("Ponle un concepto al gasto")
                                participants.isEmpty() -> fail("Elige al menos una persona")
                                amount <= 0.0 -> {
                                    step = 0
                                    fail("Introduce un importe mayor que 0")
                                }
                                else -> onSave(description, amount, paidBy, participants, category)
                            }
                        }
                    )
                    if (onDelete != null) {
                        Spacer(Modifier.height(10.dp))
                        SoftButton(text = "Eliminar gasto", onClick = onDelete, tone = Tone.Danger, leadingIcon = Icons.Rounded.DeleteOutline)
                    }
                }
                if (current == 0) {
                    error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.negative,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmountInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(java.util.Locale.US, "%.2f", value).trimEnd('0').replace('.', ',')
