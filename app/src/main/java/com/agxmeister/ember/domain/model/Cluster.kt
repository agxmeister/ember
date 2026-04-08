package com.agxmeister.ember.domain.model

data class Cluster(
    val label: String,
    val medianMinuteOfDay: Int,
    val measurements: List<Measurement>,
)
