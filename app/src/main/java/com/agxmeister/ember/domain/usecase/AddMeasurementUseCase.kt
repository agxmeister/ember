package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class AddMeasurementUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(weightKg: Double) {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(tz)
        val today = nowLocal.date
        val minuteOfDay = nowLocal.hour * 60 + nowLocal.minute
        val dayStartHour = preferencesRepository.dayStartHour.first()
        val currentCluster = ClusteringAlgorithm.assign(minuteOfDay, dayStartHour)

        val todayStartMs = today.atStartOfDayIn(tz).toEpochMilliseconds()
        measurementRepository.getSince(todayStartMs).forEach { m ->
            val mLocal = m.timestamp.toLocalDateTime(tz)
            val mCluster = ClusteringAlgorithm.assign(mLocal.hour * 60 + mLocal.minute, dayStartHour)
            if (mCluster == currentCluster) {
                measurementRepository.delete(m.id)
            }
        }

        measurementRepository.add(Measurement(weightKg = weightKg, timestamp = now))
    }
}
