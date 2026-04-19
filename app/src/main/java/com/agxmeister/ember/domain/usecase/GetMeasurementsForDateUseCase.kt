package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import javax.inject.Inject

class GetMeasurementsForDateUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    operator fun invoke(date: LocalDate): Flow<List<Measurement>> {
        val tz = TimeZone.currentSystemDefault()
        val fromMs = date.atStartOfDayIn(tz).toEpochMilliseconds()
        val toMs = date.plus(DatePeriod(days = 1)).atStartOfDayIn(tz).toEpochMilliseconds()
        return repository.getForDateRange(fromMs, toMs)
    }
}
