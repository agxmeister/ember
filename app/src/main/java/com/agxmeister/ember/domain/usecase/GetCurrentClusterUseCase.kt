package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetCurrentClusterUseCase @Inject constructor(
    private val getClusterTrends: GetClusterTrendsUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Cluster> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val minuteOfDay = now.hour * 60 + now.minute
        return combine(
            getClusterTrends(),
            preferencesRepository.dayStartHour,
        ) { clusters, dayStartHour ->
            val currentSlot = ClusteringAlgorithm.assign(minuteOfDay, dayStartHour)
            clusters.first { it.dayCluster == currentSlot }
        }
    }
}
