package com.paku.keuangan.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.paku.keuangan.data.Categories
import com.paku.keuangan.data.TxType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** Pilihan bahasa aplikasi. */
enum class LanguageMode {
    SYSTEM,
    INDONESIAN,
    ENGLISH;

    fun resolve(systemLocale: Locale = Locale.getDefault()): AppLanguage = when (this) {
        // Android memakai kode lama "in", JVM modern memakai "id".
        SYSTEM -> if (systemLocale.language in INDONESIAN_CODES) {
            AppLanguage.INDONESIAN
        } else {
            AppLanguage.ENGLISH
        }
        INDONESIAN -> AppLanguage.INDONESIAN
        ENGLISH -> AppLanguage.ENGLISH
    }

    private companion object {
        val INDONESIAN_CODES = setOf("in", "id")
    }
}

/** Bahasa yang benar-benar dipakai setelah [LanguageMode] diselesaikan. */
enum class AppLanguage(val languageCode: String, val locale: Locale) {
    INDONESIAN("in", Locale("in", "ID")),
    ENGLISH("en", Locale.ENGLISH)
}

fun AppLanguage.strings(): Strings = when (this) {
    AppLanguage.INDONESIAN -> IdStrings
    AppLanguage.ENGLISH -> EnStrings
}

/** Katalog teks aktif. Diisi oleh `PakuTheme` sesuai pengaturan pengguna. */
val LocalStrings = staticCompositionLocalOf<Strings> { IdStrings }

/**
 * Katalog teks + pemformat angka dan tanggal untuk satu bahasa.
 *
 * Nominal selalu memakai kaidah Rupiah (pemisah ribuan titik), sedangkan nama
 * bulan dan sapaan mengikuti bahasa yang dipilih.
 */
abstract class Strings(val language: AppLanguage) {

