package com.paku.keuangan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class AnalyticsTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 25)
    private val month: YearMonth = YearMonth.of(2026, 10)

    private fun tx(
        type: TxType,
        amount: Long,
        categoryId: String,
        date: LocalDate
    ) = Transaction(type = type, amount = amount, categoryId = categoryId, date = date)

    private val sample = listOf(
        tx(TxType.INCOME, 8_000_000, "salary", LocalDate.of(2026, 10, 1)),
        tx(TxType.INCOME, 500_000, "business", LocalDate.of(2026, 10, 12)),
        tx(TxType.EXPENSE, 150_000, "food", LocalDate.of(2026, 10, 2)),
        tx(TxType.EXPENSE, 100_000, "food", LocalDate.of(2026, 10, 3)),
        tx(TxType.EXPENSE, 300_000, "transport", LocalDate.of(2026, 10, 5)),
        tx(TxType.EXPENSE, 400_000, "shopping", LocalDate.of(2026, 9, 20))
    )

    @Test
    fun `forMonth hanya mengambil transaksi bulan terpilih`() {
        val result = sample.forMonth(month)
        assertEquals(5, result.size)
        assertTrue(result.all { YearMonth.from(it.date) == month })
    }

    @Test
    fun `totalOf memisahkan pemasukan dan pengeluaran`() {
        val october = sample.forMonth(month)
        assertEquals(8_500_000L, october.totalOf(TxType.INCOME))
        assertEquals(550_000L, october.totalOf(TxType.EXPENSE))
    }

    @Test
    fun `categoryTotals mengurutkan kategori terbesar lebih dulu`() {
        val totals = sample.forMonth(month).categoryTotals(TxType.EXPENSE)
        assertEquals(listOf("transport", "food"), totals.map { it.category.id })
        val food = totals.first { it.category.id == "food" }
        assertEquals(250_000L, food.amount)
        assertEquals(2, food.count)
    }

    @Test
    fun `monthlyFlows menghitung enam bulan terakhir`() {
        val flows = sample.monthlyFlows(month, count = 6)
        assertEquals(6, flows.size)
        assertEquals(month, flows.last().month)
        assertEquals(8_500_000L, flows.last().income)
        assertEquals(550_000L, flows.last().expense)
        assertEquals(7_950_000L, flows.last().net)
    }

    @Test
    fun `expenseChangePercent membandingkan dengan bulan sebelumnya`() {
        val change = sample.expenseChangePercent(month)
        assertNotNull(change)
        // 550.000 dibanding 400.000 -> naik 37,5%
        assertEquals(0.375f, change!!, 0.001f)
    }

    @Test
    fun `balance mengurangi pengeluaran dari pemasukan`() {
        assertEquals(7_550_000L, sample.balance())
    }

    @Test
    fun `data contoh terurut dan tidak melewati hari ini`() {
        val demo = DemoData.build(today)
        assertTrue(demo.size > 40)
        assertTrue(demo.all { !it.date.isAfter(today) })
        assertTrue(demo.zipWithNext().all { (a, b) -> !a.date.isBefore(b.date) })
        assertTrue(demo.any { it.type == TxType.INCOME })
        assertTrue(demo.any { it.type == TxType.EXPENSE })
    }

    @Test
    fun `setiap kategori pada data contoh dikenali`() {
        val demo = DemoData.build(today)
        val ids = Categories.all.map { it.id }.toSet()
        assertTrue(demo.all { it.categoryId in ids })
    }

    @Test
    fun `data contoh punya pemasukan dan pengeluaran wajar setiap bulan`() {
        val demo = DemoData.build(today)
        val flows = demo.monthlyFlows(month, count = 6)
        assertEquals(6, flows.size)
        flows.forEach { flow ->
            assertTrue("bulan ${flow.month} tanpa pemasukan", flow.income > 0)
            // Nominal generator dijaga pada rentang Rp 1 - 7 juta per bulan.
            assertTrue("pengeluaran ${flow.month} terlalu kecil", flow.expense >= 900_000L)
            assertTrue("pengeluaran ${flow.month} terlalu besar", flow.expense <= 9_000_000L)
        }
    }

    @Test
    fun `data contoh menghasilkan saldo yang bertumbuh`() {
        val demo = DemoData.build(today)
        assertTrue(demo.balance() > 0L)
        val flows = demo.monthlyFlows(month, count = 6)
        assertTrue(flows.all { it.net > 0L })
    }
}
