package com.paku.keuangan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paku.keuangan.data.AppSettings
import com.paku.keuangan.data.Category
import com.paku.keuangan.data.DemoData
import com.paku.keuangan.data.PakuRepository
import com.paku.keuangan.data.ThemeMode
import com.paku.keuangan.data.Transaction
import com.paku.keuangan.data.TxType
import com.paku.keuangan.i18n.LanguageMode
import com.paku.keuangan.i18n.Strings
import com.paku.keuangan.i18n.strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

/** Metrik yang ditampilkan pada diagram batang. Labelnya ada di katalog bahasa. */
enum class BarMetric {
    BOTH,
    INCOME,
    EXPENSE
}

data class UiFilters(
    val selectedMonth: YearMonth = YearMonth.now(),
    val search: String = "",
    val typeFilter: TxType? = null,
    val focusCategoryId: String? = null,
    val focusMonth: YearMonth? = null,
    val pieType: TxType = TxType.EXPENSE,
    val barMetric: BarMetric = BarMetric.BOTH
)

data class PakuUiState(
    val settings: AppSettings = AppSettings(),
    val transactions: List<Transaction> = emptyList(),
    val filters: UiFilters = UiFilters()
) {
    val selectedMonth: YearMonth get() = filters.selectedMonth
}

class PakuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PakuRepository(application)
    private val _filters = MutableStateFlow(UiFilters())

    val uiState: StateFlow<PakuUiState> = combine(
        repository.transactions,
        repository.settings,
        _filters
    ) { transactions, settings, filters ->
        PakuUiState(settings = settings, transactions = transactions, filters = filters)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PakuUiState(
            settings = repository.settings.value,
            transactions = repository.transactions.value
        )
    )

    // ------------------------- Transaksi -------------------------

    private fun currentStrings(): Strings =
        repository.settings.value.languageMode.resolve().strings()

    fun save(transaction: Transaction) = repository.save(transaction)

    fun delete(transaction: Transaction) = repository.delete(transaction.id)

    fun restore(transaction: Transaction) = repository.restore(transaction)

    fun clearAll() = repository.clearAll()

    fun loadDemoData() = repository.replaceAll(DemoData.build(strings = currentStrings()))

    // ------------------------- Pengaturan -------------------------

    fun completeOnboarding(name: String, withDemo: Boolean) {
        if (withDemo && repository.transactions.value.isEmpty()) {
            repository.replaceAll(DemoData.build(strings = currentStrings()))
        }
        repository.updateSettings {
            it.copy(
                userName = name.trim(),
                onboarded = true
            )
        }
    }

    fun updateName(name: String) {
        repository.updateSettings {
            it.copy(userName = name.trim())
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.updateSettings { it.copy(themeMode = mode) }
    }

    fun setLanguageMode(mode: LanguageMode) {
        repository.updateSettings { it.copy(languageMode = mode) }
    }

    // ------------------------- Filter & navigasi -------------------------

    fun selectMonth(month: YearMonth) {
        _filters.update { it.copy(selectedMonth = month) }
    }

    fun shiftMonth(months: Long) {
        _filters.update {
            val next = it.selectedMonth.plusMonths(months)
            if (next.isAfter(YearMonth.now())) it else it.copy(selectedMonth = next)
        }
    }

    fun goToCurrentMonth() {
        _filters.update { it.copy(selectedMonth = YearMonth.now()) }
    }

    fun setSearch(query: String) {
        _filters.update { it.copy(search = query) }
    }

    fun setTypeFilter(type: TxType?) {
        _filters.update { it.copy(typeFilter = type) }
    }

    fun setPieType(type: TxType) {
        _filters.update { it.copy(pieType = type) }
    }

    fun setBarMetric(metric: BarMetric) {
        _filters.update { it.copy(barMetric = metric) }
    }

    /** Buka daftar transaksi yang sudah terfilter dari sebuah kategori. */
    fun focusCategory(category: Category, month: YearMonth?) {
        _filters.update {
            it.copy(
                focusCategoryId = category.id,
                focusMonth = month,
                typeFilter = category.type
            )
        }
    }

    fun clearFocus() {
        _filters.update { it.copy(focusCategoryId = null, focusMonth = null) }
    }

    fun clearTransactionFilters() {
        _filters.update {
            it.copy(search = "", typeFilter = null, focusCategoryId = null, focusMonth = null)
        }
    }
}