    private val rupiahFormat = DecimalFormat("#,##0", DecimalFormatSymbols(ID_LOCALE))
    private val compactFormat = DecimalFormat("0.#", DecimalFormatSymbols(language.locale)).apply {
        // Uang lebih wajar dibulatkan ke atas pada angka tengah (1,25 -> 1,3).
        roundingMode = RoundingMode.HALF_UP
    }

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", language.locale)
    private val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM", language.locale)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", language.locale)
    private val dayDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", language.locale)

    // ------------------------- Format angka & tanggal -------------------------

    fun rupiah(value: Long): String = "Rp " + rupiahFormat.format(value)

    fun rupiahSigned(value: Long): String {
        val sign = if (value >= 0) "+" else "−"
        return "$sign Rp ${rupiahFormat.format(abs(value))}"
    }

    /** 1.250.000 -> "1,3 jt" (Indonesia) atau "1.3M" (Inggris). */
    fun compact(value: Long): String {
        val v = abs(value)
        return when {
            v >= 1_000_000_000L -> compactFormat.format(v / 1_000_000_000.0) + billionSuffix
            v >= 1_000_000L -> compactFormat.format(v / 1_000_000.0) + millionSuffix
            v >= 1_000L -> compactFormat.format(v / 1_000.0) + thousandSuffix
            else -> v.toString()
        }
    }

    fun compactRupiah(value: Long): String = "Rp " + compact(value)

    fun percent(value: Float, decimals: Int = 0): String {
        val pattern = if (decimals == 0) "0" else "0.${"#".repeat(decimals)}"
        return DecimalFormat(pattern, DecimalFormatSymbols(language.locale)).format(value * 100f) + "%"
    }

    fun monthLabel(month: YearMonth): String = month.format(monthFormatter)

    fun shortMonthLabel(month: YearMonth): String = month.format(shortMonthFormatter)

    fun fullDateLabel(date: LocalDate): String = date.format(fullDateFormatter)

    fun dayHeaderLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
        today -> todayLabel
        today.minusDays(1) -> yesterdayLabel
        else -> date.format(dayDateFormatter)
    }

    fun greeting(hour: Int = LocalTime.now().hour): String = when (hour) {
        in 4..10 -> greetingMorning
        in 11..14 -> greetingNoon
        in 15..17 -> greetingEvening
        else -> greetingNight
    }

    // ------------------------- Satuan angka ringkas -------------------------

    protected abstract val thousandSuffix: String
    protected abstract val millionSuffix: String
    protected abstract val billionSuffix: String

    // ------------------------- Nama hari & sapaan -------------------------

    abstract val todayLabel: String
    abstract val yesterdayLabel: String
    abstract val greetingMorning: String
    abstract val greetingNoon: String
    abstract val greetingEvening: String
    abstract val greetingNight: String

    // ------------------------- Merek & umum -------------------------

    abstract val appName: String
    abstract val appTagline: String
    abstract val developerName: String
    abstract val developerRole: String
    abstract val defaultUserName: String
    abstract val logoDescription: String

    abstract val income: String
    abstract val expense: String
    abstract val incomeShort: String
    abstract val expenseShort: String
    abstract val both: String

    abstract val category: String
    abstract val date: String
    abstract val note: String
    abstract val all: String
    abstract val thisMonth: String

    abstract val cancel: String
    abstract val save: String
    abstract val delete: String
    abstract val close: String
    abstract val select: String
    abstract val change: String
    abstract val detail: String
    abstract val record: String

    /** Nama tampilan kategori (mis. "Makan & Minum"). */
    abstract fun categoryName(categoryId: String): String

    fun typeLabel(type: TxType): String = if (type == TxType.INCOME) income else expense

    fun typeLabelShort(type: TxType): String = if (type == TxType.INCOME) incomeShort else expenseShort

    // ------------------------- Splash & onboarding -------------------------

    abstract val splashTagline: String
    abstract val onboardingTitle: String
    abstract val onboardingBody: String
    abstract val onboardingNameQuestion: String
    abstract val onboardingStartQuestion: String
    abstract val onboardingDemoTitle: String
    abstract val onboardingDemoSubtitle: String
    abstract val onboardingDemoBadge: String
    abstract val onboardingEmptyTitle: String
    abstract val onboardingEmptySubtitle: String
    abstract val onboardingCta: String
    abstract val onboardingPrivacy: String

    // ------------------------- Navigasi -------------------------

    abstract val tabHome: String
    abstract val tabStats: String
    abstract val tabTransactions: String
    abstract val tabSettings: String
    abstract val addTransaction: String

    // ------------------------- Beranda -------------------------

    abstract val balanceNow: String
    abstract val monthSummaryTitle: String
    abstract val monthSummaryCurrent: String
    abstract val monthSummarySelected: String
    abstract val backToThisMonth: String
    abstract val expenseByCategory: String
    abstract val totalExpense: String
    abstract val noExpenseTitle: String
    abstract val noExpenseBody: String
    abstract val recordExpense: String
    abstract val sixMonthCashflow: String
    abstract val tapBarToSeeMonth: String
    abstract val homeChartEmptyTitle: String
    abstract val homeChartEmptyBody: String
    abstract val recentTransactions: String
    abstract val seeAll: String
    abstract val homeEmptyTitle: String
    abstract val homeEmptyBody: String
    abstract val recordNow: String

    abstract fun incomeThisMonth(amount: String): String
    abstract fun recordsSaved(count: Int): String

    // ------------------------- Statistik -------------------------

    abstract val statsTitle: String
    abstract val statsSubtitle: String
    abstract val period: String
    abstract val periodHint: String
    abstract val sixMonthTrend: String
    abstract val tapBarToSwitchMonth: String
    abstract val trendEmptyTitle: String
    abstract val trendEmptyBody: String
    abstract val categoryBreakdown: String
    abstract val tapCategoryHint: String
    abstract val noCategoryData: String
    abstract val seeAllTransactions: String
    abstract val statsTip: String
    abstract val insightTitle: String
    abstract val insightNoComparison: String

    abstract fun composition(type: TxType): String
    abstract fun totalOf(type: TxType): String
    abstract fun noDataType(type: TxType): String
    abstract val noDataTypeBody: String
    abstract fun insightNoData(type: TxType): String
    abstract fun insightTopExpense(categoryName: String, percent: String): String
    abstract fun insightTopIncome(categoryName: String, percent: String): String
    abstract fun insightUp(percent: String): String
    abstract fun insightDown(percent: String): String
    abstract fun insightDaily(changeText: String, average: String, days: Int): String
    abstract fun transactionCount(count: Int): String
    abstract fun monthTransactionSubtitle(month: String, count: Int): String

    // ------------------------- Riwayat transaksi -------------------------

    abstract val transactionsTitle: String
    abstract val transactionsSubtitle: String
    abstract val noRecordsTitle: String
    abstract val noRecordsBody: String
    abstract val noResultsTitle: String
    abstract val noResultsBody: String
    abstract val searchHint: String
    abstract val clearSearch: String
    abstract val clearCategoryFilter: String
    abstract val resetFilter: String

    abstract fun recordsCount(count: Int): String

    // ------------------------- Form transaksi -------------------------

    abstract val formTitleAdd: String
    abstract val formTitleEdit: String
    abstract val deleteTransaction: String
    abstract val amount: String
    abstract val quickAdd10: String
    abstract val quickAdd50: String
    abstract val quickAdd100: String
    abstract val clearDigit: String
    abstract val noteOptional: String
    abstract val notePlaceholder: String
    abstract val saveTransaction: String
    abstract val saveChanges: String
    abstract val pickDate: String
    abstract val deleteConfirmTitle: String
    abstract val deleteConfirmBody: String

    // ------------------------- Pengaturan -------------------------

    abstract val settingsTitle: String
    abstract val settingsSubtitle: String
    abstract val nicknameLabel: String
    abstract val nicknameDialogTitle: String
    abstract val nicknameDialogBody: String
    abstract val recordsStatLabel: String
    abstract val appearance: String
    abstract val appearanceHint: String
    abstract val themeSystem: String
    abstract val themeLight: String
    abstract val themeDark: String
    abstract val languageLabel: String
    abstract val languageHint: String
    abstract val languageSystem: String
    abstract val languageIndonesian: String
    abstract val languageEnglish: String
    abstract val dataBackup: String
    abstract val dataBackupHint: String
    abstract val exportCsv: String
    abstract val exportEmpty: String
    abstract val exportSuccess: String
    abstract val exportFailed: String
    abstract val exportCannotOpen: String
    abstract val loadSample: String
    abstract val loadSampleHint: String
    abstract val sampleLoaded: String
    abstract val deleteAllData: String
    abstract val deleteAllDataHint: String
    abstract val deleteAllConfirmTitle: String
    abstract val deleteAllConfirmBody: String
    abstract val deleteAllConfirmAction: String
    abstract val deleteAllDone: String
    abstract val replaceSampleTitle: String
    abstract val replaceSampleBody: String
    abstract val aboutTitle: String
    abstract val aboutBody: String
    abstract val aboutFooter: String
    abstract val madeWith: String
    abstract val csvHeader: String

    abstract fun trackingSince(month: String): String
    abstract fun exportReady(count: Int): String

    // ------------------------- Diagram -------------------------

    abstract val tapSliceHint: String
    abstract val previousMonth: String
    abstract val nextMonth: String

    // ------------------------- Pesan singkat -------------------------

    abstract val transactionDeleted: String
    abstract val undo: String
    abstract val changesSaved: String
    abstract val transactionSaved: String

    // ------------------------- Data contoh -------------------------

    abstract val demoSalaryNote: String
    abstract val demoBonusNote: String
    abstract val demoBusinessNote: String
    abstract val demoInvestmentNote: String
    abstract val demoFoodNotes: List<String>
    abstract val demoTransportNotes: List<String>
    abstract val demoShoppingNotes: List<String>
    abstract val demoBillNotes: List<String>
    abstract val demoFunNotes: List<String>
    abstract val demoHealthNotes: List<String>
    abstract val demoEducationNotes: List<String>
    abstract val demoHomeNotes: List<String>
    abstract val demoOtherNotes: List<String>

    companion object {
        private val ID_LOCALE = Locale("in", "ID")
    }
}

