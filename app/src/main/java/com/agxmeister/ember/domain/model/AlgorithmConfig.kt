package com.agxmeister.ember.domain.model

data class AlgorithmConfig(
    val regressionWindow: Int = 28,
    val minClusterSize: Int = 14,
    val streakWindow: Int = 14,
    val scoreWindow: Int = 14,
    val volatilityWindow: Int = 14,
    val minMeasuredForVolatility: Int = 4,
    val staleCutoffPeriods: Int = 7,
    val maxGapDays: Int = 7,
    val minMeasuredForRate: Int = 7,
    val minMeasuredForEta: Int = 14,
)
