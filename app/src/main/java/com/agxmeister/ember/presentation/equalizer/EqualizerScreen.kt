package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.common.IntWheelPicker
import com.agxmeister.ember.presentation.home.WeightWheelPicker
import com.agxmeister.ember.presentation.theme.closenessColor
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen() {
    val viewModel: EqualizerViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    if (state.days.isEmpty()) return

    val isFocused = state.selectedDate != null
    val displayDate = state.selectedDate ?: state.today
    val todayWeight = state.days.find { it.date == state.today }?.weightKg
    val readoutWeight = if (isFocused) state.days.find { it.date == displayDate }?.weightKg else state.weeklyAvg
    val wkPrefix = appString(R.string.trends_readout_wk_prefix)
    val readoutLabel = state.selectedDate?.let {
        if (state.isWeekly) {
            "$wkPrefix ${it.month.name.take(3)} ${it.dayOfMonth.toString().padStart(2, '0')} ${it.year}"
        } else {
            "${it.dayOfWeek.name.take(3)} ${it.month.name.take(3)} ${it.dayOfMonth.toString().padStart(2, '0')} ${it.year}"
        }
    } ?: if (state.isWeekly) appString(R.string.trends_readout_week_avg) else appString(R.string.trends_readout_day_avg)
    val readoutCloseness = readoutWeight?.let { w ->
        (1.0 - abs(w - state.targetKg) / state.tolerance).coerceIn(0.0, 1.0).toFloat()
    } ?: 0f
    val readoutColor = closenessColor(readoutCloseness)
    val score = state.weeklyAvg?.let { w ->
        val c = (1.0 - abs(w - state.targetKg) / state.tolerance).coerceIn(0.0, 1.0)
        (c * 100).roundToInt()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ReadoutBlock(
            displayWeight = readoutWeight,
            label = readoutLabel,
            targetKg = state.targetKg,
            displayColor = readoutColor,
            weightUnit = state.weightUnit,
            isFocused = isFocused,
            onTap = { viewModel.openEdit(displayDate) },
        )
        Spacer(modifier = Modifier.height(12.dp))
        EqualizerCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            days = state.days,
            targetKg = state.targetKg,
            tolerance = state.tolerance,
            weightUnit = state.weightUnit,
            today = state.today,
            selectedDate = state.selectedDate,
            isWeekly = state.isWeekly,
            trendLine = state.trendLine,
            onDayToggle = viewModel::toggleDay,
        )
        Spacer(modifier = Modifier.height(6.dp))
        ContextStrip(state.selectedDate, state.isWeekly)
        Spacer(modifier = Modifier.height(12.dp))
        StatsRow(
            streak = state.streak,
            todayWeight = todayWeight,
            weeklyTrend = state.weeklyTrend,
            trendCloserToTarget = state.trendCloserToTarget,
            score = score,
            weightUnit = state.weightUnit,
            isWeekly = state.isWeekly,
        )
    }

    editState?.let { es ->
        val editCloseness = (1.0 - abs(es.defaultWeightKg - state.targetKg) / state.tolerance)
            .coerceIn(0.0, 1.0).toFloat()
        val accentColor = closenessColor(editCloseness)

        ModalBottomSheet(
            onDismissRequest = viewModel::closeEdit,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            EqualizerEditDrawer(
                editState = es,
                accentColor = accentColor,
                onSave = viewModel::saveMeasurement,
                onDelete = viewModel::deleteMeasurements,
            )
        }
    }
}


