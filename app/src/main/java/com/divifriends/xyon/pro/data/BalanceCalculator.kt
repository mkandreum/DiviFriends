package com.divifriends.xyon.pro.data

import com.divifriends.xyon.pro.data.model.Debt
import com.divifriends.xyon.pro.data.model.Expense
import kotlin.math.abs

/**
 * Pure, side-effect free money math. Ported from the WebView version with the
 * settlement sign convention fixed: the payer's balance increases, the receiver's
 * decreases, so a settlement always pushes both parties toward zero.
 */
object BalanceCalculator {

    private const val EPSILON = 0.01

    fun calculateBalances(expenses: List<Expense>, people: List<String>): Map<String, Double> {
        val balances = LinkedHashMap<String, Double>()
        people.forEach { balances[it] = 0.0 }

        expenses.forEach { expense ->
            if (expense.isSettlement) {
                val receiver = expense.participants.firstOrNull()
                balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
                if (receiver != null) {
                    balances[receiver] = (balances[receiver] ?: 0.0) - expense.amount
                }
            } else {
                balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
                if (expense.participants.isNotEmpty()) {
                    val share = expense.amount / expense.participants.size
                    expense.participants.forEach { person ->
                        if (balances.containsKey(person)) {
                            balances[person] = balances.getValue(person) - share
                        }
                    }
                }
            }
        }
        return balances
    }

    fun simplifyDebts(balances: Map<String, Double>): List<Debt> {
        val debtors = balances.entries
            .filter { it.value < -EPSILON }
            .map { MutableBalance(it.key, it.value) }
            .sortedBy { it.amount }
            .toMutableList()

        val creditors = balances.entries
            .filter { it.value > EPSILON }
            .map { MutableBalance(it.key, it.value) }
            .sortedByDescending { it.amount }
            .toMutableList()

        val transactions = mutableListOf<Debt>()
        var guard = 0
        while (debtors.isNotEmpty() && creditors.isNotEmpty() && guard++ < 10_000) {
            val debtor = debtors.first()
            val creditor = creditors.first()
            val amount = minOf(-debtor.amount, creditor.amount)
            if (amount <= EPSILON) break

            transactions += Debt(from = debtor.name, to = creditor.name, amount = round(amount))

            debtor.amount += amount
            creditor.amount -= amount

            if (abs(debtor.amount) < EPSILON) debtors.removeAt(0)
            if (abs(creditor.amount) < EPSILON) creditors.removeAt(0)
        }
        return transactions
    }

    fun totalSpent(expenses: List<Expense>): Double =
        expenses.filter { !it.isSettlement }.sumOf { it.amount }

    /** Rounds to cents to avoid floating point dust in the UI. */
    fun round(value: Double): Double = kotlin.math.round(value * 100.0) / 100.0

    private data class MutableBalance(val name: String, var amount: Double)
}
