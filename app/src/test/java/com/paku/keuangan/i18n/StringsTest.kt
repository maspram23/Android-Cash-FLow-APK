package com.paku.keuangan.i18n

import com.paku.keuangan.data.Categories
import com.paku.keuangan.data.DemoData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/** Memastikan katalog Indonesia & Inggris tetap konsisten dan terformat benar. */
class StringsTest {

    @Test
    fun `language mode resolves to the expected language`() {
        assertEquals(AppLanguage.INDONESIAN, LanguageMode.INDONESIAN.resolve(Locale.US))
        assertEquals(AppLanguage.ENGLISH, LanguageMode.ENGLISH.resolve(legacyIndonesian))
        assertEquals(AppLanguage.INDONESIAN, LanguageMode.SYSTEM.resolve(legacyIndonesian))
        assertEquals(
            AppLanguage.INDONESIAN,
            LanguageMode.SYSTEM.resolve(Locale.forLanguageTag("id-ID"))
        )
        assertEquals(AppLanguage.ENGLISH, LanguageMode.SYSTEM.resolve(Locale.US))
    }

    @Test
    fun `rupiah keeps indonesian thousand separators in both languages`() {
        assertEquals("Rp 32.520.000", IdStrings.rupiah(32_520_000))
        assertEquals("Rp 32.520.000", EnStrings.rupiah(32_520_000))
        assertEquals("+ Rp 10.000", IdStrings.rupiahSigned(10_000))
        assertEquals("− Rp 10.000", EnStrings.rupiahSigned(-10_000))
    }

    @Test
    fun `compact amounts follow each language convention`() {
        assertEquals("1,3 jt", IdStrings.compact(1_250_000))
        assertEquals("1.3M", EnStrings.compact(1_250_000))
        assertEquals("15 rb", IdStrings.compact(15_000))
        assertEquals("15K", EnStrings.compact(15_000))
        assertEquals("2 M", IdStrings.compact(2_000_000_000))
        assertEquals("2B", EnStrings.compact(2_000_000_000))
    }

    @Test
    fun `percent uses the language decimal separator`() {
        assertEquals("36%", IdStrings.percent(0.36f))
        assertEquals("36%", EnStrings.percent(0.36f))
        assertEquals("36,5%", IdStrings.percent(0.365f, 1))
        assertEquals("36.5%", EnStrings.percent(0.365f, 1))
    }

    @Test
    fun `month and day labels are localised`() {
        val october = YearMonth.of(2026, 10)
        assertEquals("Oktober 2026", IdStrings.monthLabel(october))
        assertEquals("October 2026", EnStrings.monthLabel(october))

        val today = LocalDate.of(2026, 10, 25)
        assertEquals("Hari ini", IdStrings.dayHeaderLabel(today, today))
        assertEquals("Today", EnStrings.dayHeaderLabel(today, today))
        assertEquals("Kemarin", IdStrings.dayHeaderLabel(today.minusDays(1), today))
        assertEquals("Yesterday", EnStrings.dayHeaderLabel(today.minusDays(1), today))
    }

    @Test
    fun `greeting depends on the hour`() {
        assertEquals("Selamat pagi", IdStrings.greeting(7))
        assertEquals("Good evening", EnStrings.greeting(19))
        assertEquals("Selamat malam", IdStrings.greeting(23))
    }

    @Test
    fun `every category has a translated name`() {
        Categories.all.forEach { category ->
            val id = IdStrings.categoryName(category.id)
            val en = EnStrings.categoryName(category.id)
            assertTrue("id name blank for ${category.id}", id.isNotBlank())
            assertTrue("en name blank for ${category.id}", en.isNotBlank())
            assertNotEquals("en name not translated for ${category.id}", category.id, en)
        }
    }

    @Test
    fun `combined slice label follows the language`() {
        assertEquals("Lainnya", IdStrings.categoryName("other_expense"))
        assertEquals("Other", EnStrings.categoryName("other_expense"))
    }

    @Test
    fun `demo data notes follow the active language`() {
        val today = LocalDate.of(2026, 10, 25)
        val indonesian = DemoData.build(today, IdStrings)
        val english = DemoData.build(today, EnStrings)

        assertEquals(indonesian.size, english.size)
        assertTrue(indonesian.any { it.note == "Gaji bulanan" })
        assertTrue(english.any { it.note == "Monthly salary" })
        assertTrue(english.none { it.note == "Gaji bulanan" })
    }

    private companion object {
        /** Android memakai kode bahasa lama "in" untuk Indonesia. */
        @Suppress("DEPRECATION")
        val legacyIndonesian: Locale = Locale("in", "ID")
    }
}
