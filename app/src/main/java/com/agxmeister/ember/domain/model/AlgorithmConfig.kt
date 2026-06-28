package com.agxmeister.ember.domain.model

data class AlgorithmConfig(
    val regressionIntervalDays: Int = 28,
    val minClusterSize: Int = 14,
    val streakTrendWindow: Int = 14,
    val scoreWindow: Int = 14,
    val volatilityWindow: Int = 14,
    val trendStalePeriods: Int = 7,
)