@Composable
private fun ReadoutBlock(
    displayWeight: Double?,
    label: String,
    targetKg: Double,
    displayColor: Color,
    weightUnit: WeightUnit,
    isFocused: Boolean,
    onTap: () -> Unit,
) {
    val status = when {
        displayWeight == null -> ""
        displayWeight > targetKg -> "ABOVE"
        displayWeight < targetKg -> "BELOW"
        else -> "ON TARGET"
    }
    val weightStr = displayWeight?.let { "%.1f".format(weightUnit.fromKg(it)) } ?: "−.−"
    val deltaKg = displayWeight?.let { it - targetKg }
    val deltaDisplay = deltaKg?.let { weightUnit.scaleDiff(it) }
    val deltaStr = when {
        deltaDisplay == null -> "−"
        deltaDisplay > 0.005 -> "+%.1f".format(deltaDisplay)
        deltaDisplay < -0.005 -> "−%.1f".format(abs(deltaDisplay))
        else -> "±0.0"
    }

    val onBg = MaterialTheme.colorScheme.onBackground
    val glow = Shadow(color = displayColor.copy(alpha = 0.65f), blurRadius = 22f)
    val dimColor = onBg.copy(alpha = 0.45f)
    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = dimColor,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFocused) Modifier.clickable(onClick = onTap) else Modifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = labelStyle,
                modifier = Modifier.weight(1f),
            )
            Text(text = appString(R.string.trends_delta_target), style = labelStyle)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = weightStr,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = displayColor,
                        shadow = glow,
                    ),
                )
                Text(
                    text = " ${weightUnit.label}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = displayColor.copy(alpha = 0.85f),
                        shadow = Shadow(color = displayColor.copy(alpha = 0.5f), blurRadius = 12f),
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Text(
                text = deltaStr,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = displayColor,
                    shadow = glow,
                ),
            )
        }
        Text(
            text = appString(R.string.trends_tap_to_edit),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = if (isFocused) onBg.copy(alpha = 0.50f) else Color.Transparent,
            ),
        )
    }
}