// ---------------------------------------------------------------------------
// Bahasa Indonesia (katalog utama aplikasi)
// ---------------------------------------------------------------------------

object IdStrings : Strings(AppLanguage.INDONESIAN) {

    override val thousandSuffix = " rb"
    override val millionSuffix = " jt"
    override val billionSuffix = " M"

    override val todayLabel = "Hari ini"
    override val yesterdayLabel = "Kemarin"
    override val greetingMorning = "Selamat pagi"
    override val greetingNoon = "Selamat siang"
    override val greetingEvening = "Selamat sore"
    override val greetingNight = "Selamat malam"

    override val appName = "PAKU"
    override val appTagline = "PencAtat Keuangan kamU"
    override val developerName = "Sigit Pramono"
    override val developerRole = "Pengembang"
    override val defaultUserName = "Sahabat PAKU"
    override val logoDescription = "Logo PAKU"

    override val income = "Pemasukan"
    override val expense = "Pengeluaran"
    override val incomeShort = "Masuk"
    override val expenseShort = "Keluar"
    override val both = "Keduanya"

    override val category = "Kategori"
    override val date = "Tanggal"
    override val note = "Catatan"
    override val all = "Semua"
    override val thisMonth = "Bulan ini"

    override val cancel = "Batal"
    override val save = "Simpan"
    override val delete = "Hapus"
    override val close = "Tutup"
    override val select = "Pilih"
    override val change = "Ubah"
    override val detail = "Detail"
    override val record = "Catat"

    override fun categoryName(categoryId: String): String =
        Categories.all.firstOrNull { it.id == categoryId }?.name ?: categoryId

