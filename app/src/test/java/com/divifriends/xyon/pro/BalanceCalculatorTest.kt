package com.divifriends.xyon.pro

import com.divifriends.xyon.pro.data.BalanceCalculator
import com.divifriends.xyon.pro.data.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceCalculatorTest {

    private fun expense(
        id: String,
        amount: Double,
        paidBy: String,
        participants: List<String>,
        settlement: Boolean = false
    ) = Expense(
        id = id,
        description = "test",
        amount = amount,
        paidBy = paidBy,
        participants = participants,
        category = "\uD83D\uDCB8",
        date = "2024-01-01T00:00:00.000Z",
        isSettlement = settlement
    )

    @Test
    fun `splits an expense evenly between participants`() {
        val balances = BalanceCalculator.calculateBalances(
            expenses = listOf(expense("1", 10.0, "A", listOf("A", "B"))),
            people = listOf("A", "B")
        )
        assertEquals(5.0, balances.getValue("A"), 0.001)
        assertEquals(-5.0, balances.getValue("B"), 0.001)
    }

    @Test
    fun `simplifies debts into a single transfer`() {
        val balances = BalanceCalculator.calculateBalances(
            expenses = listOf(expense("1", 10.0, "A", listOf("A", "B"))),
            people = listOf("A", "B")
        )
        val debts = BalanceCalculator.simplifyDebts(balances)
        assertEquals(1, debts.size)
        assertEquals("B", debts.first().from)
        assertEquals("A", debts.first().to)
        assertEquals(5.0, debts.first().amount, 0.001)
    }

    @Test
    fun `settlement increases payer balance and decreases receiver`() {
        val balances = BalanceCalculator.calculateBalances(
            expenses = listOf(
                expense("1", 10.0, "A", listOf("A", "B")),
                expense("2", 5.0, "B", listOf("A"), settlement = true)
            ),
            people = listOf("A", "B")
        )
        assertEquals(0.0, balances.getValue("A"), 0.001)
        assertEquals(0.0, balances.getValue("B"), 0.001)
        assertTrue(BalanceCalculator.simplifyDebts(balances).isEmpty())
    }

    @Test
    fun `three way split produces two transfers`() {
        val balances = BalanceCalculator.calculateBalances(
            expenses = listOf(
                expense("1", 30.0, "A", listOf("A", "B", "C"))
            ),
            people = listOf("A", "B", "C")
        )
        val debts = BalanceCalculator.simplifyDebts(balances)
        assertEquals(2, debts.size)
        val total = debts.sumOf { it.amount }
        assertEquals(20.0, total, 0.001)
    }

    @Test
    fun `total spent ignores settlements`() {
        val total = BalanceCalculator.totalSpent(
            listOf(
                expense("1", 10.0, "A", listOf("A", "B")),
                expense("2", 5.0, "B", listOf("A"), settlement = true)
            )
        )
        assertEquals(10.0, total, 0.001)
    }
}
