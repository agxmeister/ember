package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.DailyCandle
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetDailyCandlesUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    operator fun invoke(): Flow<List<DailyCandle>> =
        measurementRepository.getAll().map { measurements ->
            val byDate = measurements
                .groupBy { it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date }
                .toSortedMap()

            val dates = byDate.keys.toList()

            dates.mapIndexed { index, date ->
                val dayMeasurements = byDate[date]!!.sortedBy { it.timestamp }
                val close = dayMeasurements.last().weightKg

                // open = previous day's close so the body shows the day-over-day change.
                // For the first day, fall back to today's first measurement.
                val prevClose = if (index > 0) byDate[dates[index - 1]]!!.maxByOrNull { it.timestamp }!!.weightKg
                                else dayMeasurements.first().weightKg
                val open = prevClose

                val high = maxOf(open, close, dayMeasurements.maxOf { it.weightKg })
                val low = minOf(open, close, dayMeasurements.minOf { it.weightKg })

                val windowStart = date.minus(DatePeriod(days = 6))
                val windowWeights = measurements
                    .filter {
                        val d = it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        d in windowStart..date
                    }
                    .map { it.weightKg }
                    .sorted()

                DailyCandle(
                    date = date,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    rollingMedian = windowWeights.median(),
                )
            }
        }
}

private fun List<Double>.median(): Double {
    val s = sorted()
    return if (s.size % 2 == 0) (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0 else s[s.size / 2]
}