@Composable
private fun EqualizerCard(
    modifier: Modifier = Modifier,
    days: List<EqualizerDayData>,
    targetKg: Double,
    tolerance: Double,
    weightUnit: WeightUnit,
    today: LocalDate,
    selectedDate: LocalDate?,
    isWeekly: Boolean,
    trendLine: TrendLineData?,
    onDayToggle: (LocalDate) -> Unit,
) {
    val weights = days.mapNotNull { it.weightKg }
    val allValues = if (weights.isEmpty()) listOf(targetKg - 2.0, targetKg + 2.0) else weights + targetKg
    val minW = allValues.min()
    val maxW = allValues.max()
    val span = (maxW - minW).coerceAtLeast(4.0)
    val pad = (span * 0.18).coerceAtLeast(1.5)
    val yMin = floor((minW - pad) * 2) / 2
    val yMax = ceil((maxW + pad) * 2) / 2

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val tgtPrefix = appString(R.string.trends_tgt_prefix)
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val onCard = MaterialTheme.colorScheme.onSurface
    val dimColor = onCard.copy(alpha = 0.18f)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(days) {
                    detectTapGestures { offset ->
                        val leftPad = with(density) { 8.dp.toPx() }
                        val rightPad = with(density) { 8.dp.toPx() }
                        val innerWidth = size.width.toFloat() - leftPad - rightPad
                        val colWidth = innerWidth / 14f
                        val colIdx = ((offset.x - leftPad) / colWidth).toInt()
                        if (colIdx in 0..13) onDayToggle(days[colIdx].date)
                    }
                },
        ) {
            val leftPad = 8.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 20.dp.toPx()
            val bottomPad = 28.dp.toPx()

            val innerWidth = size.width - leftPad - rightPad
            val innerHeight = size.height - topPad - bottomPad
            val colWidth = innerWidth / 14f

            val dashUnit = innerHeight / 32f
            val dashH = dashUnit * 0.60f
            val maxDashW = (colWidth * 0.72f).coerceAtMost(10.dp.toPx())

            val dashAreaBottom = size.height - bottomPad
            val dashAreaTop = dashAreaBottom - innerHeight

            val targetFrac = ((targetKg - yMin) / (yMax - yMin)).coerceIn(0.0, 1.0).toFloat()
            val targetY = dashAreaBottom - targetFrac * innerHeight
            val tgtLabelText = "$tgtPrefix %.1f".format(weightUnit.fromKg(targetKg))
            val tgtLayout = textMeasurer.measure(
                tgtLabelText,
                TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = onCard.copy(alpha = 0.60f)),
            )
            val tgtLabelW = tgtLayout.size.width.toFloat() + 8.dp.toPx()
            val lineEndX = size.width - rightPad - tgtLabelW

            days.forEachIndexed { idx, day ->
                val colLeft = leftPad + idx * colWidth
                val colCenterX = colLeft + colWidth / 2f
                val isToday = day.date == today
                val isSelected = day.date == selectedDate
                val hasData = day.weightKg != null

                val c = if (hasData) {
                    (1.0 - abs(day.weightKg!! - targetKg) / tolerance).coerceIn(0.0, 1.0).toFloat()
                } else 0f
                val litColor = closenessColor(c)

                val litCount = if (hasData) {
                    val frac = (day.weightKg!! - yMin) / (yMax - yMin)
                    (frac * 32).roundToInt().coerceIn(1, 32)
                } else 0

                if (isSelected && !isToday) {
                    drawRect(
                        color = onCard.copy(alpha = 0.04f),
                        topLeft = Offset(colLeft + 1.dp.toPx(), dashAreaTop - 4.dp.toPx()),
                        size = Size(colWidth - 2.dp.toPx(), innerHeight + 8.dp.toPx()),
                    )
                }

                val dashW = maxDashW
                val dashX = colLeft + (colWidth - dashW) / 2f

                for (di in 0 until 32) {
                    val isLit = di < litCount
                    val isTip = isLit && di == litCount - 1 && (isToday || isSelected)
                    val yBottom = dashAreaBottom - di * dashUnit
                    val yTop = yBottom - dashH

                    if (isLit) {
                        val w = if (isTip) dashW * 1.18f else dashW
                        val x = if (isTip) dashX - (w - dashW) / 2f else dashX
                        drawRect(color = litColor.copy(alpha = 0.14f), topLeft = Offset(x - 3f, yTop - 2f), size = Size(w + 6f, dashH + 4f))
                        drawRect(color = litColor.copy(alpha = 0.28f), topLeft = Offset(x - 1f, yTop - 1f), size = Size(w + 2f, dashH + 2f))
                        drawRect(color = litColor, topLeft = Offset(x, yTop), size = Size(w, dashH))
                    } else {
                        drawRect(color = dimColor, topLeft = Offset(dashX, yTop), size = Size(dashW, dashH))
                    }
                }

                val glyphY = topPad / 2f
                when {
                    isSelected -> {
                        val r = 4.dp.toPx()
                        val path = Path().apply {
                            moveTo(colCenterX, glyphY - r)
                            lineTo(colCenterX + r, glyphY)
                            lineTo(colCenterX, glyphY + r)
                            lineTo(colCenterX - r, glyphY)
                            close()
                        }
                        drawPath(path, color = onCard.copy(alpha = 0.75f))
                    }
                    isToday -> {
                        val todayGreen = Color(0xFF4BB543)
                        drawCircle(color = todayGreen.copy(alpha = 0.22f), radius = 7.dp.toPx(), center = Offset(colCenterX, glyphY))
                        drawCircle(color = todayGreen, radius = 3.5.dp.toPx(), center = Offset(colCenterX, glyphY))
                    }
                }

                val dayLabel = if (isWeekly) {
                    day.date.month.name.take(1) + day.date.dayOfMonth.toString().padStart(2, '0')
                } else {
                    day.date.dayOfMonth.toString().padStart(2, '0')
                }
                val labelLayout = textMeasurer.measure(
                    dayLabel,
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isToday || isSelected) onCard.copy(alpha = 0.90f) else onCard.copy(alpha = 0.28f),
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    ),
                )
                drawText(labelLayout, topLeft = Offset(colCenterX - labelLayout.size.width / 2f, dashAreaBottom + 5.dp.toPx()))
            }

            drawLine(
                color = onCard.copy(alpha = 0.45f),
                start = Offset(leftPad, targetY),
                end = Offset(lineEndX, targetY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())),
            )
            drawRect(
                color = cardBg,
                topLeft = Offset(lineEndX, targetY - tgtLayout.size.height / 2f - 2.dp.toPx()),
                size = Size(tgtLabelW, tgtLayout.size.height.toFloat() + 4.dp.toPx()),
            )
            drawText(
                tgtLayout,
                topLeft = Offset(lineEndX + 4.dp.toPx(), targetY - tgtLayout.size.height / 2f),
            )

            trendLine?.let { tl ->
                val diffDisplay = weightUnit.scaleDiff(tl.diffKg)
                val diffStr = if (diffDisplay >= 0) "+%.1f".format(diffDisplay) else "%.1f".format(diffDisplay)
                val trendLabelText = "DLT $diffStr"
                val trendLabelStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Red.copy(alpha = 0.85f))
                val trendLayout = textMeasurer.measure(trendLabelText, trendLabelStyle)
                val trendLabelW = trendLayout.size.width.toFloat() + 8.dp.toPx()

                val startFrac = ((tl.startKg - yMin) / (yMax - yMin)).coerceIn(0.0, 1.0).toFloat()
                val endFrac = ((tl.endKg - yMin) / (yMax - yMin)).coerceIn(0.0, 1.0).toFloat()
                val startY = dashAreaBottom - startFrac * innerHeight
                val endY = dashAreaBottom - endFrac * innerHeight

                val totalSpan = size.width - rightPad - leftPad
                val lineStartX = leftPad + trendLabelW
                val lineStartY = startY + (endY - startY) * trendLabelW / totalSpan

                drawLine(
                    color = Color.Red.copy(alpha = 0.75f),
                    start = Offset(lineStartX, lineStartY),
                    end = Offset(size.width - rightPad, endY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())),
                )
                drawRect(
                    color = cardBg,
                    topLeft = Offset(leftPad, startY - trendLayout.size.height / 2f - 2.dp.toPx()),
                    size = Size(trendLabelW, trendLayout.size.height.toFloat() + 4.dp.toPx()),
                )
                drawText(
                    trendLayout,
                    topLeft = Offset(leftPad, startY - trendLayout.size.height / 2f),
                )
            }
        }
    }
}

