package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divifriends.xyon.pro.data.BalanceCalculator
import com.divifriends.xyon.pro.data.model.CATEGORY_OTHER
import com.divifriends.xyon.pro.data.model.Expense
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.ui.components.ButtonTone
import com.divifriends.xyon.pro.ui.components.CategoryPicker
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviDialog
import com.divifriends.xyon.pro.ui.components.DiviDropdown
import com.divifriends.xyon.pro.ui.components.DiviInnerShape
import com.divifriends.xyon.pro.ui.components.DiviLabel
import com.divifriends.xyon.pro.ui.components.DiviScreen
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.GlassButton
import com.divifriends.xyon.pro.ui.components.GlassCard
import com.divifriends.xyon.pro.ui.components.MainButton
import com.divifriends.xyon.pro.ui.components.PageTitle
import com.divifriends.xyon.pro.ui.components.ParticipantCheckItem
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.glass
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextSecondary

@Composable
fun GroupDetailScreen(
    group: Group,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddExpense: (String, Double, String, List<String>, String) -> Unit,
    onUpdateExpense: (String, String, Double, String, List<String>, String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onSettle: (String, String, Double) -> Unit,
    onExportText: () -> Unit,
    onExportCsv: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var settlementToConfirm by remember { mutableStateOf<Triple<String, String, Double>?>(null) }

    val expenses = remember(group.expenses, search) {
        group.expenses
            .filter { it.description.contains(search, ignoreCase = true) }
            .sortedByDescending { it.date }
    }

    DiviScreen {
        PageTitle(
            title = group.name,
            onBack = onBack,
            actionIcon = Icons.Filled.Edit,
            onAction = onEdit
        )

        GlassCard {
            DiviTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = "\uD83D\uDD0D Buscar gastos..."
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (expenses.isEmpty()) {
                    Text(
                        text = if (search.isBlank()) "\u2728 \u00a1A\u00f1ade el primer gasto del grupo! \u2728" else "No se encontraron gastos.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    expenses.forEach { expense ->
                        ExpenseRow(expense = expense, onClick = { editingExpense = expense })
                    }
                }
            }
            MainButton(
                text = "\uD83D\uDCB8 A\u00f1adir Gasto",
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(top = 20.dp)
            )
        }

        GlassCard {
            SectionHeader("Saldos del Grupo \u2696\uFE0F")
            val debts = remember(group.expenses, group.people) {
                BalanceCalculator.simplifyDebts(
                    BalanceCalculator.calculateBalances(group.expenses, group.people)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (debts.isEmpty()) {
                    Text(
                        text = "\u2705 \u00a1Las cuentas est\u00e1n saldadas! \u2705",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    debts.forEach { debt ->
                        DebtRow(
                            from = debt.from,
                            to = debt.to,
                            amount = debt.amount,
                            onSettle = {
                                settlementToConfirm = Triple(debt.from, debt.to, debt.amount)
                            }
                        )
                    }
                }
            }
        }

        GlassCard {
            SectionHeader("Exportar Grupo \uD83D\uDCC4")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassButton("Texto \uD83D\uDCCB", onExportText, Modifier.weight(1f))
                GlassButton("CSV \uD83D\uDCBE", onExportCsv, Modifier.weight(1f))
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            group = group,
            expense = null,
            onSave = { description, amount, paidBy, participants, category ->
                onAddExpense(description, amount, paidBy, participants, category)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingExpense?.let { expense ->
        AddExpenseDialog(
            group = group,
            expense = expense,
            onSave = { description, amount, paidBy, participants, category ->
                onUpdateExpense(expense.id, description, amount, paidBy, participants, category)
                editingExpense = null
            },
            onDelete = {
                expenseToDelete = expense
                editingExpense = null
            },
            onDismiss = { editingExpense = null }
        )
    }

    expenseToDelete?.let { expense ->
        DiviConfirmDialog(
            title = "\uD83D\uDDD1\uFE0F Eliminar Gasto",
            message = "\u00bfEst\u00e1s seguro de que quieres eliminar este gasto? Esta acci\u00f3n no se puede deshacer.",
            onConfirm = {
                onDeleteExpense(expense.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }

    settlementToConfirm?.let { (from, to, amount) ->
        DiviConfirmDialog(
            title = "\uD83E\uDD1D Saldar Deuda",
            message = "\u00bfConfirmas que $from ha pagado ${formatCurrency(amount)} a $to?",
            onConfirm = {
                onSettle(from, to, amount)
                settlementToConfirm = null
            },
            onDismiss = { settlementToConfirm = null }
        )
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape = DiviInnerShape, elevation = 2.dp)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(expense.category, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.description,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Pagado por ${expense.paidBy}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Text(
            text = formatCurrency(expense.amount),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DebtRow(from: String, to: String, amount: Double, onSettle: () -> Unit) {
    val palette = LocalDiviPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(shape = DiviInnerShape, elevation = 2.dp)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$from \u2192 $to",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatCurrency(amount),
                color = palette.secondary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        GlassButton(
            text = "Saldar",
            onClick = onSettle,
            tone = ButtonTone.Safe,
            modifier = Modifier.size(width = 110.dp, height = 46.dp)
        )
    }
}

@Composable
fun AddExpenseDialog(
    group: Group,
    expense: Expense?,
    onSave: (String, Double, String, List<String>, String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEditing = expense != null

    if (expense != null && expense.isSettlement) {
        DiviDialog(onDismiss = onDismiss) {
            Text(
                text = "\uD83E\uDD1D Pago de Liquidaci\u00f3n",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${expense.paidBy} pag\u00f3 ${formatCurrency(expense.amount)} a " +
                    expense.participants.firstOrNull().orEmpty() + ".",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (onDelete != null) {
                GlassButton(
                    text = "Eliminar Pago",
                    onClick = onDelete,
                    tone = ButtonTone.Danger,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
        return
    }

    var description by remember { mutableStateOf(expense?.description ?: "") }
    var amountText by remember { mutableStateOf(expense?.amount?.let { formatAmountInput(it) } ?: "") }
    var category by remember { mutableStateOf(expense?.category ?: CATEGORY_OTHER) }
    var paidBy by remember { mutableStateOf(expense?.paidBy ?: group.people.firstOrNull().orEmpty()) }
    var participants by remember {
        mutableStateOf((expense?.participants ?: group.people).toSet())
    }

    DiviDialog(onDismiss = onDismiss) {
        Text(
            text = if (isEditing) "\u270F\uFE0F Editar Gasto" else "\uD83D\uDCB8 A\u00f1adir Gasto",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(modifier = Modifier.padding(top = 16.dp)) {
            DiviTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descripci\u00f3n",
                placeholder = "Ej: Cena en el chiringuito"
            )
            DiviTextField(
                value = amountText,
                onValueChange = { amountText = sanitizeAmount(it) },
                label = "Importe (\u20ac)",
                placeholder = "0,00",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        DiviLabel("Categor\u00eda", Modifier.padding(top = 16.dp))
        CategoryPicker(
            categories = com.divifriends.xyon.pro.data.model.EXPENSE_CATEGORIES,
            selected = category,
            onSelect = { category = it }
        )
        DiviDropdown(
            options = group.people,
            selected = paidBy,
            optionLabel = { it },
            onSelect = { paidBy = it },
            label = "Pagado por",
            modifier = Modifier.padding(top = 16.dp)
        )
        DiviLabel("Participantes", Modifier.padding(top = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            group.people.forEach { person ->
                ParticipantCheckItem(
                    name = person,
                    checked = participants.contains(person),
                    onToggle = { checked ->
                        participants = if (checked) participants + person else participants - person
                    }
                )
            }
        }
        MainButton(
            text = if (isEditing) "Guardar Cambios" else "A\u00f1adir Gasto",
            onClick = {
                onSave(
                    description,
                    parseAmount(amountText),
                    paidBy,
                    participants.toList(),
                    category
                )
            },
            modifier = Modifier.padding(top = 20.dp)
        )
        if (isEditing && onDelete != null) {
            GlassButton(
                text = "Eliminar Gasto",
                onClick = onDelete,
                tone = ButtonTone.Danger,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

private fun sanitizeAmount(input: String): String {
    val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
    return filtered.replace('.', ',').take(12)
}

private fun parseAmount(input: String): Double =
    input.replace(',', '.').toDoubleOrNull() ?: 0.0

private fun formatAmountInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')
