@file:OptIn(ExperimentalMaterial3Api::class)

package com.paku.keuangan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paku.keuangan.data.Category
import com.paku.keuangan.data.Transaction
import com.paku.keuangan.data.TxType
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.ui.theme.expense
import com.paku.keuangan.ui.theme.income
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun TransactionsScreen(
    state: PakuUiState,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (TxType?) -> Unit,
    onClearFocus: () -> Unit,
    onResetFilters: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val filters = state.filters
    val focusCategory: Category? = remember(filters.focusCategoryId) {
        filters.focusCategoryId?.let { id ->
            state.transactions.firstOrNull { it.categoryId == id }?.category
                ?: com.paku.keuangan.data.Categories.all.firstOrNull { it.id == id }
        }
    }

    val filtered = remember(state.transactions, filters) {
        val query = filters.search.trim()
        state.transactions.filter { transaction ->
            val category = transaction.category
            val matchesQuery = query.isBlank() ||
                transaction.note.contains(query, ignoreCase = true) ||
                category.name.contains(query, ignoreCase = true)
            val matchesType = filters.typeFilter == null || transaction.type == filters.typeFilter
            val matchesCategory = filters.focusCategoryId == null ||
                transaction.categoryId == filters.focusCategoryId
            val matchesMonth = filters.focusMonth == null ||
                YearMonth.from(transaction.date) == filters.focusMonth
            matchesQuery && matchesType && matchesCategory && matchesMonth
        }
    }

    val grouped = remember(filtered) {
        filtered.groupBy { it.date }.toSortedMap(compareByDescending { it })
    }
    val hasActiveFilters = filters.search.isNotBlank() || filters.typeFilter != null ||
        filters.focusCategoryId != null

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.transactionsTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = s.transactionsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconBubble(
                    icon = PakuIcons.Receipt,
                    tint = MaterialTheme.colorScheme.secondary,
                    size = 48
                )
            }
        }

        item {
            SearchField(
                value = filters.search,
                onValueChange = onSearchChange
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftChip(
                    label = s.all,
                    selected = filters.typeFilter == null && filters.focusCategoryId == null,
                    onClick = {
                        onClearFocus()
                        onTypeFilterChange(null)
                    }
                )
                SoftChip(
                    label = s.income,
                    selected = filters.typeFilter == TxType.INCOME && filters.focusCategoryId == null,
                    onClick = {
                        onClearFocus()
                        onTypeFilterChange(TxType.INCOME)
                    },
                    accent = MaterialTheme.colorScheme.income
                )
                SoftChip(
                    label = s.expense,
                    selected = filters.typeFilter == TxType.EXPENSE && filters.focusCategoryId == null,
                    onClick = {
                        onClearFocus()
                        onTypeFilterChange(TxType.EXPENSE)
                    },
                    accent = MaterialTheme.colorScheme.expense
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = s.recordsCount(filtered.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filters.focusCategoryId != null) {
            item {
                FocusBanner(
                    category = focusCategory,
                    month = filters.focusMonth,
                    onClear = {
                        onClearFocus()
                        onTypeFilterChange(null)
                    }
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                if (state.transactions.isEmpty()) {
                    PakuCard {
                        EmptyState(
                            emoji = "📝",
                            title = s.noRecordsTitle,
                            message = s.noRecordsBody,
                            actionLabel = s.addTransaction,
                            onAction = onAddTransaction
                        )
                    }
                } else {
                    PakuCard {
                        EmptyState(
                            emoji = "🔍",
                            title = s.noResultsTitle,
                            message = s.noResultsBody,
                            actionLabel = if (hasActiveFilters) s.resetFilter else null,
                            onAction = if (hasActiveFilters) onResetFilters else null
                        )
                    }
                }
            }
        } else {
            grouped.forEach { (date, transactions) ->
                item(key = "day-${date}") {
                    DayGroupCard(
                        date = date,
                        transactions = transactions,
                        onEditTransaction = onEditTransaction,
                        onDeleteTransaction = onDeleteTransaction
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PakuIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = s.searchHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = PakuIcons.Close,
                        contentDescription = s.clearSearch,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun FocusBanner(
    category: Category?,
    month: YearMonth?,
    onClear: () -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category?.emoji ?: "🏷️",
                fontSize = 18.sp
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = buildString {
                    append(category?.let { s.categoryName(it.id) } ?: s.category)
                    if (month != null) append(" · ${s.monthLabel(month)}")
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClear, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = PakuIcons.Close,
                    contentDescription = s.clearCategoryFilter,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DayGroupCard(
    date: LocalDate,
    transactions: List<Transaction>,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    val s = LocalStrings.current
    val dayNet = remember(transactions) { transactions.sumOf { it.signedAmount } }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.dayHeaderLabel(date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (dayNet > 0) s.rupiahSigned(dayNet)
                else if (dayNet < 0) "−${s.rupiah(-dayNet)}"
                else s.rupiah(0),
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    dayNet > 0 -> MaterialTheme.colorScheme.income
                    dayNet < 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Spacer(Modifier.height(7.dp))
        PakuCard(padding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
            transactions.forEachIndexed { index, transaction ->
                SwipeableTransaction(
                    transaction = transaction,
                    onClick = { onEditTransaction(transaction) },
                    onDelete = { onDeleteTransaction(transaction) }
                )
                if (index != transactions.lastIndex) {
                    SectionDivider(
                        modifier = Modifier.padding(start = 60.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableTransaction(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val s = LocalStrings.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PakuIcons.Delete,
                        contentDescription = s.delete,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = s.delete,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        TransactionRow(
            transaction = transaction,
            onClick = onClick,
            showDate = false,
            showChevron = false
        )
    }
}
