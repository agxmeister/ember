package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeeklyData
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetWeeklyDataUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    operator fun invoke(): Flow<List<WeeklyData>> =
        measurementRepository.getAll().map { measurements ->
            measurements
                .groupBy { it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.isoWeekStart() }
                .toSortedMap()
                .map { (weekStart, weekMeasurements) ->
                    WeeklyData(weekStart = weekStart, median = weekMeasurements.map { it.weightKg }.median())
                }
        }
}

internal fun LocalDate.isoWeekStart(): LocalDate = minus(DatePeriod(days = dayOfWeek.value - 1))

private fun List<Double>.median(): Double {
    val s = sorted()
    return if (s.size % 2 == 0) (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0 else s[s.size / 2]
}
