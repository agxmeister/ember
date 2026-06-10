package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.NormalizedMeasurementsProvider
import com.agxmeister.ember.domain.model.WeeklyData
import com.agxmeister.ember.domain.util.median
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetWeeklyDataUseCase @Inject constructor(
    private val normalizedMeasurements: NormalizedMeasurementsProvider,
) {
    operator fun invoke(): Flow<List<WeeklyData>> =
        normalizedMeasurements().map { (measurements, normalizer) ->
            measurements
                .groupBy { it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.isoWeekStart() }
                .toSortedMap()
                .map { (weekStart, weekMeasurements) ->
                    WeeklyData(
                        weekStart = weekStart,
                        median = weekMeasurements.map { normalizer.normalize(it) }.median(),
                    )
                }
        }
}

internal fun LocalDate.isoWeekStart(): LocalDate = minus(DatePeriod(days = dayOfWeek.value - 1))