    override val splashTagline = "Kelola uang dengan lebih tenang"
    override val onboardingTitle = "Selamat datang di PAKU"
    override val onboardingBody =
        "Catat arus kas, pahami ke mana uangmu pergi, dan lihat perkembangannya lewat diagram yang interaktif."
    override val onboardingNameQuestion = "Siapa nama panggilanmu?"
    override val onboardingStartQuestion = "Mau mulai bagaimana?"
    override val onboardingDemoTitle = "Coba data contoh"
    override val onboardingDemoSubtitle = "Langsung lihat diagram & statistik"
    override val onboardingDemoBadge = "Disarankan"
    override val onboardingEmptyTitle = "Mulai dari kosong"
    override val onboardingEmptySubtitle = "Catat transaksi milikmu dari awal"
    override val onboardingCta = "Mulai Sekarang"
    override val onboardingPrivacy = "🔒  Data tersimpan di perangkatmu · tanpa internet"

    override val tabHome = "Beranda"
    override val tabStats = "Statistik"
    override val tabTransactions = "Transaksi"
    override val tabSettings = "Atur"
    override val addTransaction = "Catat transaksi"

    override val balanceNow = "SALDO SAAT INI"
    override val monthSummaryTitle = "Ringkasan bulan"
    override val monthSummaryCurrent = "Bulan berjalan"
    override val monthSummarySelected = "Arsip bulan pilihan"
    override val backToThisMonth = "Kembali ke bulan ini"
    override val expenseByCategory = "Pengeluaran per kategori"
    override val totalExpense = "Total pengeluaran"
    override val noExpenseTitle = "Belum ada pengeluaran"
    override val noExpenseBody = "Catat pengeluaran bulan ini, diagramnya akan langsung terisi."
    override val recordExpense = "Catat pengeluaran"
    override val sixMonthCashflow = "Arus kas 6 bulan"
    override val tapBarToSeeMonth = "Ketuk batang untuk melihat bulan tersebut"
    override val homeChartEmptyTitle = "Grafik masih kosong"
    override val homeChartEmptyBody = "Tambahkan transaksi untuk melihat perbandingan arus kas."
    override val recentTransactions = "Transaksi terbaru"
    override val seeAll = "Lihat semua"
    override val homeEmptyTitle = "Mulai catat arus kas"
    override val homeEmptyBody = "Setiap catatan kecil membantumu memahami pola keuangan."
    override val recordNow = "Catat sekarang"

    override fun incomeThisMonth(amount: String) = "Pemasukan bulan ini $amount"
    override fun recordsSaved(count: Int) = "$count catatan tersimpan"

    override val statsTitle = "Statistik"
    override val statsSubtitle = "Pahami ke mana uangmu pergi"
    override val period = "Periode"
    override val periodHint = "Pilih bulan yang ingin dianalisis"
    override val sixMonthTrend = "Tren 6 bulan"
    override val tapBarToSwitchMonth = "Ketuk batang untuk berpindah bulan"
    override val trendEmptyTitle = "Belum ada arus kas"
    override val trendEmptyBody = "Tambahkan transaksi untuk melihat tren enam bulan terakhir."
    override val categoryBreakdown = "Rincian kategori"
    override val tapCategoryHint = "Ketuk kategori untuk melihat transaksinya"
    override val noCategoryData = "Belum ada data kategori pada bulan ini."
    override val seeAllTransactions = "Lihat semua transaksi"
    override val statsTip =
        "Bandingkan beberapa bulan untuk melihat pola pengeluaranmu. Data yang konsisten membuat keputusan lebih mudah."
    override val insightTitle = "Insight bulan ini"
    override val insightNoComparison = "Belum ada pembanding dari bulan sebelumnya."

    override fun composition(type: TxType) = "Komposisi ${typeLabel(type).lowercase()}"
    override fun totalOf(type: TxType) = "Total ${typeLabel(type).lowercase()}"
    override fun noDataType(type: TxType) = "Belum ada ${typeLabel(type).lowercase()}"
    override val noDataTypeBody =
        "Data bulan ini masih kosong. Tambahkan transaksi untuk melihat komposisinya."
    override fun insightNoData(type: TxType) =
        "Belum ada ${typeLabel(type).lowercase()} untuk dianalisis. Mulai catat transaksi pertamamu."

    override fun insightTopExpense(categoryName: String, percent: String) =
        "$categoryName menjadi pengeluaran terbesar ($percent dari total)."

    override fun insightTopIncome(categoryName: String, percent: String) =
        "Sumber pemasukan terbesar adalah $categoryName ($percent dari total)."

    override fun insightUp(percent: String) = "Naik $percent dibanding bulan lalu."
    override fun insightDown(percent: String) = "Turun $percent dibanding bulan lalu."
    override fun insightDaily(changeText: String, average: String, days: Int) =
        "$changeText Rata-rata $average per hari aktif ($days hari)."

