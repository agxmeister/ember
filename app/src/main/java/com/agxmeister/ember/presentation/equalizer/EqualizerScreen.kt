package com.agxmeister.ember.presentation.equalizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(animateEntry: Boolean = false) {
    val viewModel: EqualizerViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    if (state.days.isEmpty()) return

    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    val shouldAnimate = animateEntry && !hasAnimated
    val todayColumnProgress = remember { Animatable(if (shouldAnimate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (shouldAnimate) {
            todayColumnProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            )
            hasAnimated = true
        }
    }

    val isFocused = state.selectedDate != null
    val displayDate = state.selectedDate ?: state.today
    val readoutWeight = if (isFocused) state.days.find { it.date == displayDate }?.rawWeightKg else state.weeklyAvg
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Readout(
            modifier = Modifier.fillMaxWidth().weight(5f),
            displayWeight = readoutWeight,
            label = readoutLabel,
            weeklyRateKg = state.weeklyRateKg,
            goalIsLoss = state.goalIsLoss,
            trendMeasurementsNeeded = state.trendMeasurementsNeeded,
            displayColor = readoutColor,
            weightUnit = state.weightUnit,
            isFocused = isFocused,
            onTap = { viewModel.openEdit(displayDate) },
            days = state.days,
            targetKg = state.targetKg,
            tolerance = state.tolerance,
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
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StreakCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                streak = state.streak,
                isWeekly = state.isWeekly,
            )
            VolatilityCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                volatilityKg = state.volatilityKg,
                weightUnit = state.weightUnit,
                isWeekly = state.isWeekly,
            )
            ScoreCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                score = state.score,
                isWeekly = state.isWeekly,
            )
        }
        ProjectionCard(
            modifier = Modifier.fillMaxWidth().weight(2f),
            projection = state.projection,
            targetKg = state.targetKg,
            weightUnit = state.weightUnit,
            measurementsNeeded = state.trendMeasurementsNeeded,
        )
        WeeklyRateCard(
            modifier = Modifier.fillMaxWidth().weight(1f),
            weeklyRateKg = state.weeklyRateKg,
            rateZone = state.rateZone,
            measurementsNeeded = state.trendMeasurementsNeeded,
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
