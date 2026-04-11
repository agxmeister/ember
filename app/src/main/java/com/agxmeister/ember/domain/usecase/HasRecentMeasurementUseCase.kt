package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class HasRecentMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.getAll().map { measurements ->
            val windowStart = Clock.System.now().minus(15.minutes).toEpochMilliseconds()
            measurements.any { it.timestamp.toEpochMilliseconds() >= windowStart }
        }
    }
}
