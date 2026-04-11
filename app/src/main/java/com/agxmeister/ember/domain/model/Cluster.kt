package com.agxmeister.ember.domain.model

data class Cluster(
    val dayCluster: DayCluster,
    val measurements: List<Measurement>,
) {
    val label: String get() = dayCluster.label
}
