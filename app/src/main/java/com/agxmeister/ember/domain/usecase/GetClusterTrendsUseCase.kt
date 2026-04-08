package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetClusterTrendsUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    operator fun invoke(): Flow<List<Cluster>> =
        repository.getAll().map { measurements ->
            ClusteringAlgorithm.cluster(measurements)
        }
}
