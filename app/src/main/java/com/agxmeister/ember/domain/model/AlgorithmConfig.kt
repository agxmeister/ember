package com.agxmeister.ember.domain.model

data class AlgorithmConfig(
    val regressionIntervalDays: Int = 28,
    val minClusterSize: Int = 14,
    val streakWindow: Int = 14,
    val scoreWindow: Int = 14,
    val volatilityWindow: Int = 14,
    val trendStalePeriods: Int = 7,
)
