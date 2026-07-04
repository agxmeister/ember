package com.agxmeister.ember.presentation.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.AlgorithmConfig
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetCloseMeasurementForDateUseCase
import com.agxmeister.ember.domain.usecase.GetDailyCandlesUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsForDateUseCase
import com.agxmeister.ember.domain.usecase.DeleteMeasurementUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsForWeekUseCase
import com.agxmeister.ember.domain.usecase.GetWeeklyDataUseCase
import com.agxmeister.ember.domain.usecase.SaveMeasurementUseCase
import com.agxmeister.ember.domain.usecase.isoWeekStart
import com.agxmeister.ember.domain.util.median
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
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class EqualizerDayData(
    val date: LocalDate,
    val weightKg: Double?,
    val rawWeightKg: Double? = null,
)

data class TrendLineData(val startKg: Double, val endKg: Double, val diffKg: Double)

sealed interface TrendPending {
    val measurementsNeeded: Int
    data class NotEnoughData(override val measurementsNeeded: Int) : TrendPending
    data class GapTooBig(override val measurementsNeeded: Int) : TrendPending
}

sealed interface ProjectionResult {
    data class Eta(val date: LocalDate, val daysAway: Int, val currentAvgKg: Double, val progress: Float?, val goalIsLoss: Boolean) : ProjectionResult
    data object Reached : ProjectionResult
    sealed interface Unavailable : ProjectionResult {
        data object NotEnoughData : Unavailable
        data object WrongDirection : Unavailable
        data object TooFar : Unavailable
    }
}

enum class WeeklyRateZone { TooSlow, Healthy, Aggressive, TooFast, WrongDirection, Unavailable }