@Composable
private fun ContextStrip(selectedDate: LocalDate?, isWeekly: Boolean) {
    Text(
        text = if (selectedDate != null) appString(R.string.trends_tap_again_to_clear)
               else appString(if (isWeekly) R.string.trends_tap_a_week else R.string.trends_tap_a_day),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f),
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun EqualizerEditDrawer(
    editState: EqualizerEditState,
    accentColor: Color,
    onSave: (id: Long, weightKg: Double, timestamp: Instant) -> Unit,
    onDelete: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val initialTime = editState.existingMeasurement?.timestamp?.let {
        it.toLocalDateTime(tz).time
    } ?: LocalTime(editState.defaultHour, editState.defaultMinute)
    var selectedWeightKg by remember { mutableStateOf(editState.defaultWeightKg) }
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }
    var selectedDow by remember { mutableStateOf(editState.date.dayOfWeek.value) }
    var timeEditing by remember { mutableStateOf(false) }

    val displayDate = if (editState.isWeekly && editState.weekStart != null) {
        editState.weekStart.plus(DatePeriod(days = selectedDow - 1))
    } else {
        editState.date
    }
    val dow = displayDate.dayOfWeek.name.take(3)
    val mon = displayDate.month.name.take(3)
    val day = displayDate.dayOfMonth.toString().padStart(2, '0')
    val pickerHeight = 168.dp

    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$dow  $mon $day  ${displayDate.year}",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    color = onSurface.copy(alpha = 0.80f),
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
            if (editState.existingMeasurement != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = appString(R.string.cd_delete),
                        tint = onSurface.copy(alpha = 0.50f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        val dowNames = listOf(
            appString(R.string.day_mon),
            appString(R.string.day_tue),
            appString(R.string.day_wed),
            appString(R.string.day_thu),
            appString(R.string.day_fri),
            appString(R.string.day_sat),
            appString(R.string.day_sun),
        )

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                primary = onSurface,
                onPrimary = MaterialTheme.colorScheme.surface,
                onSurface = onSurface,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WeightWheelPicker(
                    initialWeightKg = editState.defaultWeightKg,
                    unit = editState.weightUnit,
                    onWeightKgChanged = { selectedWeightKg = it },
                    modifier = Modifier.weight(1f),
                )

                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(pickerHeight)
                        .background(onSurface.copy(alpha = 0.12f)),
                )

                if (editState.isWeekly && !timeEditing) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(pickerHeight)
                            .clickable { timeEditing = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = appString(R.string.trends_day_and_time),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.40f),
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${dowNames[selectedDow - 1]}  %02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = appString(R.string.trends_tap_to_change),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.35f),
                            ),
                        )
                    }
                } else if (editState.isWeekly) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IntWheelPicker(
                            initialValue = editState.date.dayOfWeek.value,
                            range = 1..7,
                            label = { dowNames[it - 1] },
                            onValueChanged = { selectedDow = it },
                            modifier = Modifier.weight(1f),
                            dividerEndPadding = 0.dp,
                        )
                        Text(
                            text = " ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface.copy(alpha = 0.60f),
                        )
                        IntWheelPicker(
                            initialValue = initialTime.hour,
                            range = 0..23,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedHour = it },
                            modifier = Modifier.weight(1f),
                            dividerEndPadding = 0.dp,
                            dividerStartPadding = 0.dp,
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface.copy(alpha = 0.60f),
                        )
                        IntWheelPicker(
                            initialValue = initialTime.minute,
                            range = 0..59,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedMinute = it },
                            modifier = Modifier.weight(1f),
                            dividerStartPadding = 0.dp,
                        )
                    }
                } else if (!timeEditing) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(pickerHeight)
                            .clickable { timeEditing = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = appString(R.string.trends_taken_at),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.40f),
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "%02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = appString(R.string.trends_tap_to_change),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.35f),
                            ),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IntWheelPicker(
                            initialValue = initialTime.hour,
                            range = 0..23,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedHour = it },
                            modifier = Modifier.weight(1f),
                            dividerEndPadding = 0.dp,
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface.copy(alpha = 0.60f),
                        )
                        IntWheelPicker(
                            initialValue = initialTime.minute,
                            range = 0..59,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedMinute = it },
                            modifier = Modifier.weight(1f),
                            dividerStartPadding = 0.dp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val dateTime = LocalDateTime(displayDate, LocalTime(selectedHour, selectedMinute))
                onSave(
                    editState.existingMeasurement?.id ?: 0L,
                    selectedWeightKg,
                    dateTime.toInstant(tz),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
        ) {
            Text(
                text = appString(R.string.label_save),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A0A0A),
                ),
            )
        }
    }
}

