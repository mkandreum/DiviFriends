package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.BalanceCalculator
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.ui.components.DiviHeader
import com.divifriends.xyon.pro.ui.components.DiviScreen
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.GlassCard
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.DiviInnerShape
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextSecondary
import com.divifriends.xyon.pro.ui.components.glass

@Composable
fun BalancesScreen(appData: AppData) {
    val palette = LocalDiviPalette.current

    val allExpenses = remember(appData) { appData.groups.values.flatMap { it.expenses } }
    val allPeople = remember(appData) { appData.groups.values.flatMap { it.people }.distinct() }

    DiviScreen {
        DiviHeader(subtitle = "Saldos Globales \u2696\uFE0F")

        if (allPeople.isEmpty()) {
            EmptyState(
                "\u2696\uFE0F",
                "No hay datos para mostrar saldos. \u00a1Crea un grupo y a\u00f1ade gastos!"
            )
            return@DiviScreen
        }

        val balances = remember(allExpenses, allPeople) {
            BalanceCalculator.calculateBalances(allExpenses, allPeople)
        }
        val transactions = remember(balances) { BalanceCalculator.simplifyDebts(balances) }

        if (transactions.isEmpty()) {
            EmptyState("\uD83C\uDF89", "\u00a1Todo en orden! No hay deudas pendientes entre grupos.")
            return@DiviScreen
        }

        val userName = appData.config.userName
        val userBalance = balances[userName] ?: 0.0
        val balanceText = if (userBalance >= 0) {
            "Tienes un saldo global a favor de ${formatCurrency(userBalance)} \uD83E\uDD11"
        } else {
            "Debes un total de ${formatCurrency(-userBalance)} \uD83D\uDE25"
        }

        GlassCard {
            SectionHeader("Tu Saldo General \uD83C\uDF0D")
            Text(
                text = balanceText,
                color = if (userBalance >= 0) palette.secondary else palette.danger,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        GlassCard {
            SectionHeader("Pagos para Saldar Todo \uD83E\uDD1D")
            Text(
                text = "Estos son los pagos sugeridos para que todos queden a cero. Para saldar una deuda concreta, ve al grupo correspondiente.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                transactions.forEach { debt ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(shape = DiviInnerShape, elevation = 2.dp)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "${debt.from} \u2192 ${debt.to}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatCurrency(debt.amount),
                            color = palette.secondary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