data class EqualizerUiState(
    val days: List<EqualizerDayData>,
    val targetKg: Double,
    val tolerance: Double,
    val weightUnit: WeightUnit,
    val today: LocalDate,
    val selectedDate: LocalDate?,
    val streak: Int,
    val weeklyAvg: Double?,
    val score: Int?,
    val volatilityKg: Double?,
    val isWeekly: Boolean,
    val trendLine: TrendLineData?,
    val weeklyRateKg: Double?,
    val goalIsLoss: Boolean,
    val trendPending: TrendPending?,
    val canScrollLeft: Boolean,
    val canScrollRight: Boolean,
    val projection: ProjectionResult,
    val rateZone: WeeklyRateZone,
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
    private val getCloseMeasurementForDate: GetCloseMeasurementForDateUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val deleteMeasurementUseCase: DeleteMeasurementUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _editState = MutableStateFlow<EqualizerEditState?>(null)
    val editState: StateFlow<EqualizerEditState?> = _editState.asStateFlow()
    private val _windowOffset = MutableStateFlow(0)

    fun toggleDay(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun shiftWindow(delta: Int) {
        _selectedDate.value = null
        _windowOffset.value = (_windowOffset.value + delta).coerceAtLeast(0)
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
                val tz = TimeZone.currentSystemDefault()
                val actualDate = existing?.timestamp?.toLocalDateTime(tz)?.date ?: targetDate
                val defaultWeight = existing?.weightKg
                    ?: getWeeklyData().first().filter { it.weekStart < date }.maxByOrNull { it.weekStart }?.median
                    ?: current.targetKg
                _editState.value = EqualizerEditState(
                    date = actualDate,
                    existingMeasurement = existing,
                    defaultWeightKg = defaultWeight,
                    defaultHour = notifHour,
                    defaultMinute = notifMinute,
                    weightUnit = current.weightUnit,
                    isWeekly = true,
                    weekStart = date,
                )
            } else {
                val existing = getCloseMeasurementForDate(date).first()
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

    fun deleteMeasurements() {
        val es = _editState.value ?: return
        viewModelScope.launch {
            val measurements = if (es.isWeekly && es.weekStart != null) {
                getMeasurementsForWeek(es.weekStart).first()
            } else {
                getMeasurementsForDate(es.date).first()
            }
            measurements.forEach { deleteMeasurementUseCase(it.id) }
            closeEdit()
        }
    }

    val uiState: StateFlow<EqualizerUiState> = combine(
        combine(
            preferencesRepository.goalTargetKg,
            preferencesRepository.initialWeightKg,
            preferencesRepository.weightUnit,
        ) { target, initial, unit -> Triple(target, initial, unit) },
        combine(
            preferencesRepository.weighingFrequency,
            preferencesRepository.goalStartDate,
            _windowOffset,
            preferencesRepository.algorithmConfig,
        ) { freq, gsd, offset, config -> Triple(freq, gsd, offset) to config },
        combine(getDailyCandles(), getWeeklyData(), _selectedDate) { c, w, s -> Triple(c, w, s) },
    ) { (targetKg, initialWeightKg, weightUnit), (freqGsdOffset, algorithmConfig), (allCandles, allWeekly, selectedDate) ->
        val (frequency, goalStartDateStr, rawOffset) = freqGsdOffset
        val recentWeight = allCandles.maxByOrNull { it.date }?.close
            ?: allWeekly.maxByOrNull { it.weekStart }?.median
            ?: initialWeightKg
        val goalIsLoss = recentWeight > targetKg
        val tolerance = abs(initialWeightKg - targetKg).coerceAtLeast(0.1)
        val isWeekly = frequency == WeighingFrequency.Weekly

        val days: List<EqualizerDayData>
        val today: LocalDate
        val streak: Int
        val weeklyAvg: Double?
        val score: Int?
        val volatilityKg: Double?
        val trendLine: TrendLineData?
        val weeklyRateKg: Double?
        val trendPending: TrendPending?
        val canScrollLeft: Boolean
        val canScrollRight: Boolean

        val goalDate = goalStartDateStr.takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        if (isWeekly) {
            val currentWeekStart = todayDate.isoWeekStart()
            today = currentWeekStart

            val goalWeekStart = goalDate?.isoWeekStart()
            val currentEpochWeeks = currentWeekStart.toEpochDays() / 7
            val goalEpochWeeks = goalWeekStart?.toEpochDays()?.div(7) ?: (currentEpochWeeks - 13)
            val maxOffset = (currentEpochWeeks - goalEpochWeeks - 13).toInt().coerceAtLeast(0)
            val effectiveOffset = rawOffset.coerceIn(0, maxOffset)
            canScrollLeft = effectiveOffset < maxOffset
            canScrollRight = effectiveOffset > 0

            val weeklyMap = allWeekly.associateBy { it.weekStart }
            val windowStart = currentWeekStart.minus(DatePeriod(days = (13 + effectiveOffset) * 7))
            days = (0..13).map { offset ->
                val weekStart = windowStart.plus(DatePeriod(days = offset * 7))
                EqualizerDayData(date = weekStart, weightKg = weeklyMap[weekStart]?.median)
            }

            trendLine = computeTrendLine(days, maxStalePeriods = algorithmConfig.staleCutoffPeriods)
            val lastMeasuredWeekIdx = days.indexOfLast { it.weightKg != null }
            val hasRecentWeek = lastMeasuredWeekIdx >= days.size - algorithmConfig.staleCutoffPeriods
            val rateWeekWindowStart = currentWeekStart.minus(DatePeriod(days = 27 * 7))
            val rateWeekWindow = (0..27).map { offset ->
                val weekStart = rateWeekWindowStart.plus(DatePeriod(days = offset * 7))
                EqualizerDayData(date = weekStart, weightKg = weeklyMap[weekStart]?.median)
            }
            val rateWeekSegment = mostRecentCleanSegment(rateWeekWindow, algorithmConfig.maxGapDays)
            val measuredInWeekSegment = rateWeekSegment.count { it.weightKg != null }
            weeklyRateKg = if (hasRecentWeek && measuredInWeekSegment >= MIN_MEASURED_FOR_RATE) {
                computeWeeklyRate(rateWeekSegment, indexStepDays = 7, maxGap = algorithmConfig.maxGapDays)
            } else null
            trendPending = if (weeklyRateKg == null) {
                computeTrendPending(rateWeekWindow, measuredInWeekSegment)
            } else null

            var s = 0
            var w = weeklyMap.keys.filter { it <= currentWeekStart }.maxOrNull() ?: currentWeekStart
            while (true) {
                if (!weeklyMap.containsKey(w)) break
                val wStart = w.minus(DatePeriod(days = (algorithmConfig.streakWindow - 1) * 7))
                val window = (0 until algorithmConfig.streakWindow).map { offset ->
                    val weekStart = wStart.plus(DatePeriod(days = offset * 7))
                    EqualizerDayData(date = weekStart, weightKg = weeklyMap[weekStart]?.median)
                }
                val t = computeTrendLine(window) ?: break
                if (if (goalIsLoss) t.diffKg < 0 else t.diffKg > 0) s++ else break
                w = w.minus(DatePeriod(days = 7))
            }
            streak = s

            val windowWeeks = days.mapNotNull { it.weightKg }
            weeklyAvg = if (windowWeeks.isNotEmpty()) windowWeeks.median() else null

            val scoreStart = days.last().date.minus(DatePeriod(days = (algorithmConfig.scoreWindow - 1) * 7))
            val scoreWeeks = (0 until algorithmConfig.scoreWindow).map { offset ->
                val weekStart = scoreStart.plus(DatePeriod(days = offset * 7))
                EqualizerDayData(date = weekStart, weightKg = weeklyMap[weekStart]?.median)
            }
            score = computeScore(scoreWeeks, indexStepDays = 7, streak = streak, goalIsLoss = goalIsLoss, maxGap = algorithmConfig.maxGapDays)

            val volStart = days.last().date.minus(DatePeriod(days = (algorithmConfig.volatilityWindow - 1) * 7))
            val volWeeks = (0 until algorithmConfig.volatilityWindow).map { offset ->
                val weekStart = volStart.plus(DatePeriod(days = offset * 7))
                EqualizerDayData(date = weekStart, weightKg = weeklyMap[weekStart]?.median)
            }
            volatilityKg = computeVolatility(volWeeks, algorithmConfig.minMeasuredForVolatility)
        } else {
            today = todayDate

            val todayEpochDays = todayDate.toEpochDays()
            val goalEpochDays = goalDate?.toEpochDays() ?: (todayEpochDays - 13)
            val maxOffset = (todayEpochDays - goalEpochDays - 13).toInt().coerceAtLeast(0)
            val effectiveOffset = rawOffset.coerceIn(0, maxOffset)
            canScrollLeft = effectiveOffset < maxOffset
            canScrollRight = effectiveOffset > 0

            val candleMap = allCandles.associateBy { it.date }
            val windowStart = todayDate.minus(DatePeriod(days = 13 + effectiveOffset))
            days = (0..13).map { offset ->
                val date = windowStart.plus(DatePeriod(days = offset))
                val candle = candleMap[date]
                EqualizerDayData(date = date, weightKg = candle?.close, rawWeightKg = candle?.rawClose)
            }

            trendLine = computeTrendLine(days, maxStalePeriods = algorithmConfig.staleCutoffPeriods)
            val lastMeasuredDayIdx = days.indexOfLast { it.weightKg != null }
            val hasRecentDay = lastMeasuredDayIdx >= days.size - algorithmConfig.staleCutoffPeriods
            val regressionDays = algorithmConfig.regressionIntervalDays
            val rateDayWindowStart = todayDate.minus(DatePeriod(days = regressionDays - 1))
            val rateDayWindow = (0 until regressionDays).map { offset ->
                val date = rateDayWindowStart.plus(DatePeriod(days = offset))
                EqualizerDayData(date = date, weightKg = candleMap[date]?.close)
            }
            val rateDaySegment = mostRecentCleanSegment(rateDayWindow, algorithmConfig.maxGapDays)
            val measuredInDaySegment = rateDaySegment.count { it.weightKg != null }
            weeklyRateKg = if (hasRecentDay && measuredInDaySegment >= MIN_MEASURED_FOR_RATE) {
                computeWeeklyRate(rateDaySegment, maxGap = algorithmConfig.maxGapDays)
            } else null
            trendPending = if (weeklyRateKg == null) {
                computeTrendPending(rateDayWindow, measuredInDaySegment)
            } else null

            var s = 0
            var d = candleMap.keys.filter { it <= todayDate }.maxOrNull() ?: todayDate
            while (true) {
                if (!candleMap.containsKey(d)) break
                val wStart = d.minus(DatePeriod(days = algorithmConfig.streakWindow - 1))
                val window = (0 until algorithmConfig.streakWindow).map { offset ->
                    val date = wStart.plus(DatePeriod(days = offset))
                    EqualizerDayData(date = date, weightKg = candleMap[date]?.close)
                }
                val t = computeTrendLine(window) ?: break
                if (if (goalIsLoss) t.diffKg < 0 else t.diffKg > 0) s++ else break
                d = d.minus(DatePeriod(days = 1))
            }
            streak = s

            val windowWeights = days.mapNotNull { it.weightKg }
            weeklyAvg = if (windowWeights.isNotEmpty()) windowWeights.median() else null

            val scoreStart = days.last().date.minus(DatePeriod(days = algorithmConfig.scoreWindow - 1))
            val scoreDays = (0 until algorithmConfig.scoreWindow).map { offset ->
                val date = scoreStart.plus(DatePeriod(days = offset))
                EqualizerDayData(date = date, weightKg = candleMap[date]?.close)
            }
            score = computeScore(scoreDays, indexStepDays = 1, streak = streak, goalIsLoss = goalIsLoss, maxGap = algorithmConfig.maxGapDays)

            val volStart = days.last().date.minus(DatePeriod(days = algorithmConfig.volatilityWindow - 1))
            val volDays = (0 until algorithmConfig.volatilityWindow).map { offset ->
                val date = volStart.plus(DatePeriod(days = offset))
                EqualizerDayData(date = date, weightKg = candleMap[date]?.close)
            }
            volatilityKg = computeVolatility(volDays, algorithmConfig.minMeasuredForVolatility)
        }

        val projection = computeProjection(weeklyAvg, weeklyRateKg, targetKg, initialWeightKg, goalIsLoss, todayDate)
        val rateZone = classifyWeeklyRate(weeklyRateKg, goalIsLoss)

        EqualizerUiState(
            days = days,
            targetKg = targetKg,
            tolerance = tolerance,
            weightUnit = weightUnit,
            today = today,
            selectedDate = selectedDate,
            streak = streak,
            weeklyAvg = weeklyAvg,
            score = score,
            volatilityKg = volatilityKg,
            isWeekly = isWeekly,
            trendLine = trendLine,
            weeklyRateKg = weeklyRateKg,
            goalIsLoss = goalIsLoss,
            trendPending = trendPending,
            canScrollLeft = canScrollLeft,
            canScrollRight = canScrollRight,
            projection = projection,
            rateZone = rateZone,
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
            weeklyAvg = null,
            score = null,
            volatilityKg = null,
            isWeekly = false,
            trendLine = null,
            weeklyRateKg = null,
            goalIsLoss = true,
            trendPending = null,
            canScrollLeft = false,
            canScrollRight = false,
            projection = ProjectionResult.Unavailable.NotEnoughData,
            rateZone = WeeklyRateZone.Unavailable,
        ),
    )
}

private fun computeTrendLine(
    days: List<EqualizerDayData>,
    minPoints: Int = 2,
    maxStalePeriods: Int? = null,
): TrendLineData? {
    val measured = days.mapIndexedNotNull { idx, day ->
        day.weightKg?.let { Pair(idx.toDouble(), it) }
    }
    if (measured.size < minPoints) return null
    if (maxStalePeriods != null && measured.last().first.toInt() < days.size - maxStalePeriods) return null
    val xs = measured.map { it.first }
    val ys = measured.map { it.second }
    val (slope, intercept) = linearRegression(xs, ys) ?: return null
    val startKg = intercept
    val endKg = slope * (days.size - 1) + intercept
    return TrendLineData(startKg = startKg, endKg = endKg, diffKg = endKg - startKg)
}

private fun computeVolatility(window: List<EqualizerDayData>, minMeasured: Int): Double? {
    val measured = window.mapIndexedNotNull { idx, day -> day.weightKg?.let { idx.toDouble() to it } }
    if (measured.size < minMeasured) return null
    val xs = measured.map { it.first }
    val ys = measured.map { it.second }
    val (slope, intercept) = linearRegression(xs, ys) ?: return null
    val sumSquaredResiduals = measured.sumOf { (x, y) ->
        val residual = y - (slope * x + intercept)
        residual * residual
    }
    return sqrt(sumSquaredResiduals / measured.size)
}

private fun computeProjection(
    weeklyAvg: Double?,
    weeklyRateKg: Double?,
    targetKg: Double,
    initialWeightKg: Double,
    goalIsLoss: Boolean,
    today: LocalDate,
): ProjectionResult {
    if (weeklyAvg == null || weeklyRateKg == null) return ProjectionResult.Unavailable.NotEnoughData
    val alreadyReached = if (goalIsLoss) weeklyAvg <= targetKg else weeklyAvg >= targetKg
    if (alreadyReached) return ProjectionResult.Reached
    val correctDirection = if (goalIsLoss) weeklyRateKg < -0.05 else weeklyRateKg > 0.05
    if (!correctDirection) return ProjectionResult.Unavailable.WrongDirection
    val dailyRate = weeklyRateKg / 7.0
    val daysToTarget = ((targetKg - weeklyAvg) / dailyRate).toInt()
    if (daysToTarget < 0) return ProjectionResult.Unavailable.WrongDirection
    if (daysToTarget > 730) return ProjectionResult.Unavailable.TooFar
    val totalDelta = abs(initialWeightKg - targetKg)
    val progress = if (totalDelta > 0.0) (abs(initialWeightKg - weeklyAvg) / totalDelta).toFloat().coerceIn(0f, 1f) else null
    return ProjectionResult.Eta(
        date = today.plus(DatePeriod(days = daysToTarget)),
        daysAway = daysToTarget,
        currentAvgKg = weeklyAvg,
        progress = progress,
        goalIsLoss = goalIsLoss,
    )
}

private fun computeScore(
    scoreDays: List<EqualizerDayData>,
    indexStepDays: Int,
    streak: Int,
    goalIsLoss: Boolean,
    maxGap: Int,
): Int? {
    val window = scoreDays.size
    val measuredCount = scoreDays.count { it.weightKg != null }
    if (window == 0 || measuredCount == 0) return null

    val factors = mutableListOf<Pair<Double, Double>>()

    val momentum = computeWeeklyRate(scoreDays, indexStepDays, maxGap)?.let { momentumQuality(it, goalIsLoss) }
    if (momentum != null) factors += 0.5 to momentum

    val consistency = (streak.toDouble() / window).coerceIn(0.0, 1.0)
    factors += 0.25 to consistency

    val adherence = (measuredCount.toDouble() / window).coerceIn(0.0, 1.0)
    factors += 0.25 to adherence

    val totalWeight = factors.sumOf { it.first }
    val weighted = factors.sumOf { it.first * it.second } / totalWeight
    return (weighted * 100).roundToInt()
}

private fun momentumQuality(weeklyRateKg: Double, goalIsLoss: Boolean): Double =
    when (classifyWeeklyRate(weeklyRateKg, goalIsLoss)) {
        WeeklyRateZone.Healthy -> 1.0
        WeeklyRateZone.Aggressive -> 0.8
        WeeklyRateZone.TooSlow -> 0.5
        WeeklyRateZone.TooFast -> 0.4
        WeeklyRateZone.WrongDirection -> 0.0
        WeeklyRateZone.Unavailable -> 0.0
    }

private fun classifyWeeklyRate(weeklyRateKg: Double?, goalIsLoss: Boolean): WeeklyRateZone {
    if (weeklyRateKg == null) return WeeklyRateZone.Unavailable
    val absRate = abs(weeklyRateKg)
    val correctDirection = if (goalIsLoss) weeklyRateKg < -0.05 else weeklyRateKg > 0.05
    if (!correctDirection && absRate > 0.1) return WeeklyRateZone.WrongDirection
    return when {
        absRate < 0.25 -> WeeklyRateZone.TooSlow
        absRate <= 1.0 -> WeeklyRateZone.Healthy
        absRate <= 1.5 -> WeeklyRateZone.Aggressive
        else -> WeeklyRateZone.TooFast
    }
}

private const val MIN_MEASURED_FOR_RATE = 7

private fun computeWeeklyRate(window: List<EqualizerDayData>, indexStepDays: Int = 1, maxGap: Int): Double? {
    val filled = fillGaps(window, maxGap) ?: return null
    val measured = filled.mapIndexedNotNull { idx, day -> day.weightKg?.let { idx.toDouble() to it } }
    if (measured.size < 2) return null
    val (slope, _) = linearRegression(measured.map { it.first }, measured.map { it.second }) ?: return null
    return slope * 7.0 / indexStepDays
}

private fun computeTrendPending(rateWindow: List<EqualizerDayData>, measuredInSegment: Int): TrendPending {
    val needed = (MIN_MEASURED_FOR_RATE - measuredInSegment).coerceAtLeast(1)
    val hasOlderData = rateWindow.count { it.weightKg != null } > measuredInSegment
    return if (hasOlderData) TrendPending.GapTooBig(needed) else TrendPending.NotEnoughData(needed)
}

private fun mostRecentCleanSegment(window: List<EqualizerDayData>, maxGap: Int): List<EqualizerDayData> {
    val measuredIdx = window.mapIndexedNotNull { idx, day -> idx.takeIf { day.weightKg != null } }
    if (measuredIdx.isEmpty()) return emptyList()
    var start = measuredIdx.last()
    for (k in measuredIdx.size - 1 downTo 1) {
        if (measuredIdx[k] - measuredIdx[k - 1] - 1 > maxGap) break
        start = measuredIdx[k - 1]
    }
    return window.subList(start, window.size)
}

private fun fillGaps(days: List<EqualizerDayData>, maxGap: Int): List<EqualizerDayData>? {
    val weights = days.map { it.weightKg }.toMutableList()
    val n = weights.size
    var i = 0
    while (i < n) {
        if (weights[i] == null) {
            var j = i
            while (j < n && weights[j] == null) j++
            val before = if (i > 0) weights[i - 1] else null
            val after = if (j < n) weights[j] else null
            if (before != null && after != null) {
                if (j - i > maxGap) return null
                for (k in i until j) {
                    weights[k] = before + (after - before) * (k - i + 1).toDouble() / (j - i + 1)
                }
            }
            // edge gaps (no before or no after) are left as null
            i = j
        } else {
            i++
        }
    }
    return days.mapIndexed { idx, day -> day.copy(weightKg = weights[idx]) }
}

private fun linearRegression(xs: List<Double>, ys: List<Double>): Pair<Double, Double>? {
    val n = xs.size.toDouble()
    val sumX = xs.sum()
    val sumY = ys.sum()
    val sumXY = xs.zip(ys).sumOf { (x, y) -> x * y }
    val sumX2 = xs.sumOf { x -> x * x }
    val denom = n * sumX2 - sumX * sumX
    if (denom == 0.0) return null
    val slope = (n * sumXY - sumX * sumY) / denom
    val intercept = (sumY - slope * sumX) / n
    return Pair(slope, intercept)
}
