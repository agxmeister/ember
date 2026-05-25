package com.agxmeister.ember.presentation.equalizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import com.agxmeister.ember.presentation.theme.trendSpeedColor
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(animateEntry: Boolean = false) {
    val viewModel: EqualizerViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    if (state.days.isEmpty()) return

    val todayColumnProgress = remember { Animatable(if (animateEntry) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animateEntry) {
            todayColumnProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            )
        }
    }

    val isFocused = state.selectedDate != null
    val displayDate = state.selectedDate ?: state.today
    val readoutWeight = if (isFocused) state.days.find { it.date == displayDate }?.weightKg else state.weeklyAvg
    val wkPrefix = appString(R.string.trends_readout_wk_prefix)
    val readoutLabel = state.selectedDate?.let {
        if (state.isWeekly) {
            val weekEnd = it.plus(DatePeriod(days = 6))
            val startDay = it.dayOfMonth.toString().padStart(2, '0')
            val endDay = weekEnd.dayOfMonth.toString().padStart(2, '0')
            if (it.month == weekEnd.month) {
                "$wkPrefix ${it.month.name.take(3)} $startDay/$endDay ${it.year}"
            } else {
                "$wkPrefix ${it.month.name.take(3)} $startDay / ${weekEnd.month.name.take(3)} $endDay ${weekEnd.year}"
            }
        } else {
            "${it.dayOfWeek.name.take(3)} ${it.month.name.take(3)} ${it.dayOfMonth.toString().padStart(2, '0')} ${it.year}"
        }
    } ?: if (state.isWeekly) appString(R.string.trends_readout_week_avg) else appString(R.string.trends_readout_day_avg)
    val darkTheme = isSystemInDarkTheme()
    val readoutCloseness = readoutWeight?.let { w ->
        (1.0 - abs(w - state.targetKg) / state.tolerance).coerceIn(0.0, 1.0).toFloat()
    } ?: 0f
    val readoutColor = closenessColor(readoutCloseness, darkTheme)
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
            weeklyRateKg = state.weeklyRateKg,
            goalIsLoss = state.goalIsLoss,
            trendMeasurementsNeeded = state.trendMeasurementsNeeded,
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
            canScrollLeft = state.canScrollLeft,
            canScrollRight = state.canScrollRight,
            todayColumnProgress = todayColumnProgress.value,
            onDayToggle = viewModel::toggleDay,
            onScroll = { viewModel.shiftWindow(it) },
        )
        Spacer(modifier = Modifier.height(6.dp))
        ContextStrip(state.selectedDate, state.isWeekly)
        Spacer(modifier = Modifier.height(12.dp))
        StatsRow(
            streak = state.streak,
            weeklyAvg = state.weeklyAvg,
            targetKg = state.targetKg,
            tolerance = state.tolerance,
            score = score,
            weightUnit = state.weightUnit,
            isWeekly = state.isWeekly,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProjectionCard(
            projection = state.projection,
            targetKg = state.targetKg,
            weightUnit = state.weightUnit,
            measurementsNeeded = state.trendMeasurementsNeeded,
        )
        Spacer(modifier = Modifier.height(8.dp))
        WeeklyRateCard(
            weeklyRateKg = state.weeklyRateKg,
            rateZone = state.rateZone,
            goalIsLoss = state.goalIsLoss,
            weightUnit = state.weightUnit,
        )
    }

    editState?.let { es ->
        val editCloseness = (1.0 - abs(es.defaultWeightKg - state.targetKg) / state.tolerance)
            .coerceIn(0.0, 1.0).toFloat()
        val accentColor = closenessColor(editCloseness, darkTheme)

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
    weeklyRateKg: Double?,
    goalIsLoss: Boolean,
    trendMeasurementsNeeded: Int?,
    displayColor: Color,
    weightUnit: WeightUnit,
    isFocused: Boolean,
    onTap: () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val weightStr = displayWeight?.let { "%.1f".format(weightUnit.fromKg(it)) } ?: "−.−"
    val diffDisplay = weeklyRateKg?.let { weightUnit.scaleDiff(it) }
    val arrow = when {
        diffDisplay == null -> null
        diffDisplay > 0.005 -> "▲"
        diffDisplay < -0.005 -> "▼"
        else -> null
    }
    val deltaStr = when {
        diffDisplay == null -> ".−−"
        abs(diffDisplay) > 0.005 -> "%.2f".format(abs(diffDisplay)).let { if (abs(diffDisplay) < 1.0) it.removePrefix("0") else it }
        else -> ".00"
    }
    val trendColor = trendSpeedColor(weeklyRateKg, goalIsLoss, darkTheme)

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
            Row(verticalAlignment = Alignment.Bottom) {
                val trendGlow = Shadow(color = trendColor.copy(alpha = 0.65f), blurRadius = 22f)
                if (arrow != null) {
                    Text(
                        text = arrow,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            color = trendColor,
                            shadow = trendGlow,
                        ),
                        modifier = Modifier.padding(bottom = 6.dp, end = 2.dp),
                    )
                }
                Text(
                    text = deltaStr,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor,
                        shadow = trendGlow,
                    ),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = appString(R.string.trends_tap_to_edit),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = if (isFocused) onBg.copy(alpha = 0.50f) else Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
            if (trendMeasurementsNeeded != null) {
                Text(
                    text = "$trendMeasurementsNeeded TO GO",
                    style = labelStyle,
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