    override fun transactionCount(count: Int) = "$count transaksi"
    override fun monthTransactionSubtitle(month: String, count: Int) = "$month · $count transaksi"

    override val transactionsTitle = "Transaksi"
    override val transactionsSubtitle = "Semua catatan arus kasmu"
    override val noRecordsTitle = "Belum ada catatan"
    override val noRecordsBody = "Mulai catat pemasukan dan pengeluaran pertamamu hari ini."
    override val noResultsTitle = "Tidak ada hasil"
    override val noResultsBody = "Coba kata kunci lain atau ubah filter yang dipilih."
    override val searchHint = "Cari catatan atau kategori..."
    override val clearSearch = "Hapus pencarian"
    override val clearCategoryFilter = "Hapus filter kategori"
    override val resetFilter = "Reset filter"

    override fun recordsCount(count: Int) = "$count catatan"

    override val formTitleAdd = "Catat Transaksi"
    override val formTitleEdit = "Ubah Transaksi"
    override val deleteTransaction = "Hapus transaksi"
    override val amount = "Nominal"
    override val quickAdd10 = "+10 rb"
    override val quickAdd50 = "+50 rb"
    override val quickAdd100 = "+100 rb"
    override val clearDigit = "Hapus angka"
    override val noteOptional = "Catatan (opsional)"
    override val notePlaceholder = "Contoh: Makan siang"
    override val saveTransaction = "Simpan Transaksi"
    override val saveChanges = "Simpan Perubahan"
    override val pickDate = "Pilih tanggal"
    override val deleteConfirmTitle = "Hapus transaksi?"
    override val deleteConfirmBody = "Catatan ini akan dihapus dari riwayat keuanganmu."

    override val settingsTitle = "Pengaturan"
    override val settingsSubtitle = "Sesuaikan PAKU dengan gayamu"
    override val nicknameLabel = "Nama panggilanmu"
    override val nicknameDialogTitle = "Nama panggilan"
    override val nicknameDialogBody = "Nama ini akan tampil di beranda PAKU."
    override val recordsStatLabel = "Catatan"
    override val appearance = "Tampilan"
    override val appearanceHint = "Pilih tema yang paling nyaman untukmu"
    override val themeSystem = "Ikuti Sistem"
    override val themeLight = "Terang"
    override val themeDark = "Gelap"
    override val languageLabel = "Bahasa"
    override val languageHint = "Pilih bahasa tampilan aplikasi"
    override val languageSystem = "Ikuti Sistem"
    override val languageIndonesian = "Indonesia"
    override val languageEnglish = "English"
    override val dataBackup = "Data & cadangan"
    override val dataBackupHint = "Semua data tersimpan lokal di perangkatmu"
    override val exportCsv = "Ekspor data (CSV)"
    override val exportEmpty = "Belum ada transaksi untuk diekspor"
    override val exportSuccess = "Data berhasil diekspor ke CSV"
    override val exportFailed = "Gagal mengekspor data"
    override val exportCannotOpen = "Tidak bisa membuka file"
    override val loadSample = "Muat data contoh"
    override val loadSampleHint = "Isi grafik dengan transaksi contoh"
    override val sampleLoaded = "Data contoh berhasil dimuat"
    override val deleteAllData = "Hapus semua data"
    override val deleteAllDataHint = "Hapus seluruh catatan secara permanen"
    override val deleteAllConfirmTitle = "Hapus semua data?"
    override val deleteAllConfirmBody =
        "Seluruh transaksi akan dihapus permanen. Tindakan ini tidak bisa dibatalkan."
    override val deleteAllConfirmAction = "Hapus semua"
    override val deleteAllDone = "Semua data telah dihapus"
    override val replaceSampleTitle = "Ganti dengan data contoh?"
    override val replaceSampleBody =
        "Data yang sekarang akan diganti dengan transaksi contoh selama enam bulan terakhir."
    override val aboutTitle = "Tentang aplikasi"
    override val aboutBody =
        "PAKU membantumu mencatat arus kas, memahami kebiasaan belanja, dan melihat perkembangan keuangan lewat diagram yang mudah dibaca."
    override val aboutFooter = "Tanpa internet. Tanpa akun. Privasimu tetap milikmu."
    override val madeWith = "Dibuat dengan ❤️ untuk keuangan yang lebih rapi."
    override val csvHeader = "Tanggal;Jenis;Kategori;Catatan;Nominal"

    override fun trackingSince(month: String) = "Mencatat sejak $month"
    override fun exportReady(count: Int) = "$count transaksi siap diekspor"

