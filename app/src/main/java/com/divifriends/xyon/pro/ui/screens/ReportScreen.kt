package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.Expense
import com.divifriends.xyon.pro.data.model.categoryLabel
import com.divifriends.xyon.pro.ui.components.DiviDropdown
import com.divifriends.xyon.pro.ui.components.DiviHeader
import com.divifriends.xyon.pro.ui.components.DiviScreen
import com.divifriends.xyon.pro.ui.components.DoughnutChart
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.GlassCard
import com.divifriends.xyon.pro.ui.components.LineChart
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.formatDate
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import com.divifriends.xyon.pro.ui.theme.TextSecondary
import java.time.Instant

@Composable
fun ReportScreen(appData: AppData) {
    val groups = appData.groups.values.toList()

    DiviScreen {
        DiviHeader(subtitle = "Informe de Grupo y Actividad \uD83D\uDCC8")

        if (groups.isEmpty()) {
            EmptyState("\uD83D\uDCC8", "Crea un grupo para ver sus estad\u00edsticas y actividad.")
            return@DiviScreen
        }

        var selectedId by remember(groups) { mutableStateOf(groups.first().id) }
        val group = groups.firstOrNull { it.id == selectedId } ?: groups.first()

        GlassCard {
            SectionHeader("Estad\u00edsticas de Grupo")
            DiviDropdown(
                options = groups,
                selected = group,
                optionLabel = { it.name },
                onSelect = { selectedId = it.id },
                label = "Selecciona un Grupo",
                modifier = Modifier.padding(top = 16.dp)
            )

            val expensesToChart = remember(group.expenses) { group.expenses.filter { !it.isSettlement } }
            if (expensesToChart.isEmpty()) {
                Text(
                    text = "A\u00fan no hay gastos que mostrar en este grupo.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                val categoryData = remember(expensesToChart) {
                    expensesToChart
                        .groupBy { it.category }
                        .map { (category, list) ->
                            "${category} ${categoryLabel(category)}" to list.sumOf { it.amount }
                        }
                }
                val timeline = remember(expensesToChart) {
                    expensesToChart.map { expense ->
                        parseMillis(expense) to expense.amount
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    DoughnutChart(data = categoryData)
                    LineChart(points = timeline)
                }
            }
        }

        GlassCard {
            SectionHeader("Actividad Reciente")
            val activities = remember(appData.activities) {
                appData.activities.sortedByDescending { it.date }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (activities.isEmpty()) {
                    Text(
                        text = "\uD83E\uDDD0 No hay actividad reciente.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    activities.forEach { activity ->
                        ActivityRow(text = activity.text, date = activity.date)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(text: String, date: String) {
    val palette = LocalDiviPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = palette.border,
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                )
            }
            .padding(start = 14.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "\uD83D\uDDD3\uFE0F ${formatDate(date)}",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun parseMillis(expense: Expense): Long =
    runCatching { Instant.parse(expense.date).toEpochMilli() }.getOrDefault(0L)
