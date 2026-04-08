package com.agxmeister.ember.data.repository

import com.agxmeister.ember.data.local.dao.MeasurementDao
import com.agxmeister.ember.data.local.mapper.toDomain
import com.agxmeister.ember.data.local.mapper.toEntity
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MeasurementRepositoryImpl @Inject constructor(
    private val dao: MeasurementDao,
) : MeasurementRepository {

    override fun getAll(): Flow<List<Measurement>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(measurement: Measurement) {
        dao.insert(measurement.toEntity())
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}
