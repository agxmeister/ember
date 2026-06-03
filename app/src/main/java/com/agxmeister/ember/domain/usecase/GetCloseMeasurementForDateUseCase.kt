package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.MeasurementNormalizer
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetCloseMeasurementForDateUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(date: LocalDate): Flow<Measurement?> =
        combine(
            measurementRepository.getAll(),
            preferencesRepository.dayStartHour,
            preferencesRepository.clusteringEnabled,
            preferencesRepository.algorithmConfig,
        ) { allMeasurements, dayStartHour, clusteringEnabled, config ->
            val tz = TimeZone.currentSystemDefault()
            val dayMeasurements = allMeasurements
                .filter { it.timestamp.toLocalDateTime(tz).date == date }
                .sortedBy { it.timestamp }
            if (dayMeasurements.isEmpty()) return@combine null
            val normalizer = MeasurementNormalizer.build(allMeasurements, dayStartHour, clusteringEnabled, config.minClusterSize)
            normalizer.selectClose(dayMeasurements)
        }
}
