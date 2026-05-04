package com.agxmeister.ember.presentation.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetDailyCandlesUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsForDateUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsForWeekUseCase
import com.agxmeister.ember.domain.usecase.GetWeeklyDataUseCase
import com.agxmeister.ember.domain.usecase.SaveMeasurementUseCase
import com.agxmeister.ember.domain.usecase.isoWeekStart
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
    val isWeekly: Boolean,
)

data class EqualizerEditState(
    val date: LocalDate,
    val existingMeasurement: Measurement?,
    val defaultWeightKg: Double,
    val defaultHour: Int,
    val defaultMinute: Int,
    val weightUnit: WeightUnit,
    val isWeekly: Boolean = false,
    val weekStart: LocalDate? = null,
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val getDailyCandles: GetDailyCandlesUseCase,
    private val getWeeklyData: GetWeeklyDataUseCase,
    private val getMeasurementsForDate: GetMeasurementsForDateUseCase,
    private val getMeasurementsForWeek: GetMeasurementsForWeekUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _editState = MutableStateFlow<EqualizerEditState?>(null)
    val editState: StateFlow<EqualizerEditState?> = _editState.asStateFlow()

    fun toggleDay(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun openEdit(date: LocalDate) {
        viewModelScope.launch {
            val frequency = preferencesRepository.weighingFrequency.first()
            val current = uiState.value

            if (frequency == WeighingFrequency.Weekly) {
                val notifDow = preferencesRepository.notificationDayOfWeek.first()
                val notifHour = preferencesRepository.notificationHour.first()
                val notifMinute = preferencesRepository.notificationMinute.first()
                val targetDate = date.plus(DatePeriod(days = notifDow - 1))
                val measurements = getMeasurementsForWeek(date).first()
                val existing = measurements.maxByOrNull { it.timestamp }
                val defaultWeight = existing?.weightKg
                    ?: getWeeklyData().first().filter { it.weekStart < date }.maxByOrNull { it.weekStart }?.median
                    ?: current.targetKg
                _editState.value = EqualizerEditState(
                    date = targetDate,
                    existingMeasurement = existing,
                    defaultWeightKg = defaultWeight,
                    defaultHour = notifHour,
                    defaultMinute = notifMinute,
                    weightUnit = current.weightUnit,
                    isWeekly = true,
                    weekStart = date,
                )
            } else {
                val measurements = getMeasurementsForDate(date).first()
                val existing = measurements.maxByOrNull { it.timestamp }
                val defaultWeight = existing?.weightKg
                    ?: getDailyCandles().first()
                        .filter { it.date < date }
                        .maxByOrNull { it.date }
                        ?.close
                    ?: current.targetKg
                val dayStartHour = preferencesRepository.dayStartHour.first()
                val dayStartMinute = preferencesRepository.dayStartMinute.first()
                val totalMinutes = dayStartHour * 60 + dayStartMinute + 15
                _editState.value = EqualizerEditState(
                    date = date,
                    existingMeasurement = existing,
                    defaultWeightKg = defaultWeight,
                    defaultHour = totalMinutes / 60 % 24,
                    defaultMinute = totalMinutes % 60,
                    weightUnit = current.weightUnit,
                )
            }
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
        preferencesRepository.weighingFrequency,
        combine(getDailyCandles(), getWeeklyData(), _selectedDate) { c, w, s -> Triple(c, w, s) },
    ) { (targetKg, initialWeightKg, weightUnit), frequency, (allCandles, allWeekly, selectedDate) ->
        val tolerance = abs(initialWeightKg - targetKg).coerceAtLeast(0.1)
        val isWeekly = frequency == WeighingFrequency.Weekly

        val days: List<EqualizerDayData>
        val today: LocalDate
        val streak: Int
        val weeklyTrend: Double?
        val weeklyAvg: Double?
        val trendCloserToTarget: Boolean?

        if (isWeekly) {
            val currentWeekStart = todayDate.isoWeekStart()
            today = currentWeekStart

            val weeklyMap = allWeekly.associateBy { it.weekStart }
            val windowStart = currentWeekStart.minus(DatePeriod(days = 13 * 7))
            days = (0..13).map { offset ->
                val weekStart = windowStart.plus(DatePeriod(days = offset * 7))
                EqualizerDayData(date = weekStart, weightKg = weeklyMap[weekStart]?.median)
            }

            var s = 0
            var expected = currentWeekStart
            for (week in allWeekly.sortedByDescending { it.weekStart }) {
                if (week.weekStart > currentWeekStart) continue
                if (week.weekStart != expected) break
                s++
                expected = expected.minus(DatePeriod(days = 7))
            }
            streak = s

            val last7Weeks = (0..6).map { currentWeekStart.minus(DatePeriod(days = it * 7)) }.toSet()
            val last7WeekData = allWeekly.filter { it.weekStart in last7Weeks }
            weeklyAvg = if (last7WeekData.isNotEmpty()) last7WeekData.map { it.median }.median() else null

            val currentWeek = allWeekly.find { it.weekStart == currentWeekStart }
            val prevWeek = allWeekly.find { it.weekStart == currentWeekStart.minus(DatePeriod(days = 7)) }
            if (currentWeek != null && prevWeek != null) {
                weeklyTrend = currentWeek.median - prevWeek.median
                trendCloserToTarget = abs(currentWeek.median - targetKg) < abs(prevWeek.median - targetKg)
            } else {
                weeklyTrend = null
                trendCloserToTarget = null
            }
        } else {
            today = todayDate

            val candleMap = allCandles.associateBy { it.date }
            val windowStart = todayDate.minus(DatePeriod(days = 13))
            days = (0..13).map { offset ->
                val date = windowStart.plus(DatePeriod(days = offset))
                EqualizerDayData(date = date, weightKg = candleMap[date]?.close)
            }

            var s = 0
            var expected = todayDate
            for (candle in allCandles.sortedByDescending { it.date }) {
                if (candle.date > todayDate) continue
                if (candle.date != expected) break
                s++
                expected = expected.minus(DatePeriod(days = 1))
            }
            streak = s

            val last7Dates = (0..6).map { todayDate.minus(DatePeriod(days = it)) }.toSet()
            val prev7Dates = (7..13).map { todayDate.minus(DatePeriod(days = it)) }.toSet()
            val last7 = allCandles.filter { it.date in last7Dates }.map { it.close }
            val prev7 = allCandles.filter { it.date in prev7Dates }.map { it.close }

            weeklyAvg = if (last7.isNotEmpty()) last7.median() else null

            if (last7.isNotEmpty() && prev7.isNotEmpty()) {
                val currentMedian = last7.median()
                val prevMedian = prev7.median()
                weeklyTrend = currentMedian - prevMedian
                trendCloserToTarget = abs(currentMedian - targetKg) < abs(prevMedian - targetKg)
            } else {
                weeklyTrend = null
                trendCloserToTarget = null
            }
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
            isWeekly = isWeekly,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EqualizerUiState(
            days = emptyList(),
            targetKg = 70.0,
            tolerance = 10.0,
            weightUnit = WeightUnit.Kg,
            today = todayDate,
            selectedDate = null,
            streak = 0,
            weeklyTrend = null,
            weeklyAvg = null,
            trendCloserToTarget = null,
            isWeekly = false,
        ),
    )
}

private fun List<Double>.median(): Double {
    val s = sorted()
    return if (s.size % 2 == 0) (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0 else s[s.size / 2]
}
