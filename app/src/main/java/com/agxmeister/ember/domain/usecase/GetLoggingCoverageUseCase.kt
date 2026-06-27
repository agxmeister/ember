package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

private const val WINDOW = 14

/** Percentage (0–100) of the last [WINDOW] periods (days or weeks) that have at least one measurement. */
class GetLoggingCoverageUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
) {
    suspend operator fun invoke(frequency: WeighingFrequency): Int {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val windowStart = when (frequency) {
            WeighingFrequency.Daily -> today.minus(DatePeriod(days = WINDOW - 1))
            WeighingFrequency.Weekly -> today.isoWeekStart().minus(DatePeriod(days = (WINDOW - 1) * 7))
        }
        val fromMs = windowStart.atStartOfDayIn(tz).toEpochMilliseconds()
        val coveredPeriods = measurementRepository.getSince(fromMs)
            .map { measurement ->
                val date = measurement.timestamp.toLocalDateTime(tz).date
                if (frequency == WeighingFrequency.Weekly) date.isoWeekStart() else date
            }
            .toSet()
            .size
        return coveredPeriods * 100 / WINDOW
    }
}
