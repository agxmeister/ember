package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMeasurementsUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    operator fun invoke(): Flow<List<Measurement>> = repository.getAll()
}