@Composable
private fun StatsRow(
    streak: Int,
    todayWeight: Double?,
    weeklyTrend: Double?,
    trendCloserToTarget: Boolean?,
    score: Int?,
    weightUnit: WeightUnit,
    isWeekly: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatPill(modifier = Modifier.weight(1f), label = appString(R.string.trends_streak)) {
            val onSurface = MaterialTheme.colorScheme.onSurface
            val streakColor = if (streak >= 5) Color(0xFF4BB543) else onSurface
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = streak.toString(),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = streakColor,
                    ),
                )
                Text(
                    text = " ${appString(if (isWeekly) R.string.trends_wks else R.string.trends_days)}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = streakColor.copy(alpha = 0.75f),
                    ),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }

        if (isWeekly) {
            StatPill(modifier = Modifier.weight(1f), label = appString(R.string.trends_this_week)) {
                val displayNum = todayWeight?.let { weightUnit.fromKg(it) }
                Text(
                    text = displayNum?.let { "%.1f".format(it) } ?: "−",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        } else {
            StatPill(
                modifier = Modifier.weight(1f),
                label = if (weeklyTrend != null) appString(R.string.trends_7_day_trend) else appString(R.string.stat_today),
            ) {
                val onSurface = MaterialTheme.colorScheme.onSurface
                if (weeklyTrend != null) {
                    val trendColor = if (trendCloserToTarget == true) Color(0xFF4BB543) else Color(0xFFD9534F)
                    val trendDisplay = weightUnit.scaleDiff(weeklyTrend)
                    val trendStr = if (trendDisplay >= 0) "+%.1f".format(trendDisplay) else "−%.1f".format(abs(trendDisplay))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (weeklyTrend >= 0) "▲" else "▼",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp, color = trendColor),
                            modifier = Modifier.padding(bottom = 4.dp, end = 2.dp),
                        )
                        Text(
                            text = trendStr,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = trendColor,
                            ),
                        )
                        Text(
                            text = " ${weightUnit.label}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = trendColor.copy(alpha = 0.75f)),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                } else {
                    val displayNum = todayWeight?.let { weightUnit.fromKg(it) }
                    Text(
                        text = displayNum?.let { "%.1f".format(it) } ?: "−",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface,
                        ),
                    )
                }
            }
        }

        StatPill(modifier = Modifier.weight(1f), label = appString(R.string.trends_score)) {
            val scoreColor = closenessColor((score ?: 0) / 100f)
            Text(
                text = score?.toString() ?: "−",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                    shadow = Shadow(color = scoreColor.copy(alpha = 0.5f), blurRadius = 10f),
                ),
            )
        }
    }
}

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
