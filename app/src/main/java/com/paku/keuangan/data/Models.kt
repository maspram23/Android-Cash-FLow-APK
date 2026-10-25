package com.paku.keuangan.data

import com.paku.keuangan.i18n.LanguageMode
import java.time.LocalDate
import java.util.UUID

/** Jenis arus kas. Teks labelnya ada di katalog bahasa (`Strings`). */
enum class TxType {
    INCOME,
    EXPENSE;

    val sign: String get() = if (this == INCOME) "+" else "-"
}

/** Satu kategori pemasukan / pengeluaran. */
data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Long,
    val type: TxType
)

object Categories {

    val income: List<Category> = listOf(
        Category("salary", "Gaji", "💼", 0xFF2E9E7E, TxType.INCOME),
        Category("bonus", "Bonus", "🎁", 0xFFE3A33C, TxType.INCOME),
        Category("business", "Usaha", "🏪", 0xFF1C9BAA, TxType.INCOME),
        Category("investment", "Investasi", "📈", 0xFF4C7BD9, TxType.INCOME),
        Category("gift", "Hadiah", "🎉", 0xFFD06FA8, TxType.INCOME),
        Category("other_income", "Lainnya", "✨", 0xFF8C7AF0, TxType.INCOME)
    )

    val expense: List<Category> = listOf(
        Category("food", "Makan & Minum", "🍜", 0xFFE5684F, TxType.EXPENSE),
        Category("transport", "Transportasi", "🛵", 0xFF4C86D9, TxType.EXPENSE),
        Category("shopping", "Belanja", "🛍️", 0xFFD96FA8, TxType.EXPENSE),
        Category("bills", "Tagihan", "🧾", 0xFFE3A33C, TxType.EXPENSE),
        Category("entertainment", "Hiburan", "🎬", 0xFF8B6FD9, TxType.EXPENSE),
        Category("health", "Kesehatan", "💊", 0xFF2AA7A0, TxType.EXPENSE),
        Category("education", "Pendidikan", "📚", 0xFF5F7BD9, TxType.EXPENSE),
        Category("home", "Rumah", "🏠", 0xFFB8814E, TxType.EXPENSE),
        Category("other_expense", "Lainnya", "📦", 0xFF8A807C, TxType.EXPENSE)
    )

    val all: List<Category> = income + expense

    private val categoryById = all.associateBy { it.id }

    fun byId(id: String): Category =
        categoryById[id] ?: expense.last()

    fun of(type: TxType): List<Category> =
        if (type == TxType.INCOME) income else expense
}

/** Catatan transaksi arus kas. */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TxType,
    val amount: Long,
    val categoryId: String,
    val note: String = "",
    val date: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
) {
    val category: Category get() = Categories.byId(categoryId)

    /** Nominal dengan tanda +/- untuk tampilan daftar. */
    val signedAmount: Long get() = if (type == TxType.INCOME) amount else -amount
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val userName: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val onboarded: Boolean = false
)
