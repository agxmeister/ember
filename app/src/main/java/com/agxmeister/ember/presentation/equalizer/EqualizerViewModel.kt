package com.agxmeister.ember.presentation.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetDailyCandlesUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsForDateUseCase
import com.agxmeister.ember.domain.usecase.SaveMeasurementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.math.abs

data class EqualizerDayData(
    val date: LocalDate,
    val weightKg: Double?,
)

data class EqualizerUiState(
    val days: List<EqualizerDayData>,
    val targetKg: Double,
    val tolerance: Double,
    val weightUnit: WeightUnit,
    val today: LocalDate,
    val selectedDate: LocalDate?,
    val streak: Int,
    val weeklyTrend: Double?,
    val weeklyAvg: Double?,
    val trendCloserToTarget: Boolean?,
)

data class EqualizerEditState(
    val date: LocalDate,
    val existingMeasurement: Measurement?,
    val defaultWeightKg: Double,
    val dayStartHour: Int,
    val dayStartMinute: Int,
    val weightUnit: WeightUnit,
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val getDailyCandles: GetDailyCandlesUseCase,
    private val getMeasurementsForDate: GetMeasurementsForDateUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _editState = MutableStateFlow<EqualizerEditState?>(null)
    val editState: StateFlow<EqualizerEditState?> = _editState.asStateFlow()

    fun toggleDay(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun openEdit(date: LocalDate) {
        viewModelScope.launch {
            val measurements = getMeasurementsForDate(date).first()
            val existing = measurements.maxByOrNull { it.timestamp }
            val current = uiState.value
            val defaultWeight = existing?.weightKg
                ?: getDailyCandles().first()
                    .filter { it.date < date }
                    .maxByOrNull { it.date }
                    ?.close
                ?: current.targetKg
            _editState.value = EqualizerEditState(
                date = date,
                existingMeasurement = existing,
                defaultWeightKg = defaultWeight,
                dayStartHour = preferencesRepository.dayStartHour.first(),
                dayStartMinute = preferencesRepository.dayStartMinute.first(),
                weightUnit = current.weightUnit,
            )
        }
    }

    fun closeEdit() {
        _editState.value = null
    }

    fun saveMeasurement(id: Long, weightKg: Double, timestamp: Instant) {
        viewModelScope.launch {
            saveMeasurementUseCase(id, weightKg, timestamp)
            closeEdit()
        }
    }

    val uiState: StateFlow<EqualizerUiState> = combine(
        combine(
            preferencesRepository.goalTargetKg,
            preferencesRepository.initialWeightKg,
            preferencesRepository.weightUnit,
        ) { target, initial, unit -> Triple(target, initial, unit) },
        combine(getDailyCandles(), _selectedDate) { candles, selected -> candles to selected },
    ) { (targetKg, initialWeightKg, weightUnit), (allCandles, selectedDate) ->
        val tolerance = abs(initialWeightKg - targetKg).coerceAtLeast(0.1)

        val candleMap = allCandles.associateBy { it.date }
        val windowStart = today.minus(DatePeriod(days = 13))
        val days = (0..13).map { offset ->
            val date = windowStart.plus(DatePeriod(days = offset))
            EqualizerDayData(date = date, weightKg = candleMap[date]?.close)
        }

        var streak = 0
        var expected = today
        for (candle in allCandles.sortedByDescending { it.date }) {
            if (candle.date > today) continue
            if (candle.date != expected) break
            streak++
            expected = expected.minus(DatePeriod(days = 1))
        }

        val last7Dates = (0..6).map { today.minus(DatePeriod(days = it)) }.toSet()
        val prev7Dates = (7..13).map { today.minus(DatePeriod(days = it)) }.toSet()
        val last7 = allCandles.filter { it.date in last7Dates }.map { it.close }
        val prev7 = allCandles.filter { it.date in prev7Dates }.map { it.close }

        val weeklyTrend: Double?
        val weeklyAvg: Double?
        val trendCloserToTarget: Boolean?
        if (last7.isNotEmpty() && prev7.isNotEmpty()) {
            val currentAvg = last7.average()
            val prevAvg = prev7.average()
            weeklyTrend = currentAvg - prevAvg
            weeklyAvg = null
            trendCloserToTarget = abs(currentAvg - targetKg) < abs(prevAvg - targetKg)
        } else {
            weeklyTrend = null
            weeklyAvg = if (last7.isNotEmpty()) last7.average() else null
            trendCloserToTarget = null
        }

        EqualizerUiState(
            days = days,
            targetKg = targetKg,
            tolerance = tolerance,
            weightUnit = weightUnit,
            today = today,
            selectedDate = selectedDate,
            streak = streak,
            weeklyTrend = weeklyTrend,
            weeklyAvg = weeklyAvg,
            trendCloserToTarget = trendCloserToTarget,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EqualizerUiState(
            days = emptyList(),
            targetKg = 70.0,
            tolerance = 10.0,
            weightUnit = WeightUnit.Kg,
            today = today,
            selectedDate = null,
            streak = 0,
            weeklyTrend = null,
            weeklyAvg = null,
            trendCloserToTarget = null,
        ),
    )
}