    override val tapSliceHint = "Ketuk potongan untuk melihat detail"
    override val previousMonth = "Bulan sebelumnya"
    override val nextMonth = "Bulan berikutnya"

    override val transactionDeleted = "Transaksi dihapus"
    override val undo = "Urungkan"
    override val changesSaved = "Perubahan disimpan"
    override val transactionSaved = "Transaksi disimpan"

    override val demoSalaryNote = "Gaji bulanan"
    override val demoBonusNote = "Bonus proyek"
    override val demoBusinessNote = "Penjualan toko online"
    override val demoInvestmentNote = "Dividen reksa dana"
    override val demoFoodNotes = listOf(
        "Makan siang", "Sarapan bubur", "Kopi & camilan", "Makan malam",
        "Pesan antar makanan", "Bakso malang", "Nasi padang", "Seblak",
        "Martabak manis", "Nasi goreng", "Ayam geprek", "Jajan pasar"
    )
    override val demoTransportNotes = listOf(
        "Bensin", "Ojek online", "Tarif angkot", "Parkir", "Servis motor",
        "Tol & bensin", "Tiket KRL", "Tambal ban"
    )
    override val demoShoppingNotes = listOf(
        "Belanja bulanan", "Skincare", "Baju baru", "Alat tulis",
        "Belanja online", "Sepatu", "Tas kerja"
    )
    override val demoBillNotes = listOf(
        "Listrik & air", "Paket data", "Langganan streaming", "Cicilan motor",
        "Iuran kebersihan", "WiFi rumah"
    )
    override val demoFunNotes = listOf(
        "Nonton bioskop", "Kafe bareng teman", "Konser mini", "Top up game",
        "Karaoke", "Jalan-jalan akhir pekan"
    )
    override val demoHealthNotes = listOf("Vitamin", "Obat batuk", "Cek kesehatan", "Masker & sanitizer")
    override val demoEducationNotes = listOf("Buku bacaan", "Kursus online", "Seminar", "Alat tulis kuliah")
    override val demoHomeNotes = listOf("Perbaikan lampu", "Peralatan dapur", "Sabun & pembersih", "Gas elpiji")
    override val demoOtherNotes = listOf("Donasi", "Kado ulang tahun", "Biaya admin", "Dana tak terduga")
}

// ---------------------------------------------------------------------------
// English
// ---------------------------------------------------------------------------

object EnStrings : Strings(AppLanguage.ENGLISH) {

    override val thousandSuffix = "K"
    override val millionSuffix = "M"
    override val billionSuffix = "B"

    override val todayLabel = "Today"
    override val yesterdayLabel = "Yesterday"
    override val greetingMorning = "Good morning"
    override val greetingNoon = "Good afternoon"
    override val greetingEvening = "Good evening"
    override val greetingNight = "Good evening"

    override val appName = "PAKU"
    override val appTagline = "Your personal money tracker"
    override val developerName = "Sigit Pramono"
    override val developerRole = "Developer"
    override val defaultUserName = "PAKU Friend"
    override val logoDescription = "PAKU logo"

    override val income = "Income"
    override val expense = "Expenses"
    override val incomeShort = "In"
    override val expenseShort = "Out"
    override val both = "Both"

    override val category = "Category"
    override val date = "Date"
    override val note = "Note"
    override val all = "All"
    override val thisMonth = "This month"

    override val cancel = "Cancel"
    override val save = "Save"
    override val delete = "Delete"
    override val close = "Close"
    override val select = "Select"
    override val change = "Change"
    override val detail = "Details"
    override val record = "Record"

    override fun categoryName(categoryId: String): String = when (categoryId) {
        "salary" -> "Salary"
        "bonus" -> "Bonus"
        "business" -> "Business"
        "investment" -> "Investment"
        "gift" -> "Gift"
        "other_income" -> "Other"
        "food" -> "Food & Drink"
        "transport" -> "Transport"
        "shopping" -> "Shopping"
        "bills" -> "Bills"
        "entertainment" -> "Entertainment"
        "health" -> "Health"
        "education" -> "Education"
        "home" -> "Home"
        "other_expense" -> "Other"
        else -> categoryId
    }

    override val splashTagline = "Manage your money with a calmer mind"
    override val onboardingTitle = "Welcome to PAKU"
    override val onboardingBody =
        "Track your cash flow, understand where your money goes, and watch it grow through interactive charts."
    override val onboardingNameQuestion = "What should we call you?"
    override val onboardingStartQuestion = "How would you like to start?"
    override val onboardingDemoTitle = "Try sample data"
    override val onboardingDemoSubtitle = "See the charts and stats right away"
    override val onboardingDemoBadge = "Recommended"
    override val onboardingEmptyTitle = "Start from scratch"
    override val onboardingEmptySubtitle = "Record your own transactions from the beginning"
    override val onboardingCta = "Get Started"
    override val onboardingPrivacy = "🔒  Your data stays on this device · no internet"

