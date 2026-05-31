package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.MeasurementNormalizer
import com.agxmeister.ember.domain.model.DailyCandle
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetDailyCandlesUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<List<DailyCandle>> =
        combine(
            measurementRepository.getAll(),
            preferencesRepository.dayStartHour,
            preferencesRepository.clusteringEnabled,
        ) { measurements, dayStartHour, clusteringEnabled ->
            val tz = TimeZone.currentSystemDefault()
            val byDate = measurements
                .groupBy { it.timestamp.toLocalDateTime(tz).date }
                .toSortedMap()
            val dates = byDate.keys.toList()

            if (dates.isEmpty()) return@combine emptyList()

            val normalizer = MeasurementNormalizer.build(measurements, dayStartHour, clusteringEnabled)

            dates.mapIndexed { index, date ->
                val dayMeasurements = byDate[date]!!.sortedBy { it.timestamp }
                val closeMs = normalizer.selectClose(dayMeasurements)
                val close = normalizer.normalize(closeMs)
                val rawClose = closeMs.weightKg

                val prevMeasurements = if (index > 0) byDate[dates[index - 1]]!!.sortedBy { it.timestamp } else null
                val open = if (prevMeasurements != null)
                    normalizer.normalize(normalizer.selectClose(prevMeasurements))
                else
                    normalizer.normalize(dayMeasurements.first())

                val allWeights = dayMeasurements.map { normalizer.normalize(it) }
                val high = maxOf(open, close, allWeights.max())
                val low = minOf(open, close, allWeights.min())

                val windowStart = date.minus(DatePeriod(days = 6))
                val windowWeights = measurements
                    .filter { it.timestamp.toLocalDateTime(tz).date in windowStart..date }
                    .map { normalizer.normalize(it) }
                    .sorted()

                DailyCandle(
                    date = date,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    rawClose = rawClose,
                    rollingMedian = windowWeights.median(),
                )
            }
        }
}

private fun List<Double>.median(): Double {
    val s = sorted()
    return if (s.size % 2 == 0) (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0 else s[s.size / 2]
}
