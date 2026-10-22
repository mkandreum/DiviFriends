package com.divifriends.xyon.pro.ui.screens

import com.divifriends.xyon.pro.data.BalanceCalculator
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.Expense
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.ui.ES_LOCALE
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/** Derived numbers shared by several screens. Pure functions, no Compose. */

fun Expense.localDate(): LocalDate? =
    runCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()

fun Expense.inRange(start: LocalDate, end: LocalDate): Boolean {
    val day = localDate() ?: return false
    return !day.isBefore(start) && !day.isAfter(end)
}

private val shortDate = DateTimeFormatter.ofPattern("d MMM", ES_LOCALE)
private val longDate = DateTimeFormatter.ofPattern("d MMM yyyy", ES_LOCALE)

fun Expense.shortDateLabel(): String = localDate()?.format(shortDate).orEmpty()

/** "Hoy", "Ayer" or "12 oct 2026". */
fun dayHeader(day: LocalDate?): String {
    if (day == null) return "Sin fecha"
    val today = LocalDate.now()
    return when (day) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> day.format(longDate)
    }
}

fun Group.balances(): Map<String, Double> = BalanceCalculator.calculateBalances(expenses, people)

fun Group.debts() = BalanceCalculator.simplifyDebts(balances())

fun Group.totalSpent(): Double = BalanceCalculator.totalSpent(expenses)

/** The user's net position in this group (positive: they are owed). */
fun Group.balanceFor(user: String): Double = if (user.isBlank()) 0.0 else balances()[user] ?: 0.0

/** Share of money already paid back, 0..1. A group with nothing pending is fully settled. */
fun Group.settledRatio(): Float {
    val settled = expenses.filter { it.isSettlement }.sumOf { it.amount }
    val pending = debts().sumOf { it.amount }
    return when {
        settled + pending <= 0.0 -> if (expenses.isEmpty()) 0f else 1f
        else -> (settled / (settled + pending)).toFloat()
    }
}

/** Most used category emoji in the group. */
fun Group.signatureEmoji(): String =
    expenses.filter { !it.isSettlement }
        .groupingBy { it.category }
        .eachCount()
        .maxByOrNull { it.value }?.key ?: "👥"

/** How an expense moves the user's balance: + they lent, − they owe. */
fun Expense.effectFor(user: String): Double {
    if (user.isBlank() || participants.isEmpty()) return 0.0
    val share = amount / participants.size
    var effect = 0.0
    if (paidBy == user) effect += amount
    if (user in participants) effect -= share
    return effect
}

data class ExpenseEntry(val expense: Expense, val group: Group)

fun AppData.recentExpenses(limit: Int): List<ExpenseEntry> =
    groups.values
        .flatMap { g -> g.expenses.map { ExpenseEntry(it, g) } }
        .sortedByDescending { it.expense.date }
        .take(limit)

fun AppData.allPeople(): List<String> =
    groups.values.flatMap { it.people }
        .filter { it != config.userName }
        .distinct()

/** Month starts for the last [count] months, oldest first. */
fun lastMonths(count: Int, now: LocalDate = LocalDate.now()): List<LocalDate> =
    (count - 1 downTo 0).map { now.minusMonths(it.toLong()).withDayOfMonth(1) }

fun monthLabel(start: LocalDate): String =
    start.month.getDisplayName(TextStyle.SHORT, ES_LOCALE).trimEnd('.').replaceFirstChar { it.uppercase() }

fun monthlyTotals(expenses: List<Expense>, months: List<LocalDate>): List<Double> =
    months.map { start ->
        val end = start.plusMonths(1).minusDays(1)
        expenses.filter { !it.isSettlement && it.inRange(start, end) }.sumOf { it.amount }
    }

/** "+2,5 %" style change between the last two values, or null when meaningless. */
fun percentChange(values: List<Double>): String? {
    if (values.size < 2) return null
    val prev = values[values.size - 2]
    val cur = values.last()
    if (prev <= 0.0) return null
    val pct = (cur - prev) / prev * 100.0
    val sign = if (pct >= 0) "+" else "−"
    return sign + String.format(ES_LOCALE, "%.1f", kotlin.math.abs(pct)) + " %"
}
