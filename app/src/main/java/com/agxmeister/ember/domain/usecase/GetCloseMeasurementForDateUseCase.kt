package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.NormalizedMeasurementsProvider
import com.agxmeister.ember.domain.model.Measurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetCloseMeasurementForDateUseCase @Inject constructor(
    private val normalizedMeasurements: NormalizedMeasurementsProvider,
) {
    operator fun invoke(date: LocalDate): Flow<Measurement?> =
        normalizedMeasurements().map { (measurements, normalizer) ->
            val tz = TimeZone.currentSystemDefault()
            val dayMeasurements = measurements
                .filter { it.timestamp.toLocalDateTime(tz).date == date }
                .sortedBy { it.timestamp }
            if (dayMeasurements.isEmpty()) null
            else normalizer.selectClose(dayMeasurements)
        }
}
