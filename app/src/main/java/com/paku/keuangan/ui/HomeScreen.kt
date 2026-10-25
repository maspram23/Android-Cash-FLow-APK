package com.paku.keuangan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paku.keuangan.data.CategoryTotal
import com.paku.keuangan.data.Transaction
import com.paku.keuangan.data.TxType
import com.paku.keuangan.data.balance
import com.paku.keuangan.data.categoryTotals
import com.paku.keuangan.data.forMonth
import com.paku.keuangan.data.monthlyFlows
import com.paku.keuangan.data.totalOf
import com.paku.keuangan.i18n.IdStrings
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.i18n.Strings
import com.paku.keuangan.ui.theme.expense
import com.paku.keuangan.ui.theme.income
import java.time.YearMonth

@Composable
fun HomeScreen(
    state: PakuUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onThisMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenAllTransactions: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val month = state.selectedMonth
    val monthTransactions = remember(state.transactions, month) {
        state.transactions.forMonth(month)
    }
    val income = remember(monthTransactions) { monthTransactions.totalOf(TxType.INCOME) }
    val expense = remember(monthTransactions) { monthTransactions.totalOf(TxType.EXPENSE) }
    val balance = remember(state.transactions) { state.transactions.balance() }
    val expenseTotals = remember(monthTransactions) { monthTransactions.categoryTotals(TxType.EXPENSE) }
    val slices = remember(expenseTotals, s) {
        buildChartSlices(expenseTotals, limit = 5, strings = s)
    }
    val flows = remember(state.transactions, month) { state.transactions.monthlyFlows(month, 6) }
    val recent = state.transactions.take(5)
    val isThisMonth = month == YearMonth.now()

    var selectedSlice by remember(month) { mutableStateOf<String?>(null) }
    val selected = slices.firstOrNull { it.key == selectedSlice }
    val selectedTotal = expenseTotals.firstOrNull { it.category.id == selectedSlice }
    val selectedCount = when {
        selected == null -> 0
        selected.key == "other_combined" -> expenseTotals.drop(5).sumOf { it.count }
        else -> selectedTotal?.count ?: 0
    }
    val percent = selected?.let { it.value.toFloat() / expense.coerceAtLeast(1L) }

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
                LogoMark(size = 48)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${s.greeting()},",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.settings.userName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PakuIcons.Settings,
                            contentDescription = s.settingsTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            BalanceHero(
                balance = balance,
                monthIncome = income,
                monthExpense = expense,
                month = month
            )
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.monthSummaryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isThisMonth) s.monthSummaryCurrent else s.monthSummarySelected,
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
                AnimatedVisibility(visible = !isThisMonth) {
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
                            Text(s.backToThisMonth)
                        }
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
                            text = s.expenseByCategory,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = s.monthTransactionSubtitle(
                                s.monthLabel(month),
                                monthTransactions.count { it.type == TxType.EXPENSE }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onOpenStatistics)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.detail,
                            style = MaterialTheme.typography.labelMedium,
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

                if (slices.isEmpty()) {
                    EmptyState(
                        emoji = "🧾",
                        title = s.noExpenseTitle,
                        message = s.noExpenseBody,
                        actionLabel = s.recordExpense,
                        onAction = onAddTransaction
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                    DonutChart(
                        slices = slices,
                        selectedKey = selectedSlice,
                        onSelect = { selectedSlice = it },
                        centerLabel = selected?.label ?: s.totalExpense,
                        centerValue = s.rupiah(selected?.value ?: expense),
                        centerCaption = if (selected != null) {
                            "${s.percent(percent ?: 0f)} · ${s.transactionCount(selectedCount)}"
                        } else {
                            s.monthLabel(month)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(244.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    DonutLegend(
                        slices = slices,
                        selectedKey = selectedSlice,
                        onSelect = { selectedSlice = it }
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = s.incomeThisMonth(s.rupiah(income)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.income
                        )
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
                            text = s.sixMonthCashflow,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = s.tapBarToSeeMonth,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (flows.all { it.income == 0L && it.expense == 0L }) {
                    EmptyState(
                        emoji = "📊",
                        title = s.homeChartEmptyTitle,
                        message = s.homeChartEmptyBody
                    )
                } else {
                    CashFlowBarChart(
                        flows = flows,
                        metric = BarMetric.BOTH,
                        selectedMonth = month,
                        onSelectMonth = onSelectMonth
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ChartLegendDot(
                            color = MaterialTheme.colorScheme.income,
                            label = s.income
                        )
                        ChartLegendDot(
                            color = MaterialTheme.colorScheme.expense,
                            label = s.expense
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = s.recentTransactions,
                subtitle = if (recent.isEmpty()) null else s.recordsSaved(state.transactions.size),
                actionLabel = if (recent.isEmpty()) null else s.seeAll,
                onAction = onOpenAllTransactions
            )
        }

        item {
            if (recent.isEmpty()) {
                PakuCard {
                    EmptyState(
                        emoji = "🌱",
                        title = s.homeEmptyTitle,
                        message = s.homeEmptyBody,
                        actionLabel = s.recordNow,
                        onAction = onAddTransaction
                    )
                }
            } else {
                PakuCard(padding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    recent.forEachIndexed { index, transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onClick = { onEditTransaction(transaction) },
                            showDate = true,
                            showChevron = true
                        )
                        if (index != recent.lastIndex) {
                            SectionDivider(
                                modifier = Modifier.padding(start = 62.dp, end = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Ambil maksimal [limit] kategori teratas, sisanya digabung menjadi "Lainnya". */
fun buildChartSlices(
    totals: List<CategoryTotal>,
    limit: Int = 5,
    strings: Strings = IdStrings
): List<ChartSlice> {
    if (totals.isEmpty()) return emptyList()
    val top = totals.take(limit)
    val rest = totals.drop(limit)
    val result = top.map {
        ChartSlice(
            key = it.category.id,
            label = strings.categoryName(it.category.id),
            value = it.amount,
            color = androidx.compose.ui.graphics.Color(it.category.color),
            emoji = it.category.emoji
        )
    }.toMutableList()
    if (rest.isNotEmpty()) {
        result += ChartSlice(
            key = "other_combined",
            label = strings.categoryName("other_expense"),
            value = rest.sumOf { it.amount },
            color = androidx.compose.ui.graphics.Color(0xFF9C908A)
        )
    }
    return result
}
