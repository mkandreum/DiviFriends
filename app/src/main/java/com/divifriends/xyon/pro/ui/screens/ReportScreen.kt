package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.categoryLabel
import com.divifriends.xyon.pro.ui.ES_LOCALE
import com.divifriends.xyon.pro.ui.components.AnimatedAmount
import com.divifriends.xyon.pro.ui.components.AnimatedMoneyText
import com.divifriends.xyon.pro.ui.components.DiviCardShape
import com.divifriends.xyon.pro.ui.components.DonutChart
import com.divifriends.xyon.pro.ui.components.EmptyState
import com.divifriends.xyon.pro.ui.components.FillBar
import com.divifriends.xyon.pro.ui.components.HeroPill
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.SegmentedControl
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.components.WaveChart
import com.divifriends.xyon.pro.ui.components.bleed
import com.divifriends.xyon.pro.ui.components.chartColors
import com.divifriends.xyon.pro.ui.components.container
import com.divifriends.xyon.pro.ui.components.content
import com.divifriends.xyon.pro.ui.formatCurrency
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.staggeredAppear
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette
import java.time.LocalDate

private val PERIODS = listOf("Hoy", "Semana", "Mes", "Año")
private val PERIOD_DAYS = listOf(1L, 7L, 30L, 365L)

@Composable
fun ReportScreen(appData: AppData) {
    val palette = LocalDiviPalette.current
    val user = appData.config.userName
    var period by rememberSaveable { mutableIntStateOf(2) }
    var selectedCategory by rememberSaveable { mutableIntStateOf(-1) }

    val now = remember { LocalDate.now() }
    val expenses = remember(appData) { appData.groups.values.flatMap { it.expenses }.filter { !it.isSettlement } }
    val days = PERIOD_DAYS[period]
    val start = now.minusDays(days - 1)
    val prevEnd = start.minusDays(1)
    val prevStart = start.minusDays(days)

    val inPeriod = remember(expenses, period) { expenses.filter { it.inRange(start, now) } }
    val spent = inPeriod.sumOf { it.amount }
    val previous = remember(expenses, period) { expenses.filter { it.inRange(prevStart, prevEnd) }.sumOf { it.amount } }
    val change = if (previous > 0) (spent - previous) / previous * 100.0 else null

    val months = remember { lastMonths(6) }
    val monthly = remember(expenses) { monthlyTotals(expenses, months) }

    val byCategory = remember(inPeriod) {
        inPeriod.groupBy { it.category }
            .map { (c, list) -> c to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }
    val byGroup = remember(appData, period) {
        appData.groups.values
            .map { g -> g to g.expenses.filter { !it.isSettlement && it.inRange(start, now) }.sumOf { it.amount } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    }
    val owedToUser = remember(appData) { appData.groups.values.sumOf { it.balanceFor(user).coerceAtLeast(0.0) } }
    val dailyAverage = spent / days

    HeroScaffold(
        compactTitle = "Estadísticas",
        hero = {
            HeroTopBar(title = "Estadísticas", caption = "Todos tus grupos", modifier = Modifier.staggeredAppear(0))
            SegmentedControl(
                options = PERIODS,
                selectedIndex = period,
                onSelect = {
                    period = it
                    selectedCategory = -1
                },
                onHero = true,
                modifier = Modifier.staggeredAppear(1)
            )
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.staggeredAppear(2)) {
                Column(Modifier.weight(1f)) {
                    Text("Gastado", style = MaterialTheme.typography.labelLarge, color = palette.onHeroMuted)
                    AnimatedAmount(value = spent, color = palette.onHero)
                }
                if (change != null) {
                    HeroPill(
                        text = (if (change >= 0) "+" else "−") + String.format(ES_LOCALE, "%.0f", kotlin.math.abs(change)) + " %",
                        icon = if (change >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            WaveChart(
                values = monthly,
                labels = months.map { monthLabel(it) },
                valueLabel = { formatCurrency(it) },
                chartHeight = 130.dp,
                modifier = Modifier
                    .bleed(8.dp)
                    .staggeredAppear(3)
            )
        },
        sheet = {
            if (appData.groups.isEmpty()) {
                EmptyState(
                    emoji = "📊",
                    title = "Aún no hay datos",
                    message = "Añade grupos y gastos para ver tus estadísticas."
                )
                return@HeroScaffold
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.staggeredAppear(0)) {
                MetricTile("Te deben", owedToUser, Tone.Mint, Modifier.weight(1f))
                MetricTile("Media diaria", dailyAverage, Tone.Aqua, Modifier.weight(1f))
            }

            SectionHeader("Por categoría")
            if (byCategory.isEmpty()) {
                EmptyState(emoji = "🌱", title = "Periodo tranquilo", message = "No hay gastos en ${PERIODS[period].lowercase()}.")
            } else {
                val colors = chartColors()
                val sel = selectedCategory.takeIf { it in byCategory.indices }
                DonutChart(
                    data = byCategory,
                    selected = sel ?: -1,
                    onSelect = { selectedCategory = it },
                    centerTitle = sel?.let { categoryLabel(byCategory[it].first) } ?: "${byCategory.size} categorías",
                    centerValue = formatCurrency(sel?.let { byCategory[it].second } ?: spent),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .staggeredAppear(1)
                )
                Spacer(Modifier.height(16.dp))
                byCategory.forEachIndexed { index, (emoji, value) ->
                    val color = colors[index % colors.size]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(pressedScale = 0.98f, feedback = { it.toggle() }) {
                                selectedCategory = if (selectedCategory == index) -1 else index
                            }
                            .clip(DiviCardShape)
                            .background(if (sel == index) palette.raised else palette.sheet)
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .staggeredAppear(index + 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(DiviCardShape)
                                    .background(color)
                            )
                            Text(
                                text = "  $emoji  ${categoryLabel(emoji)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatCurrency(value),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        FillBar(fraction = (value / spent).toFloat(), color = color, delayMillis = 150 + index * 60)
                    }
                }
            }

            if (byGroup.isNotEmpty()) {
                SectionHeader("Grupos más activos")
                val top = byGroup.first().second
                byGroup.take(5).forEachIndexed { index, (group, value) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .staggeredAppear(index)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.accent,
                            modifier = Modifier.padding(end = 14.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Row {
                                Text(
                                    group.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(formatCurrency(value), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(6.dp))
                            FillBar(fraction = (value / top).toFloat(), color = palette.accent, delayMillis = 200 + index * 80)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun MetricTile(title: String, value: Double, tone: Tone, modifier: Modifier) {
    val palette = LocalDiviPalette.current
    val fg = palette.content(tone, MaterialTheme.colorScheme.onSurface)
    Column(
        modifier = modifier
            .clip(DiviCardShape)
            .background(palette.container(tone))
            .padding(18.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = fg.copy(alpha = 0.75f))
        Spacer(Modifier.height(6.dp))
        AnimatedMoneyText(value = value, style = MaterialTheme.typography.headlineSmall, color = fg)
    }
}
