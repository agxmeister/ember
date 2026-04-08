package com.agxmeister.ember.domain.model

import kotlinx.datetime.Instant

data class Measurement(
    val id: Long = 0,
    val weightKg: Double,
    val timestamp: Instant,
)
