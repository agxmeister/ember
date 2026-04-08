package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

class AddMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    suspend operator fun invoke(weightKg: Double) {
        repository.add(
            Measurement(
                weightKg = weightKg,
                timestamp = Clock.System.now(),
            )
        )
    }
}
