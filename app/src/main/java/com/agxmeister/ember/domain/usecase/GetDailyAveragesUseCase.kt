package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.DailyAverage
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetDailyAveragesUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    operator fun invoke(): Flow<List<DailyAverage>> =
        measurementRepository.getAll().map { measurements ->
            measurements
                .groupBy { m -> m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date }
                .map { (date, list) -> DailyAverage(date, list.map { it.weightKg }.average()) }
                .sortedBy { it.date }
        }
}
