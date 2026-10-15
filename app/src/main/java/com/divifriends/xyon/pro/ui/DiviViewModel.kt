package com.divifriends.xyon.pro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.divifriends.xyon.pro.data.AppRepository
import com.divifriends.xyon.pro.data.BalanceCalculator
import com.divifriends.xyon.pro.data.FileAppDataStore
import com.divifriends.xyon.pro.data.model.ActivityLog
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.CATEGORY_SETTLEMENT
import com.divifriends.xyon.pro.data.model.Expense
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.data.model.categoryLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface UiEvent {
    data class Toast(val text: String) : UiEvent
    data object MoneyRain : UiEvent
}

class DiviViewModel(private val repository: AppRepository) : ViewModel() {

    val appData: StateFlow<AppData> = repository.data

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun toast(text: String) {
        viewModelScope.launch { _events.send(UiEvent.Toast(text)) }
    }

    private fun moneyRain() {
        viewModelScope.launch { _events.send(UiEvent.MoneyRain) }
    }

    // ---------------------------------------------------------------- config

    fun setUserName(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) {
            toast("Por favor, introduce tu nombre.")
            return
        }
        repository.mutate { it.copy(config = it.config.copy(userName = clean)) }
    }

    fun changeTheme(themeKey: String, silent: Boolean = false) {
        repository.mutate { it.copy(config = it.config.copy(theme = themeKey)) }
        if (!silent) toast("\uD83C\uDFA8 Tema cambiado")
    }

    // ---------------------------------------------------------------- groups

    fun createGroup(name: String, people: List<String>) {
        val cleanName = name.trim()
        val cleanPeople = people.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (cleanName.isEmpty() || cleanPeople.size < 2) {
            toast("Completa el nombre y al menos dos participantes. \uD83E\uDDD0")
            return
        }
        val id = newId("group")
        repository.mutate { data ->
            data.copy(
                groups = data.groups + (id to Group(id = id, name = cleanName, people = cleanPeople)),
                activities = appendActivity(data.activities, "Se cre\u00f3 el grupo \"$cleanName\" \uD83C\uDF89.")
            )
        }
        toast("\u00a1Grupo \"$cleanName\" creado con \u00e9xito! \uD83E\uDD73")
        moneyRain()
    }

    fun renameGroup(groupId: String, newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) {
            toast("El nombre del grupo no puede estar vac\u00edo.")
            return
        }
        repository.mutate { data ->
            val group = data.groups[groupId] ?: return@mutate data
            val old = group.name
            data.copy(
                groups = data.groups + (groupId to group.copy(name = clean)),
                activities = appendActivity(data.activities, "El grupo \"$old\" ahora se llama \"$clean\".")
            )
        }
        toast("Nombre del grupo actualizado.")
    }

    fun addParticipant(groupId: String, name: String) {
        val clean = name.trim()
        val group = repository.data.value.groups[groupId]
        if (group == null || clean.isEmpty() || group.people.contains(clean)) {
            toast("Introduce un nombre v\u00e1lido y que no exista ya en el grupo.")
            return
        }
        repository.mutate { data ->
            val g = data.groups[groupId] ?: return@mutate data
            data.copy(
                groups = data.groups + (groupId to g.copy(people = g.people + clean)),
                activities = appendActivity(data.activities, "$clean se ha unido al grupo \"${g.name}\".")
            )
        }
    }

    fun removeParticipant(groupId: String, participant: String) {
        val group = repository.data.value.groups[groupId] ?: return
        val hasExpenses = group.expenses.any {
            it.paidBy == participant || it.participants.contains(participant)
        }
        if (hasExpenses) {
            toast("No se puede eliminar a un participante con gastos asociados.")
            return
        }
        repository.mutate { data ->
            val g = data.groups[groupId] ?: return@mutate data
            data.copy(
                groups = data.groups + (groupId to g.copy(people = g.people - participant)),
                activities = appendActivity(data.activities, "$participant ha sido eliminado del grupo \"${g.name}\".")
            )
        }
    }

    fun deleteGroup(groupId: String) {
        repository.mutate { data ->
            val group = data.groups[groupId] ?: return@mutate data
            data.copy(
                groups = data.groups - groupId,
                activities = appendActivity(data.activities, "Se elimin\u00f3 el grupo \"${group.name}\".")
            )
        }
        toast("Grupo eliminado.")
    }

    // --------------------------------------------------------------- expenses

    fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        paidBy: String,
        participants: List<String>,
        category: String
    ) {
        val clean = description.trim()
        if (clean.isEmpty() || amount <= 0.0 || participants.isEmpty()) {
            toast("Completa todos los campos correctamente. \uD83E\uDDD0")
            return
        }
        repository.mutate { data ->
            val group = data.groups[groupId] ?: return@mutate data
            val expense = Expense(
                id = newId("exp"),
                description = clean,
                amount = BalanceCalculator.round(amount),
                paidBy = paidBy,
                participants = participants,
                category = category,
                date = isoNow()
            )
            data.copy(
                groups = data.groups + (groupId to group.copy(expenses = group.expenses + expense)),
                activities = appendActivity(
                    data.activities,
                    "Se a\u00f1adi\u00f3 un gasto de ${formatCurrency(amount)} por \"$clean\" en \"${group.name}\"."
                )
            )
        }
        toast("Gasto a\u00f1adido \uD83D\uDCB8")
        moneyRain()
    }

    fun updateExpense(
        groupId: String,
        expenseId: String,
        description: String,
        amount: Double,
        paidBy: String,
        participants: List<String>,
        category: String
    ) {
        val clean = description.trim()
        if (clean.isEmpty() || amount <= 0.0 || participants.isEmpty()) {
            toast("Completa todos los campos correctamente. \uD83E\uDDD0")
            return
        }
        repository.mutate { data ->
            val group = data.groups[groupId] ?: return@mutate data
            val updated = group.expenses.map { expense ->
                if (expense.id == expenseId) {
                    expense.copy(
                        description = clean,
                        amount = BalanceCalculator.round(amount),
                        paidBy = paidBy,
                        participants = participants,
                        category = category
                    )
                } else expense
            }
            data.copy(
                groups = data.groups + (groupId to group.copy(expenses = updated)),
                activities = appendActivity(
                    data.activities,
                    "Se edit\u00f3 un gasto de ${formatCurrency(amount)} en \"${group.name}\"."
                )
            )
        }
        toast("Gasto actualizado \uD83D\uDCBE")
    }

    fun deleteExpense(groupId: String, expenseId: String) {
        repository.mutate { data ->
            val group = data.groups[groupId] ?: return@mutate data
            val expense = group.expenses.firstOrNull { it.id == expenseId }
            data.copy(
                groups = data.groups + (groupId to group.copy(expenses = group.expenses.filterNot { it.id == expenseId })),
                activities = appendActivity(
                    data.activities,
                    "Se elimin\u00f3 el gasto \"${expense?.description ?: ""}\" de \"${group.name}\"."
                )
            )
        }
        toast("Gasto eliminado.")
    }

    fun settleDebt(groupId: String, from: String, to: String, amount: Double) {
        repository.mutate { data ->
            val group = data.groups[groupId] ?: return@mutate data
            val settlement = Expense(
                id = newId("exp"),
                description = "Pago de $from a $to",
                amount = BalanceCalculator.round(amount),
                paidBy = from,
                participants = listOf(to),
                category = CATEGORY_SETTLEMENT,
                date = isoNow(),
                isSettlement = true
            )
            data.copy(
                groups = data.groups + (groupId to group.copy(expenses = group.expenses + settlement)),
                activities = appendActivity(
                    data.activities,
                    "$from ha saldado una deuda de ${formatCurrency(amount)} con $to en \"${group.name}\"."
                )
            )
        }
        toast("Deuda saldada. \u00a1Bien hecho! \uD83D\uDC4D")
    }

    // ------------------------------------------------------------- invitations

    fun applyInvitation(invited: Group) {
        val userName = repository.data.value.config.userName
        repository.mutate { data ->
            if (data.groups.containsKey(invited.id)) return@mutate data
            val people = if (userName.isNotEmpty() && !invited.people.contains(userName)) {
                invited.people + userName
            } else invited.people
            val group = invited.copy(people = people)
            data.copy(
                groups = data.groups + (invited.id to group),
                activities = appendActivity(data.activities, "Te uniste al grupo \"${invited.name}\" \uD83C\uDF89.")
            )
        }
        toast("\u00a1Te has unido a \"${invited.name}\"! \uD83C\uDF89")
    }

    // ------------------------------------------------------------------ data

    fun backupJson(): String =
        FileAppDataStore.json().encodeToString(AppData.serializer(), repository.data.value)

    fun restoreFromJson(json: String): Boolean {
        return runCatching {
            val parsed = FileAppDataStore.json().decodeFromString(AppData.serializer(), json)
            if (parsed.groups.isEmpty() && parsed.config.userName.isEmpty()) return false
            repository.replace(parsed)
            true
        }.getOrDefault(false)
    }

    fun deleteAllData() {
        repository.clear()
        toast("\uD83D\uDDD1\uFE0F Todos los datos eliminados.")
    }

    fun exportGroupAsText(group: Group): String {
        val builder = StringBuilder()
        builder.append("Resumen del Grupo: ${group.name}\n")
        builder.append("=========================\n\n")
        builder.append("Participantes: ${group.people.joinToString(", ")}\n\n")
        builder.append("--- Gastos ---\n")
        group.expenses.forEach { expense ->
            builder.append("- ${expense.description} (${formatCurrency(expense.amount)}), pagado por ${expense.paidBy}\n")
        }
        builder.append("\n--- Saldos ---\n")
        val balances = BalanceCalculator.calculateBalances(group.expenses, group.people)
        val transactions = BalanceCalculator.simplifyDebts(balances)
        if (transactions.isEmpty()) {
            builder.append("\u00a1Todos est\u00e1n en paz!\n")
        } else {
            transactions.forEach {
                builder.append("${it.from} debe a ${it.to}: ${formatCurrency(it.amount)}\n")
            }
        }
        return builder.toString()
    }

    fun exportGroupAsCsv(group: Group): String {
        val builder = StringBuilder()
        builder.append("Descripcion,Importe,Moneda,Pagado Por,Participantes,Categoria,Fecha\n")
        group.expenses.forEach { expense ->
            val row = listOf(
                quoteCsv(expense.description),
                expense.amount.toString(),
                "EUR",
                quoteCsv(expense.paidBy),
                quoteCsv(expense.participants.joinToString("; ")),
                quoteCsv(categoryLabel(expense.category)),
                expense.date
            ).joinToString(",")
            builder.append(row).append("\n")
        }
        return builder.toString()
    }

    // --------------------------------------------------------------- helpers

    private fun appendActivity(activities: List<ActivityLog>, text: String): List<ActivityLog> {
        val updated = activities + ActivityLog(text = text, date = isoNow())
        return if (updated.size > 100) updated.takeLast(100) else updated
    }

    private fun quoteCsv(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""

    private fun newId(prefix: String): String =
        "$prefix-${System.currentTimeMillis()}-${(100..999).random()}"

    private fun isoNow(): String = Instant.now().toString()

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DiviViewModel(repository) as T
            }
    }
}

val ES_LOCALE: Locale = Locale("es", "ES")
private val currencyFormatter: java.text.NumberFormat =
    java.text.NumberFormat.getCurrencyInstance(ES_LOCALE)

fun formatCurrency(value: Double): String = currencyFormatter.format(value)

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ES_LOCALE)

fun formatDate(isoDate: String): String = runCatching {
    Instant.parse(isoDate).atZone(ZoneId.systemDefault()).format(displayFormatter)
}.getOrDefault(isoDate)
