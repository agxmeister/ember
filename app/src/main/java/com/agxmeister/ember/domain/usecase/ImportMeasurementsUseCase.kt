package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class ImportMeasurementsUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    suspend operator fun invoke(values: List<Double>, weightUnit: WeightUnit) {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        values.reversed().forEachIndexed { daysAgo, value ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val dayStartMs = date.atStartOfDayIn(tz).toEpochMilliseconds()
            measurementRepository.add(
                Measurement(
                    weightKg = weightUnit.toKg(value),
                    timestamp = Instant.fromEpochMilliseconds(dayStartMs + 12 * 3600 * 1000L),
                )
            )
        }
    }
}