    override val tabHome = "Home"
    override val tabStats = "Statistics"
    override val tabTransactions = "Transactions"
    override val tabSettings = "Settings"
    override val addTransaction = "Add transaction"

    override val balanceNow = "CURRENT BALANCE"
    override val monthSummaryTitle = "Month summary"
    override val monthSummaryCurrent = "Current month"
    override val monthSummarySelected = "Selected month archive"
    override val backToThisMonth = "Back to this month"
    override val expenseByCategory = "Expenses by category"
    override val totalExpense = "Total expenses"
    override val noExpenseTitle = "No expenses yet"
    override val noExpenseBody = "Record an expense this month and the chart fills in instantly."
    override val recordExpense = "Record expense"
    override val sixMonthCashflow = "6-month cash flow"
    override val tapBarToSeeMonth = "Tap a bar to see that month"
    override val homeChartEmptyTitle = "Chart is still empty"
    override val homeChartEmptyBody = "Add transactions to compare your cash flow."
    override val recentTransactions = "Recent transactions"
    override val seeAll = "See all"
    override val homeEmptyTitle = "Start tracking your cash flow"
    override val homeEmptyBody = "Every small record helps you understand your money habits."
    override val recordNow = "Record now"

    override fun incomeThisMonth(amount: String) = "Income this month $amount"
    override fun recordsSaved(count: Int) = "$count records saved"

    override val statsTitle = "Statistics"
    override val statsSubtitle = "See where your money goes"
    override val period = "Period"
    override val periodHint = "Choose the month you want to analyse"
    override val sixMonthTrend = "6-month trend"
    override val tapBarToSwitchMonth = "Tap a bar to switch month"
    override val trendEmptyTitle = "No cash flow yet"
    override val trendEmptyBody = "Add transactions to see the last six months."
    override val categoryBreakdown = "Category breakdown"
    override val tapCategoryHint = "Tap a category to see its transactions"
    override val noCategoryData = "No category data for this month yet."
    override val seeAllTransactions = "See all transactions"
    override val statsTip =
        "Compare several months to spot your spending pattern. Consistent data makes decisions easier."
    override val insightTitle = "This month's insight"
    override val insightNoComparison = "Nothing to compare with last month yet."

    override fun composition(type: TxType) = "${typeLabel(type)} composition"
    override fun totalOf(type: TxType) = "Total ${typeLabel(type).lowercase()}"
    override fun noDataType(type: TxType) = "No ${typeLabel(type).lowercase()} yet"
    override val noDataTypeBody =
        "This month is still empty. Add transactions to see the composition."
    override fun insightNoData(type: TxType) =
        "No ${typeLabel(type).lowercase()} to analyse yet. Start by recording your first transaction."

    override fun insightTopExpense(categoryName: String, percent: String) =
        "$categoryName is your biggest expense ($percent of the total)."

    override fun insightTopIncome(categoryName: String, percent: String) =
        "Your biggest income source is $categoryName ($percent of the total)."

    override fun insightUp(percent: String) = "Up $percent compared to last month."
    override fun insightDown(percent: String) = "Down $percent compared to last month."
    override fun insightDaily(changeText: String, average: String, days: Int) =
        "$changeText On average $average per active day ($days days)."

    override fun transactionCount(count: Int) = if (count == 1) "1 transaction" else "$count transactions"
    override fun monthTransactionSubtitle(month: String, count: Int) =
        if (count == 1) "$month · 1 transaction" else "$month · $count transactions"

    override val transactionsTitle = "Transactions"
    override val transactionsSubtitle = "All of your cash flow records"
    override val noRecordsTitle = "No records yet"
    override val noRecordsBody = "Start by recording your first income and expense today."
    override val noResultsTitle = "No results"
    override val noResultsBody = "Try another keyword or change the selected filter."
    override val searchHint = "Search notes or categories..."
    override val clearSearch = "Clear search"
    override val clearCategoryFilter = "Clear category filter"
    override val resetFilter = "Reset filter"

    override fun recordsCount(count: Int) = if (count == 1) "1 record" else "$count records"

