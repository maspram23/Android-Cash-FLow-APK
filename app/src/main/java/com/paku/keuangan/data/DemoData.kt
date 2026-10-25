package com.paku.keuangan.data

import com.paku.keuangan.i18n.IdStrings
import com.paku.keuangan.i18n.Strings
import java.time.LocalDate
import java.time.YearMonth
import kotlin.random.Random

/**
 * Data contoh yang realistis untuk memperkenalkan grafik & statistik.
 * Nominal dalam Rupiah, tersebar pada enam bulan terakhir. Catatan transaksi
 * mengikuti bahasa yang sedang dipakai aplikasi.
 */
object DemoData {

    fun build(
        today: LocalDate = LocalDate.now(),
        strings: Strings = IdStrings
    ): List<Transaction> {
        val foodNotes = strings.demoFoodNotes
        val transportNotes = strings.demoTransportNotes
        val shoppingNotes = strings.demoShoppingNotes
        val billNotes = strings.demoBillNotes
        val funNotes = strings.demoFunNotes
        val healthNotes = strings.demoHealthNotes
        val educationNotes = strings.demoEducationNotes
        val homeNotes = strings.demoHomeNotes
        val otherNotes = strings.demoOtherNotes

        val random = Random(today.toEpochDay())
        val result = mutableListOf<Transaction>()
        var seq = 0L

        fun add(
            type: TxType,
            amount: Long,
            categoryId: String,
            note: String,
            date: LocalDate
        ) {
            seq += 1
            result += Transaction(
                type = type,
                amount = amount,
                categoryId = categoryId,
                note = note,
                date = date,
                createdAt = date.toEpochDay() * 100_000 + seq
            )
        }

        for (back in 5 downTo 0) {
            val month = YearMonth.from(today).minusMonths(back.toLong())
            val lastDay = if (back == 0) today.dayOfMonth else month.lengthOfMonth()
            fun day(d: Int): LocalDate = month.atDay(d.coerceIn(1, lastDay))
            fun randomDay(): LocalDate = month.atDay(random.nextInt(1, lastDay + 1))

            // ---- Pemasukan ----
            val salaryBase = 8_500_000L + random.nextInt(0, 6) * 100_000L
            add(
                TxType.INCOME, salaryBase, "salary", strings.demoSalaryNote,
                month.atDay(minOf(25, lastDay))
            )

            if (back % 3 == 1) {
                add(
                    TxType.INCOME, 1_500_000L + random.nextInt(0, 5) * 100_000L,
                    "bonus", strings.demoBonusNote, day(minOf(10, lastDay))
                )
            }
            if (back % 2 == 0) {
                add(
                    TxType.INCOME, 350_000L + random.nextInt(0, 10) * 50_000L,
                    "business", strings.demoBusinessNote, randomDay()
                )
            }
            if (back == 4 || back == 1) {
                add(
                    TxType.INCOME, 180_000L + random.nextInt(0, 6) * 20_000L,
                    "investment", strings.demoInvestmentNote, day(minOf(18, lastDay))
                )
            }

            // ---- Pengeluaran ----
            // Nominal ditulis dalam Rupiah penuh. Total pengeluaran bulanan
            // berkisar Rp 1 - 7 juta sehingga saldo selalu bertumbuh.
            repeat(random.nextInt(12, 18)) {
                add(
                    TxType.EXPENSE,
                    (15L + random.nextInt(0, 46)) * 1_000L,
                    "food", foodNotes.random(random), randomDay()
                )
            }
            repeat(random.nextInt(6, 11)) {
                add(
                    TxType.EXPENSE,
                    (10L + random.nextInt(0, 36)) * 1_000L,
                    "transport", transportNotes.random(random), randomDay()
                )
            }
            repeat(random.nextInt(2, 5)) {
                add(
                    TxType.EXPENSE,
                    (120L + random.nextInt(0, 281)) * 1_000L,
                    "shopping", shoppingNotes.random(random), randomDay()
                )
            }
            repeat(random.nextInt(2, 5)) {
                add(
                    TxType.EXPENSE,
                    (250L + random.nextInt(0, 451)) * 1_000L,
                    "bills", billNotes.random(random), randomDay()
                )
            }
            repeat(random.nextInt(1, 4)) {
                add(
                    TxType.EXPENSE,
                    (50L + random.nextInt(0, 181)) * 1_000L,
                    "entertainment", funNotes.random(random), randomDay()
                )
            }
            if (random.nextInt(2) == 0) {
                add(
                    TxType.EXPENSE,
                    (70L + random.nextInt(0, 201)) * 1_000L,
                    "health", healthNotes.random(random), randomDay()
                )
            }
            if (random.nextInt(2) == 0) {
                add(
                    TxType.EXPENSE,
                    (120L + random.nextInt(0, 251)) * 1_000L,
                    "education", educationNotes.random(random), randomDay()
                )
            }
            repeat(random.nextInt(1, 3)) {
                add(
                    TxType.EXPENSE,
                    (80L + random.nextInt(0, 271)) * 1_000L,
                    "home", homeNotes.random(random), randomDay()
                )
            }
            repeat(random.nextInt(1, 4)) {
                add(
                    TxType.EXPENSE,
                    (15L + random.nextInt(0, 86)) * 1_000L,
                    "other_expense", otherNotes.random(random), randomDay()
                )
            }
        }

        return result.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.createdAt })
    }
}
