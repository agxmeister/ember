package com.agxmeister.ember.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.DailyCandle
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetDailyCandlesUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

sealed class ChartUiState {
    data object Empty : ChartUiState()
    data class Candle(
        val candles: List<DailyCandle>,
        val recentCount: Int,
        val showChart: Boolean,
        val showMedianLine: Boolean,
        val median: Double?,
        val trend: Double?,
        val weightGoal: WeightGoal,
        val weightUnit: WeightUnit,
        val visualizationDate: LocalDate,
    ) : ChartUiState()
}

@HiltViewModel
class ChartViewModel @Inject constructor(
    getMeasurements: GetMeasurementsUseCase,
    getDailyCandles: GetDailyCandlesUseCase,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _visualizationDate = MutableStateFlow(today)

    fun setVisualizationDate(date: LocalDate) {
        _visualizationDate.value = date
    }

    val uiState: StateFlow<ChartUiState> = combine(
        combine(preferencesRepository.weightGoal, preferencesRepository.weightUnit, _visualizationDate) { g, u, d -> Triple(g, u, d) },
        combine(getMeasurements(), getDailyCandles()) { m, c -> m to c },
    ) { (weightGoal, weightUnit, vizDate), (measurements, candles) ->
        if (candles.isEmpty()) return@combine ChartUiState.Empty

        val tz = TimeZone.currentSystemDefault()
        val vizInstant = vizDate.plus(DatePeriod(days = 1)).atStartOfDayIn(tz)
        val oneWeekBefore = vizInstant - 7.days
        val twoWeeksBefore = vizInstant - 14.days

        val recentCount = measurements.count { it.timestamp >= oneWeekBefore && it.timestamp < vizInstant }
        val previousWeekCount = measurements.count { it.timestamp >= twoWeeksBefore && it.timestamp < oneWeekBefore }

        val showChart = recentCount >= 3
        val oldestTimestamp = measurements.minByOrNull { it.timestamp }?.timestamp
        val showMedian = oldestTimestamp != null && (vizInstant - oldestTimestamp) >= 7.days
        val showTrend = previousWeekCount >= 1

        val currentWeekWeights = measurements
            .filter { it.timestamp >= oneWeekBefore && it.timestamp < vizInstant }
            .map { it.weightKg }
        val median = if (showMedian) {
            if (currentWeekWeights.isNotEmpty()) currentWeekWeights.median()
            else measurements.map { it.weightKg }.median()
        } else null
        val trend = if (showTrend && median != null) {
            val previousWeekWeights = measurements
                .filter { it.timestamp >= twoWeeksBefore && it.timestamp < oneWeekBefore }
                .map { it.weightKg }
            if (previousWeekWeights.isNotEmpty()) median - previousWeekWeights.median() else null
        } else null

        val windowStart = vizDate.minus(DatePeriod(days = 6))
        val visibleCandles = candles.filter { it.date >= windowStart && it.date <= vizDate }

        ChartUiState.Candle(visibleCandles, recentCount, showChart, showMedian, median, trend, weightGoal, weightUnit, vizDate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartUiState.Empty,
    )
}

private fun List<Double>.median(): Double {
    val sorted = sorted()
    return if (sorted.size % 2 == 0) {
        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
    } else {
        sorted[sorted.size / 2]
    }
}
