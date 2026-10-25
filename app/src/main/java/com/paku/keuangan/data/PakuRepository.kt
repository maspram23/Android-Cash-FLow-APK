package com.paku.keuangan.data

import android.content.Context
import androidx.core.content.edit
import com.paku.keuangan.i18n.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Penyimpanan lokal sederhana (SharedPreferences + JSON) supaya aplikasi
 * tetap ringan dan bisa dipakai sepenuhnya offline.
 */
class PakuRepository(context: Context) {

    private val prefs = context.getSharedPreferences("paku_prefs", Context.MODE_PRIVATE)

    private val _transactions = MutableStateFlow(loadTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // ---------------- Transaksi ----------------

    fun save(transaction: Transaction) {
        updateTransactions { current ->
            val index = current.indexOfFirst { it.id == transaction.id }
            if (index >= 0) current.toMutableList().also { it[index] = transaction }
            else (current + transaction)
        }
    }

    fun delete(id: String) {
        updateTransactions { current -> current.filterNot { it.id == id } }
    }

    fun restore(transaction: Transaction) {
        updateTransactions { current ->
            if (current.any { it.id == transaction.id }) current else current + transaction
        }
    }

    fun replaceAll(transactions: List<Transaction>) {
        updateTransactions { transactions }
    }

    fun clearAll() {
        updateTransactions { emptyList() }
    }

    private fun updateTransactions(transform: (List<Transaction>) -> List<Transaction>) {
        val next = transform(_transactions.value).sortedWith(
            compareByDescending<Transaction> { it.date }.thenByDescending { it.createdAt }
        )
        _transactions.value = next
        persistTransactions(next)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private fun persistTransactions(transactions: List<Transaction>) {
        val array = JSONArray()
        transactions.forEach { tx ->
            array.put(
                JSONObject()
                    .put("id", tx.id)
                    .put("type", tx.type.name)
                    .put("amount", tx.amount)
                    .put("category", tx.categoryId)
                    .put("note", tx.note)
                    .put("date", tx.date.toString())
                    .put("createdAt", tx.createdAt)
            )
        }
        prefs.edit().putString(KEY_TRANSACTIONS, array.toString()).apply()
    }

    private fun loadTransactions(): List<Transaction> {
        val raw = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val type = runCatching { TxType.valueOf(obj.getString("type")) }
                        .getOrDefault(TxType.EXPENSE)
                    val categoryId = obj.optString("category")
                    val known = Categories.all.any { it.id == categoryId }
                    add(
                        Transaction(
                            id = obj.optString("id").ifBlank { "tx-$i" },
                            type = type,
                            amount = obj.optLong("amount"),
                            categoryId = if (known) categoryId
                            else if (type == TxType.INCOME) "other_income" else "other_expense",
                            note = obj.optString("note"),
                            date = runCatching { LocalDate.parse(obj.getString("date")) }
                                .getOrDefault(LocalDate.now()),
                            createdAt = obj.optLong("createdAt", i.toLong())
                        )
                    )
                }
            }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.createdAt })
        }.getOrDefault(emptyList())
    }

    // ---------------- Pengaturan ----------------

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
        _settings.value = next
        prefs.edit {
            putString(KEY_NAME, next.userName)
            putString(KEY_THEME, next.themeMode.name)
            putString(KEY_LANGUAGE, next.languageMode.name)
            putBoolean(KEY_ONBOARDED, next.onboarded)
        }
    }

    private fun loadSettings(): AppSettings = AppSettings(
        userName = prefs.getString(KEY_NAME, null)?.takeIf { it.isNotBlank() } ?: "",
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM),
        languageMode = runCatching {
            LanguageMode.valueOf(
                prefs.getString(KEY_LANGUAGE, null) ?: LanguageMode.SYSTEM.name
            )
        }.getOrDefault(LanguageMode.SYSTEM),
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false)
    )

    companion object {
        private const val KEY_TRANSACTIONS = "transactions_json"
        private const val KEY_NAME = "user_name"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "language_mode"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_SEEDED = "seeded"
    }
}
