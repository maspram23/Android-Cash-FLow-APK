package com.paku.keuangan.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paku.keuangan.data.MonthFlow
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.ui.theme.PakuFont
import com.paku.keuangan.ui.theme.expense
import com.paku.keuangan.ui.theme.income
import java.time.YearMonth
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

data class ChartSlice(
    val key: String,
    val label: String,
    val value: Long,
    val color: Color,
    val emoji: String = ""
)

/**
 * Diagram donat interaktif.
 * Ketuk salah satu potongan (atau legenda) untuk menyorot kategori.
 */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier,
    centerCaption: String? = null,
    ringWidth: Dp = 24.dp,
    showHint: Boolean = true
) {
    val s = LocalStrings.current
    val total = slices.sumOf { it.value }.coerceAtLeast(1L)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(slices, selectedKey) {
                        detectTapGestures { tap ->
                            val outer = min(size.width, size.height) / 2f
                            val inner = outer - ringWidth.toPx() * 1.8f
                            val dx = tap.x - size.width / 2f
                            val dy = tap.y - size.height / 2f
                            val distance = hypot(dx, dy)
                            if (distance in inner..outer) {
                                val angle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 450.0) % 360.0
                                var start = 0.0
                                val hit = slices.firstOrNull { slice ->
                                    val sweep = 360.0 * slice.value / total
                                    val match = angle >= start && angle < start + sweep
                                    start += sweep
                                    match
                                }
                                onSelect(if (hit?.key == selectedKey) null else hit?.key)
                            }
                        }
                    }
            ) {
                val strokePx = ringWidth.toPx()
                val inset = strokePx / 2f + 2.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = trackColor.copy(alpha = 0.7f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                if (slices.isEmpty()) return@Canvas
                var startAngle = -90f
                slices.forEach { slice ->
                    val fullSweep = 360f * slice.value / total
                    val gap = if (slices.size > 1) min(2.6f, fullSweep * 0.2f) else 0f
                    val sweep = (fullSweep - gap) * progress.value
                    val isSelected = slice.key == selectedKey
                    val dimmed = selectedKey != null && !isSelected
                    if (sweep > 0.2f) {
                        if (isSelected) {
                            drawArc(
                                color = slice.color.copy(alpha = 0.22f),
                                startAngle = startAngle + gap / 2f - 1.5f,
                                sweepAngle = sweep + 3f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = strokePx + 9.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                        drawArc(
                            color = slice.color.copy(alpha = if (dimmed) 0.38f else 1f),
                            startAngle = startAngle + gap / 2f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += fullSweep * progress.value
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 38.dp)
            ) {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = centerValue,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                if (centerCaption != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = centerCaption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showHint) {
            Text(
                text = s.tapSliceHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutLegend(
    slices: List<ChartSlice>,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val total = slices.sumOf { it.value }.coerceAtLeast(1L)
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        slices.forEach { slice ->
            val selected = slice.key == selectedKey
            Surface(
                onClick = { onSelect(if (selected) null else slice.key) },
                shape = CircleShape,
                color = if (selected) slice.color.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = if (selected) BorderStroke(1.dp, slice.color.copy(alpha = 0.45f)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = s.percent(slice.value.toFloat() / total),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Diagram batang arus kas. Ketuk satu bulan untuk memilihnya.
 */
@Composable
fun CashFlowBarChart(
    flows: List<MonthFlow>,
    metric: BarMetric,
    selectedMonth: YearMonth,
    onSelectMonth: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 190.dp
) {
    val s = LocalStrings.current
    val incomeColor = MaterialTheme.colorScheme.income
    val expenseColor = MaterialTheme.colorScheme.expense
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedLabelColor = MaterialTheme.colorScheme.onSurface
    val highlight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val textMeasurer = rememberTextMeasurer()
    val monthLabels = remember(flows, s) { flows.map { s.shortMonthLabel(it.month) } }

    val maxValue = remember(flows, metric) {
        val values = when (metric) {
            BarMetric.BOTH -> flows.flatMap { listOf(it.income, it.expense) }
            BarMetric.INCOME -> flows.map { it.income }
            BarMetric.EXPENSE -> flows.map { it.expense }
        }
        values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(flows, metric) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(flows) {
                    detectTapGestures { tap ->
                        if (flows.isEmpty()) return@detectTapGestures
                        val index = (tap.x / size.width * flows.size)
                            .toInt()
                            .coerceIn(0, flows.lastIndex)
                        onSelectMonth(flows[index].month)
                    }
                }
        ) {
            if (flows.isEmpty()) return@Canvas
            val labelHeight = 24.dp.toPx()
            val topPadding = 10.dp.toPx()
            val chartHeight = size.height - labelHeight - topPadding
            val baseline = topPadding + chartHeight
            val groupWidth = size.width / flows.size

            // Garis bantu horizontal
            listOf(0f, 0.33f, 0.66f, 1f).forEach { fraction ->
                val y = baseline - chartHeight * fraction
                drawLine(
                    color = gridColor.copy(alpha = if (fraction == 0f) 0.9f else 0.55f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = if (fraction == 0f) null
                    else PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx()))
                )
            }

            val barGap = 3.5.dp.toPx()
            val barWidth = when (metric) {
                BarMetric.BOTH -> groupWidth * 0.26f
                else -> groupWidth * 0.34f
            }
            val radius = CornerRadius(barWidth / 2f, barWidth / 2f)

            flows.forEachIndexed { index, flow ->
                val groupStart = index * groupWidth
                val isSelected = flow.month == selectedMonth
                if (isSelected) {
                    drawRoundRect(
                        color = highlight,
                        topLeft = Offset(groupStart + 2.dp.toPx(), topPadding - 2.dp.toPx()),
                        size = Size(groupWidth - 4.dp.toPx(), chartHeight + 4.dp.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }

                val center = groupStart + groupWidth / 2f
                when (metric) {
                    BarMetric.BOTH -> {
                        val incomeHeight = (flow.income.toFloat() / maxValue) * chartHeight * progress.value
                        val expenseHeight = (flow.expense.toFloat() / maxValue) * chartHeight * progress.value
                        val incomeX = center - barGap / 2f - barWidth
                        val expenseX = center + barGap / 2f
                        if (incomeHeight > 0f) {
                            drawRoundRect(
                                color = incomeColor.copy(alpha = if (isSelected) 1f else 0.82f),
                                topLeft = Offset(incomeX, baseline - incomeHeight),
                                size = Size(barWidth, incomeHeight),
                                cornerRadius = radius
                            )
                        }
                        if (expenseHeight > 0f) {
                            drawRoundRect(
                                color = expenseColor.copy(alpha = if (isSelected) 1f else 0.82f),
                                topLeft = Offset(expenseX, baseline - expenseHeight),
                                size = Size(barWidth, expenseHeight),
                                cornerRadius = radius
                            )
                        }
                    }
                    BarMetric.INCOME, BarMetric.EXPENSE -> {
                        val value = if (metric == BarMetric.INCOME) flow.income else flow.expense
                        val barHeight = (value.toFloat() / maxValue) * chartHeight * progress.value
                        val color = if (metric == BarMetric.INCOME) incomeColor else expenseColor
                        if (barHeight > 0f) {
                            drawRoundRect(
                                color = color.copy(alpha = if (isSelected) 1f else 0.8f),
                                topLeft = Offset(center - barWidth / 2f, baseline - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = radius
                            )
                        }
                    }
                }

                // Label bulan
                val style = TextStyle(
                    fontFamily = PakuFont,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) selectedLabelColor else labelColor
                )
                val layout = textMeasurer.measure(monthLabels[index], style)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        center - layout.size.width / 2f,
                        baseline + 7.dp.toPx()
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedFlow = flows.firstOrNull { it.month == selectedMonth }
            Text(
                text = selectedFlow?.let { s.monthLabel(it.month) } ?: s.monthLabel(selectedMonth),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (selectedFlow != null) {
                when (metric) {
                    BarMetric.BOTH -> {
                        ChartLegendValue(s.incomeShort, selectedFlow.income, incomeColor)
                        Spacer(Modifier.width(10.dp))
                        ChartLegendValue(s.expenseShort, selectedFlow.expense, expenseColor)
                    }
                    BarMetric.INCOME -> ChartLegendValue(s.incomeShort, selectedFlow.income, incomeColor)
                    BarMetric.EXPENSE -> ChartLegendValue(s.expenseShort, selectedFlow.expense, expenseColor)
                }
            }
        }
    }
}

@Composable
private fun ChartLegendValue(label: String, value: Long, color: Color) {
    val s = LocalStrings.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$label ${s.compact(value)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChartLegendDot(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