    override val formTitleAdd = "Add Transaction"
    override val formTitleEdit = "Edit Transaction"
    override val deleteTransaction = "Delete transaction"
    override val amount = "Amount"
    override val quickAdd10 = "+10K"
    override val quickAdd50 = "+50K"
    override val quickAdd100 = "+100K"
    override val clearDigit = "Clear digit"
    override val noteOptional = "Note (optional)"
    override val notePlaceholder = "e.g. Lunch"
    override val saveTransaction = "Save Transaction"
    override val saveChanges = "Save Changes"
    override val pickDate = "Pick a date"
    override val deleteConfirmTitle = "Delete transaction?"
    override val deleteConfirmBody = "This record will be removed from your financial history."

    override val settingsTitle = "Settings"
    override val settingsSubtitle = "Make PAKU fit your style"
    override val nicknameLabel = "Your nickname"
    override val nicknameDialogTitle = "Nickname"
    override val nicknameDialogBody = "This name shows up on the PAKU home screen."
    override val recordsStatLabel = "Records"
    override val appearance = "Appearance"
    override val appearanceHint = "Pick the theme that feels most comfortable"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val languageLabel = "Language"
    override val languageHint = "Choose the app display language"
    override val languageSystem = "System"
    override val languageIndonesian = "Indonesian"
    override val languageEnglish = "English"
    override val dataBackup = "Data & backup"
    override val dataBackupHint = "Everything is stored locally on your device"
    override val exportCsv = "Export data (CSV)"
    override val exportEmpty = "No transactions to export yet"
    override val exportSuccess = "Data exported to CSV"
    override val exportFailed = "Failed to export data"
    override val exportCannotOpen = "Cannot open the file"
    override val loadSample = "Load sample data"
    override val loadSampleHint = "Fill the charts with sample transactions"
    override val sampleLoaded = "Sample data loaded"
    override val deleteAllData = "Delete all data"
    override val deleteAllDataHint = "Permanently remove every record"
    override val deleteAllConfirmTitle = "Delete all data?"
    override val deleteAllConfirmBody =
        "Every transaction will be permanently deleted. This cannot be undone."
    override val deleteAllConfirmAction = "Delete all"
    override val deleteAllDone = "All data has been deleted"
    override val replaceSampleTitle = "Replace with sample data?"
    override val replaceSampleBody =
        "Your current data will be replaced with sample transactions from the last six months."
    override val aboutTitle = "About"
    override val aboutBody =
        "PAKU helps you record your cash flow, understand your spending habits, and watch your finances grow through charts that are easy to read."
    override val aboutFooter = "No internet. No account. Your privacy stays yours."
    override val madeWith = "Made with ❤️ for tidier finances."
    override val csvHeader = "Date;Type;Category;Note;Amount"

    override fun trackingSince(month: String) = "Tracking since $month"
    override fun exportReady(count: Int) = "$count transactions ready to export"

    override val tapSliceHint = "Tap a slice to see details"
    override val previousMonth = "Previous month"
    override val nextMonth = "Next month"

    override val transactionDeleted = "Transaction deleted"
    override val undo = "Undo"
    override val changesSaved = "Changes saved"
    override val transactionSaved = "Transaction saved"

    override val demoSalaryNote = "Monthly salary"
    override val demoBonusNote = "Project bonus"
    override val demoBusinessNote = "Online shop sales"
    override val demoInvestmentNote = "Mutual fund dividend"
    override val demoFoodNotes = listOf(
        "Lunch", "Breakfast porridge", "Coffee & snacks", "Dinner",
        "Food delivery", "Meatball soup", "Padang rice", "Spicy stew",
        "Sweet martabak", "Fried rice", "Fried chicken", "Market treats"
    )
    override val demoTransportNotes = listOf(
        "Fuel", "Ride hailing", "Public minivan", "Parking", "Motorbike service",
        "Toll & fuel", "Commuter train", "Tire patch"
    )
    override val demoShoppingNotes = listOf(
        "Monthly groceries", "Skincare", "New clothes", "Stationery",
        "Online shopping", "Shoes", "Work bag"
    )
    override val demoBillNotes = listOf(
        "Electricity & water", "Mobile data plan", "Streaming subscription", "Motorbike instalment",
        "Garbage fee", "Home WiFi"
    )
    override val demoFunNotes = listOf(
        "Cinema tickets", "Cafe with friends", "Mini concert", "Game top-up",
        "Karaoke", "Weekend trip"
    )
    override val demoHealthNotes = listOf("Vitamins", "Cough medicine", "Health check-up", "Masks & sanitizer")
    override val demoEducationNotes = listOf("Reading book", "Online course", "Seminar", "College stationery")
    override val demoHomeNotes = listOf("Light repair", "Kitchen tools", "Soap & cleaner", "LPG canister")
    override val demoOtherNotes = listOf("Donation", "Birthday gift", "Admin fee", "Unforeseen expense")
}
