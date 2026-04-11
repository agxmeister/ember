package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetClusterTrendsUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<List<Cluster>> =
        combine(
            measurementRepository.getAll(),
            preferencesRepository.dayStartHour,
        ) { measurements, dayStartHour ->
            ClusteringAlgorithm.cluster(measurements, dayStartHour)
        }
}
