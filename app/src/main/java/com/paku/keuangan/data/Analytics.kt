package com.paku.keuangan.data

import java.time.YearMonth

// ---------------------------------------------------------------------------
// Perhitungan arus kas — dipakai bersama oleh Beranda & Statistik.
// ---------------------------------------------------------------------------

data class CategoryTotal(
    val category: Category,
    val amount: Long,
    val count: Int
)

data class MonthFlow(
    val month: YearMonth,
    val income: Long,
    val expense: Long
) {
    val net: Long get() = income - expense
}

fun List<Transaction>.forMonth(month: YearMonth): List<Transaction> =
    filter { YearMonth.from(it.date) == month }

fun List<Transaction>.totalOf(type: TxType): Long =
    filter { it.type == type }.sumOf { it.amount }

fun List<Transaction>.balance(): Long = sumOf { it.signedAmount }

fun List<Transaction>.categoryTotals(type: TxType): List<CategoryTotal> =
    filter { it.type == type }
        .groupBy { it.categoryId }
        .map { (categoryId, list) ->
            CategoryTotal(
                category = Categories.byId(categoryId),
                amount = list.sumOf { it.amount },
                count = list.size
            )
        }
        .sortedByDescending { it.amount }

fun List<Transaction>.monthlyFlows(endMonth: YearMonth, count: Int = 6): List<MonthFlow> =
    (count - 1 downTo 0).map { back ->
        val month = endMonth.minusMonths(back.toLong())
        val items = forMonth(month)
        MonthFlow(
            month = month,
            income = items.totalOf(TxType.INCOME),
            expense = items.totalOf(TxType.EXPENSE)
        )
    }

/** Selisih persentase pengeluaran dibanding bulan sebelumnya. */
fun List<Transaction>.expenseChangePercent(month: YearMonth): Float? {
    val current = forMonth(month).totalOf(TxType.EXPENSE)
    val previous = forMonth(month.minusMonths(1)).totalOf(TxType.EXPENSE)
    if (previous <= 0L) return null
    return (current - previous).toFloat() / previous.toFloat()
}

fun List<Transaction>.daysWithActivity(month: YearMonth): Int =
    forMonth(month).map { it.date }.distinct().size

fun List<Transaction>.averageDailyExpense(month: YearMonth): Long {
    val days = daysWithActivity(month).coerceAtLeast(1)
    return forMonth(month).totalOf(TxType.EXPENSE) / days
}
