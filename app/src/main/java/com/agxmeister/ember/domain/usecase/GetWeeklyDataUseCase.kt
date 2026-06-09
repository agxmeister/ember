package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.MeasurementNormalizer
import com.agxmeister.ember.domain.model.WeeklyData
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.util.median
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetWeeklyDataUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<List<WeeklyData>> =
        combine(
            measurementRepository.getAll(),
            preferencesRepository.dayStartHour,
            preferencesRepository.clusteringEnabled,
            preferencesRepository.algorithmConfig,
        ) { measurements, dayStartHour, clusteringEnabled, config ->
            val normalizer = MeasurementNormalizer.build(measurements, dayStartHour, clusteringEnabled, config.minClusterSize)
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
