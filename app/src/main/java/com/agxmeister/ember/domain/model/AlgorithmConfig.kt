package com.agxmeister.ember.domain.model

data class AlgorithmConfig(
    val regressionIntervalDays: Int = 28,
    val minClusterSize: Int = 14,
)
