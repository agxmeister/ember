package com.agxmeister.ember.domain.model

import kotlinx.datetime.LocalDate

data class WeeklyData(
    val weekStart: LocalDate,
    val median: Double,
)
