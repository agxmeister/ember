package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.datetime.Instant
import javax.inject.Inject

class SaveMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    suspend operator fun invoke(id: Long, weightKg: Double, timestamp: Instant) {
        val measurement = Measurement(id = id, weightKg = weightKg, timestamp = timestamp)
        if (id == 0L) repository.add(measurement) else repository.update(measurement)
    }
}
