package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class AddMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    suspend operator fun invoke(weightKg: Double) {
        val now = Clock.System.now()
        val windowStart = now.minus(15.minutes).toEpochMilliseconds()
        repository.getSince(windowStart).forEach { repository.delete(it.id) }
        repository.add(Measurement(weightKg = weightKg, timestamp = now))
    }
}
