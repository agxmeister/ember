package com.agxmeister.ember.domain.model

import kotlinx.datetime.LocalDate

data class DailyAverage(
    val date: LocalDate,
    val weightKg: Double,
)
