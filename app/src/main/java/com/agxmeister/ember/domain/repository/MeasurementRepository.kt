package com.agxmeister.ember.domain.repository

import com.agxmeister.ember.domain.model.Measurement
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun getAll(): Flow<List<Measurement>>
    suspend fun add(measurement: Measurement)
    suspend fun delete(id: Long)
}
