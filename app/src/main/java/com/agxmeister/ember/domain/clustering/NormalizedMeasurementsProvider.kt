package com.agxmeister.ember.domain.clustering

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** The current measurements paired with a [MeasurementNormalizer] built from user preferences. */
data class NormalizedMeasurements(
    val measurements: List<Measurement>,
    val normalizer: MeasurementNormalizer,
)

/** Emits [NormalizedMeasurements] whenever measurements or normalization preferences change. */
class NormalizedMeasurementsProvider @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<NormalizedMeasurements> =
        combine(
            measurementRepository.getAll(),
            preferencesRepository.dayStartHour,
            preferencesRepository.clusteringEnabled,
            preferencesRepository.algorithmConfig,
        ) { measurements, dayStartHour, clusteringEnabled, config ->
            NormalizedMeasurements(
                measurements = measurements,
                normalizer = MeasurementNormalizer.build(measurements, dayStartHour, clusteringEnabled, config.minClusterSize),
            )
        }
}
