package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetMeasurementDatesUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    operator fun invoke(): Flow<Set<LocalDate>> =
        measurementRepository.getAll().map { measurements ->
            measurements.mapTo(mutableSetOf()) { m ->
                m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
        }
}
