package com.agxmeister.ember.domain.model

import kotlinx.datetime.LocalDate

data class DailyCandle(
    val date: LocalDate,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val rollingMedian: Double,
)
