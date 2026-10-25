package com.paku.keuangan.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paku.keuangan.data.ThemeMode
import com.paku.keuangan.data.Transaction
import com.paku.keuangan.data.TxType
import com.paku.keuangan.data.totalOf
import com.paku.keuangan.i18n.LanguageMode
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.i18n.Strings
import com.paku.keuangan.ui.theme.Coral
import com.paku.keuangan.ui.theme.CoralDeep
import com.paku.keuangan.ui.theme.expense
import com.paku.keuangan.ui.theme.income
import java.time.LocalDate
import java.time.YearMonth

private enum class ConfirmAction { CLEAR_ALL, LOAD_DEMO }

@Composable
fun SettingsScreen(
    state: PakuUiState,
    onUpdateName: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (LanguageMode) -> Unit,
    onLoadDemo: () -> Unit,
    onClearAll: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    var showNameDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }
    val transactions = state.transactions
    val totalIncome = remember(transactions) { transactions.totalOf(TxType.INCOME) }
    val totalExpense = remember(transactions) { transactions.totalOf(TxType.EXPENSE) }
    val firstDate = remember(transactions) { transactions.minOfOrNull { it.date } }


    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val result = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(buildCsv(transactions, s).toByteArray(Charsets.UTF_8))
                } ?: error(s.exportCannotOpen)
            }
            onMessage(
                if (result.isSuccess) s.exportSuccess
                else s.exportFailed
            )
        }
    }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.settingsTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = s.settingsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconBubble(
                    icon = PakuIcons.Settings,
                    tint = MaterialTheme.colorScheme.secondary,
                    size = 48
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Coral, CoralDeep))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.settings.userName.firstOrNull()?.uppercase()
                                    ?: s.defaultUserName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.settings.userName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = s.nicknameLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = { showNameDialog = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PakuIcons.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = s.change,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    SectionDivider()
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ProfileStat(
                            label = s.recordsStatLabel,
                            value = transactions.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStat(
                            label = s.income,
                            value = s.compact(totalIncome),
                            color = MaterialTheme.colorScheme.income,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStat(
                            label = s.expense,
                            value = s.compact(totalExpense),
                            color = MaterialTheme.colorScheme.expense,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (firstDate != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = s.trackingSince(s.monthLabel(YearMonth.from(firstDate))),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            PakuCard {
                Text(
                    text = s.appearance,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.appearanceHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SegmentedToggle(
                    options = ThemeMode.entries.toList(),
                    selected = state.settings.themeMode,
                    label = { mode ->
                        when (mode) {
                            ThemeMode.SYSTEM -> s.themeSystem
                            ThemeMode.LIGHT -> s.themeLight
                            ThemeMode.DARK -> s.themeDark
                        }
                    },
                    onSelect = onThemeChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            PakuCard {
                Text(
                    text = s.languageLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.languageHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SegmentedToggle(
                    options = LanguageMode.entries.toList(),
                    selected = state.settings.languageMode,
                    label = { mode ->
                        when (mode) {
                            LanguageMode.SYSTEM -> s.languageSystem
                            LanguageMode.INDONESIAN -> s.languageIndonesian
                            LanguageMode.ENGLISH -> s.languageEnglish
                        }
                    },
                    onSelect = onLanguageChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            PakuCard {
                Text(
                    text = s.dataBackup,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.dataBackupHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                SettingsRow(
                    icon = PakuIcons.Download,
                    title = s.exportCsv,
                    subtitle = if (transactions.isEmpty()) s.exportEmpty
                    else s.exportReady(transactions.size),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        if (transactions.isEmpty()) {
                            onMessage(s.exportEmpty)
                        } else {
                            val name = "paku-transaksi-${LocalDate.now()}.csv"
                            exportLauncher.launch(name)
                        }
                    }
                )
                SectionDivider(modifier = Modifier.padding(vertical = 2.dp))
                SettingsRow(
                    icon = PakuIcons.Refresh,
                    title = s.loadSample,
                    subtitle = s.loadSampleHint,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onClick = {
                        if (transactions.isEmpty()) {
                            onLoadDemo()
                            onMessage(s.sampleLoaded)
                        } else {
                            confirmAction = ConfirmAction.LOAD_DEMO
                        }
                    }
                )
                SectionDivider(modifier = Modifier.padding(vertical = 2.dp))
                SettingsRow(
                    icon = PakuIcons.Delete,
                    title = s.deleteAllData,
                    subtitle = s.deleteAllDataHint,
                    iconTint = MaterialTheme.colorScheme.error,
                    trailing = {
                        Icon(
                            imageVector = PakuIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = { confirmAction = ConfirmAction.CLEAR_ALL }
                )
            }
        }

        item {
            PakuCard {
                Text(
                    text = s.aboutTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoMark(size = 50)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.appName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = s.appTagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MetaPill(text = "v1.0.0")
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = s.aboutBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(
                        icon = PakuIcons.Info,
                        tint = MaterialTheme.colorScheme.secondary,
                        size = 36
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.developerRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = s.developerName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = s.aboutFooter,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = s.madeWith,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showNameDialog) {
        var draft by remember { mutableStateOf(state.settings.userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(s.nicknameDialogTitle, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(
                        text = s.nicknameDialogBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 24) draft = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text(s.defaultUserName) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateName(draft)
                        showNameDialog = false
                    }
                ) { Text(s.save) }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text(s.cancel) }
            },
            shape = RoundedCornerShape(26.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    when (confirmAction) {
        ConfirmAction.CLEAR_ALL -> ConfirmDialog(
            title = s.deleteAllConfirmTitle,
            message = s.deleteAllConfirmBody,
            confirmLabel = s.deleteAllConfirmAction,
            onConfirm = {
                onClearAll()
                onMessage(s.deleteAllDone)
            },
            onDismiss = { confirmAction = null },
            destructive = true
        )

        ConfirmAction.LOAD_DEMO -> ConfirmDialog(
            title = s.replaceSampleTitle,
            message = s.replaceSampleBody,
            confirmLabel = s.loadSample,
            onConfirm = {
                onLoadDemo()
                onMessage(s.sampleLoaded)
            },
            onDismiss = { confirmAction = null }
        )

        null -> Unit
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            maxLines = 1
        )
    }
}

private fun buildCsv(transactions: List<Transaction>, strings: Strings): String {
    val header = strings.csvHeader
    val rows = transactions.sortedByDescending { it.date }.map { tx ->
        listOf(
            tx.date.toString(),
            strings.typeLabel(tx.type),
            strings.categoryName(tx.categoryId),
            tx.note.ifBlank { "-" },
            tx.amount.toString()
        ).joinToString(";") { field ->
            if (field.contains(';') || field.contains('"') || field.contains('\n')) {
                "\"" + field.replace("\"", "\"\"") + "\""
            } else {
                field
            }
        }
    }
    return "\uFEFF" + (listOf(header) + rows).joinToString("\r\n")
}
