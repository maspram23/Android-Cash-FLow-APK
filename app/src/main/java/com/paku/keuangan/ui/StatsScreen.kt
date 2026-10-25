package com.paku.keuangan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paku.keuangan.data.Category
import com.paku.keuangan.data.TxType
import com.paku.keuangan.data.averageDailyExpense
import com.paku.keuangan.data.categoryTotals
import com.paku.keuangan.data.daysWithActivity
import com.paku.keuangan.data.forMonth
import com.paku.keuangan.data.monthlyFlows
import com.paku.keuangan.data.totalOf
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.ui.theme.expense
import com.paku.keuangan.ui.theme.income
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun StatsScreen(
    state: PakuUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onThisMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPieTypeChange: (TxType) -> Unit,
    onBarMetricChange: (BarMetric) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onOpenTransactions: () -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val month = state.selectedMonth
    val type = state.filters.pieType
    val monthTransactions = remember(state.transactions, month) {
        state.transactions.forMonth(month)
    }
    val totals = remember(monthTransactions, type) { monthTransactions.categoryTotals(type) }
    val totalAmount = remember(totals) { totals.sumOf { it.amount } }
    val slices = remember(totals, s) { buildChartSlices(totals, limit = 5, strings = s) }
    val flows = remember(state.transactions, month) { state.transactions.monthlyFlows(month, 6) }
    val previousTotal = remember(state.transactions, month, type) {
        state.transactions.forMonth(month.minusMonths(1)).totalOf(type)
    }
    val changePercent = if (previousTotal > 0L) {
        (totalAmount - previousTotal).toFloat() / previousTotal.toFloat()
    } else {
        null
    }
    val largest = totals.firstOrNull()
    val largestPercent = largest?.let { it.amount.toFloat() / totalAmount.coerceAtLeast(1L) }
    val dailyAverage = remember(monthTransactions) { monthTransactions.averageDailyExpense(month) }
    val activeDays = remember(monthTransactions) { monthTransactions.daysWithActivity(month) }
    val typeColor = if (type == TxType.INCOME) MaterialTheme.colorScheme.income
    else MaterialTheme.colorScheme.expense

    var selectedSlice by remember(month, type) { mutableStateOf<String?>(null) }
    val selected = slices.firstOrNull { it.key == selectedSlice }
    val selectedCategoryTotal = totals.firstOrNull { it.category.id == selectedSlice }
    val selectedCount = when {
        selected == null -> 0
        selected.key == "other_combined" -> totals.drop(5).sumOf { it.count }
        else -> selectedCategoryTotal?.count ?: 0
    }
    val selectedLabel = when {
        selected == null -> s.totalOf(type)
        selectedCategoryTotal != null -> s.categoryName(selectedCategoryTotal.category.id)
        else -> selected.label
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.statsTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = s.statsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconBubble(
                    icon = PakuIcons.Chart,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 48
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.period,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = s.periodHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MonthStepper(
                    month = month,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth
                )
            }
            if (month != YearMonth.now()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onThisMonth) {
                        Icon(
                            imageVector = PakuIcons.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(s.thisMonth)
                    }
                }
            }
        }

        item {
            PakuCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.composition(type),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = s.monthTransactionSubtitle(
                                s.monthLabel(month),
                                monthTransactions.count { it.type == type }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TypeBadge(type = type)
                }
                Spacer(Modifier.height(12.dp))
                SegmentedToggle(
                    options = listOf(TxType.EXPENSE, TxType.INCOME),
                    selected = type,
                    label = { s.typeLabel(it) },
                    onSelect = { onPieTypeChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    activeColor = typeColor
                )
                Spacer(Modifier.height(12.dp))

                if (slices.isEmpty()) {
                    EmptyState(
                        emoji = if (type == TxType.INCOME) "💰" else "🧾",
                        title = s.noDataType(type),
                        message = s.noDataTypeBody,
                        actionLabel = s.addTransaction,
                        onAction = onAddTransaction
                    )
                } else {
                    DonutChart(
                        slices = slices,
                        selectedKey = selectedSlice,
                        onSelect = { selectedSlice = it },
                        centerLabel = selectedLabel,
                        centerValue = s.rupiah(selected?.value ?: totalAmount),
                        centerCaption = if (selected != null) {
                            "${s.percent(selected.value.toFloat() / totalAmount.coerceAtLeast(1L))} · ${s.transactionCount(selectedCount)}"
                        } else {
                            s.monthLabel(month)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(242.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    DonutLegend(
                        slices = slices,
                        selectedKey = selectedSlice,
                        onSelect = { selectedSlice = it }
                    )
                }
            }
        }

        item {
            InsightCard(
                type = type,
                largestCategoryId = largest?.category?.id,
                largestPercent = largestPercent,
                changePercent = changePercent,
                dailyAverage = dailyAverage,
                activeDays = activeDays,
                hasData = totals.isNotEmpty()
            )
        }

        item {
            PakuCard {
                Text(
                    text = s.sixMonthTrend,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.tapBarToSwitchMonth,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                SegmentedToggle(
                    options = BarMetric.entries.toList(),
                    selected = state.filters.barMetric,
                    label = {
                        when (it) {
                            BarMetric.BOTH -> s.both
                            BarMetric.INCOME -> s.incomeShort
                            BarMetric.EXPENSE -> s.expenseShort
                        }
                    },
                    onSelect = onBarMetricChange,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                if (flows.all { it.income == 0L && it.expense == 0L }) {
                    EmptyState(
                        emoji = "📊",
                        title = s.trendEmptyTitle,
                        message = s.trendEmptyBody
                    )
                } else {
                    CashFlowBarChart(
                        flows = flows,
                        metric = state.filters.barMetric,
                        selectedMonth = month,
                        onSelectMonth = onSelectMonth
                    )
                }
            }
        }

        item {
            PakuCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.categoryBreakdown,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = s.tapCategoryHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = s.rupiah(totalAmount),
                        style = MaterialTheme.typography.labelLarge,
                        color = typeColor
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (totals.isEmpty()) {
                    Text(
                        text = s.noCategoryData,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    totals.forEachIndexed { index, item ->
                        CategoryDetailRow(
                            total = item,
                            maxAmount = totals.first().amount,
                            totalAmount = totalAmount,
                            onClick = { onCategoryClick(item.category) }
                        )
                        if (index != totals.lastIndex) {
                            SectionDivider(
                                modifier = Modifier.padding(
                                    start = 54.dp,
                                    top = 4.dp,
                                    bottom = 4.dp
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    onClick = onOpenTransactions,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.seeAllTransactions,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = PakuIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💡", modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = s.statsTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    type: TxType,
    largestCategoryId: String?,
    largestPercent: Float?,
    changePercent: Float?,
    dailyAverage: Long,
    activeDays: Int,
    hasData: Boolean
) {
    val s = LocalStrings.current
    val largestName = largestCategoryId?.let { s.categoryName(it) } ?: "-"
    val accent = if (type == TxType.INCOME) MaterialTheme.colorScheme.income
    else MaterialTheme.colorScheme.expense
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Row(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text("💡", modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.insightTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                if (!hasData) {
                    Text(
                        text = s.insightNoData(type),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = if (type == TxType.EXPENSE) {
                            s.insightTopExpense(
                                largestName,
                                s.percent(largestPercent ?: 0f)
                            )
                        } else {
                            s.insightTopIncome(
                                largestName,
                                s.percent(largestPercent ?: 0f)
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(5.dp))
                    val changeText = when {
                        changePercent == null -> s.insightNoComparison
                        changePercent >= 0 -> s.insightUp(s.percent(abs(changePercent)))
                        else -> s.insightDown(s.percent(abs(changePercent)))
                    }
                    Text(
                        text = if (type == TxType.EXPENSE) {
                            s.insightDaily(
                                changeText,
                                s.compactRupiah(dailyAverage),
                                activeDays
                            )
                        } else {
                            changeText
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDetailRow(
    total: com.paku.keuangan.data.CategoryTotal,
    maxAmount: Long,
    totalAmount: Long,
    onClick: () -> Unit
) {
    val category = total.category
    val s = LocalStrings.current
    val color = Color(category.color)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryBubble(category = category, size = 40)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = s.categoryName(category.id),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = s.rupiah(total.amount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(6.dp))
                ProgressBar(
                    progress = total.amount.toFloat() / maxAmount.coerceAtLeast(1L),
                    color = color,
                    height = 7
                )
                Spacer(Modifier.height(5.dp))
                Row {
                    Text(
                        text = s.transactionCount(total.count),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = s.percent(total.amount.toFloat() / totalAmount.coerceAtLeast(1L), 1),
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = PakuIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
