@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.paku.keuangan.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paku.keuangan.data.Categories
import com.paku.keuangan.data.Category
import com.paku.keuangan.data.Transaction
import com.paku.keuangan.data.TxType
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.ui.theme.expense
import com.paku.keuangan.ui.theme.income
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@Composable
fun TransactionFormScreen(
    initial: Transaction?,
    onClose: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val today = LocalDate.now()
    var type by rememberSaveable { mutableStateOf(initial?.type ?: TxType.EXPENSE) }
    var amountDigits by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.amount?.toString() ?: "")
    }
    var categoryId by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.categoryId ?: Categories.of(initial?.type ?: TxType.EXPENSE).first().id)
    }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note ?: "") }
    var dateEpochDay by rememberSaveable(initial?.id) {
        mutableStateOf((initial?.date ?: today).toEpochDay())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val categories = Categories.of(type)
    val category: Category = categories.firstOrNull { it.id == categoryId } ?: categories.first()
    val amount = amountDigits.toLongOrNull() ?: 0L
    val date = LocalDate.ofEpochDay(dateEpochDay)
    val typeColor = if (type == TxType.INCOME) MaterialTheme.colorScheme.income
    else MaterialTheme.colorScheme.expense
    val focusManager = LocalFocusManager.current

    BackHandler(onBack = onClose)

    fun appendDigits(newDigits: String) {
        val joined = (amountDigits + newDigits).trimStart('0')
        amountDigits = if (joined.length > 12) amountDigits else joined
    }

    fun addAmount(delta: Long) {
        val next = (amount + delta).coerceAtMost(999_999_999_999L)
        amountDigits = next.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onClose,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PakuIcons.Close,
                        contentDescription = s.close,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Text(
                text = if (initial == null) s.formTitleAdd else s.formTitleEdit,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (initial != null) {
                Surface(
                    onClick = { showDeleteDialog = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                ) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PakuIcons.Delete,
                            contentDescription = s.deleteTransaction,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            } else {
                Spacer(Modifier.size(42.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(2.dp))

            TypeSelector(type = type, onSelect = { newType ->
                if (newType != type) {
                    type = newType
                    categoryId = Categories.of(newType).first().id
                }
            })

            AmountCard(
                amount = amount,
                typeColor = typeColor,
                onQuickAdd = { addAmount(it) }
            )

            NumericKeypad(
                onDigit = { appendDigits(it) },
                onBackspace = {
                    amountDigits = if (amountDigits.length <= 1) "" else amountDigits.dropLast(1)
                }
            )

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = s.category,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = category.emoji + " " + s.categoryName(category.id),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(category.color)
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { item ->
                        CategoryChoice(
                            category = item,
                            selected = item.id == category.id,
                            onClick = { categoryId = item.id }
                        )
                    }
                }
            }

            PakuCard(padding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showDatePicker = true }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(icon = PakuIcons.Calendar, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = s.fullDateLabel(date),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = PakuIcons.ChevronRight,
                        contentDescription = s.pickDate,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
                SectionDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    text = s.noteOptional,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (note.isEmpty()) {
                        Text(
                            text = s.notePlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    BasicTextField(
                        value = note,
                        onValueChange = { if (it.length <= 80) note = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (note.isNotEmpty()) {
                    Text(
                        text = "${note.length}/80",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                GradientButton(
                    text = if (initial == null) s.saveTransaction else s.saveChanges,
                    onClick = {
                        onSave(
                            Transaction(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                type = type,
                                amount = amount,
                                categoryId = category.id,
                                note = note.trim(),
                                date = date,
                                createdAt = initial?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    },
                    enabled = amount > 0,
                    icon = PakuIcons.Check
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateEpochDay * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        dateEpochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                    }
                    showDatePicker = false
                }) {
                    Text(s.select)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.cancel)
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }

    if (showDeleteDialog && initial != null) {
        ConfirmDialog(
            title = s.deleteConfirmTitle,
            message = s.deleteConfirmBody,
            confirmLabel = s.delete,
            onConfirm = { onDelete(initial) },
            onDismiss = { showDeleteDialog = false },
            destructive = true
        )
    }
}

@Composable
private fun TypeSelector(
    type: TxType,
    onSelect: (TxType) -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf(TxType.EXPENSE, TxType.INCOME).forEach { option ->
                val selected = option == type
                val color = if (option == TxType.INCOME) MaterialTheme.colorScheme.income
                else MaterialTheme.colorScheme.expense
                Surface(
                    onClick = { onSelect(option) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) color else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (option == TxType.INCOME) PakuIcons.ArrowDown
                            else PakuIcons.ArrowUp,
                            contentDescription = null,
                            tint = if (selected) Color.White else color,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = s.typeLabel(option),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountCard(
    amount: Long,
    typeColor: Color,
    onQuickAdd: (Long) -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = s.amount,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = s.rupiah(amount),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = if (amount == 0L) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                else typeColor,
                maxLines = 1
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10_000L to s.quickAdd10, 50_000L to s.quickAdd50, 100_000L to s.quickAdd100)
                    .forEach { (value, label) ->
                        Surface(
                            onClick = { onQuickAdd(value) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val s = LocalStrings.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    Surface(
                        onClick = {
                            if (key == "⌫") onBackspace() else onDigit(key)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = PakuIcons.Backspace,
                                    contentDescription = s.clearDigit,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChoice(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    val s = LocalStrings.current
    val color = Color(category.color)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) color.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) color.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category.emoji, fontSize = 15.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = s.categoryName(category.id),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
