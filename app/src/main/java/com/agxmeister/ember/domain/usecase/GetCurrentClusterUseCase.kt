package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.model.Cluster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetCurrentClusterUseCase @Inject constructor(
    private val getClusterTrends: GetClusterTrendsUseCase,
) {
    operator fun invoke(): Flow<Cluster?> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentMinuteOfDay = now.hour * 60 + now.minute
        return getClusterTrends().map { clusters ->
            ClusteringAlgorithm.nearestCluster(clusters, currentMinuteOfDay)
        }
    }
}
