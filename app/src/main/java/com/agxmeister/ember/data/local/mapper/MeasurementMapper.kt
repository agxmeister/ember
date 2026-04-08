package com.agxmeister.ember.data.local.mapper

import com.agxmeister.ember.data.local.entity.MeasurementEntity
import com.agxmeister.ember.domain.model.Measurement
import kotlinx.datetime.Instant

fun MeasurementEntity.toDomain() = Measurement(
    id = id,
    weightKg = weightKg,
    timestamp = Instant.fromEpochMilliseconds(timestamp),
)

fun Measurement.toEntity() = MeasurementEntity(
    id = id,
    weightKg = weightKg,
    timestamp = timestamp.toEpochMilliseconds(),
)
