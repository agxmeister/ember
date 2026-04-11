package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class HasRecentMeasurementUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)
        val today = now.date
        val minuteOfDay = now.hour * 60 + now.minute
        return combine(
            measurementRepository.getAll(),
            preferencesRepository.dayStartHour,
        ) { measurements, dayStartHour ->
            val currentCluster = ClusteringAlgorithm.assign(minuteOfDay, dayStartHour)
            measurements.any { m ->
                val mLocal = m.timestamp.toLocalDateTime(tz)
                val mCluster = ClusteringAlgorithm.assign(mLocal.hour * 60 + mLocal.minute, dayStartHour)
                mLocal.date == today && mCluster == currentCluster
            }
        }
    }
}
