package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetWeighingFrequencyUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(frequency: WeighingFrequency) {
        preferencesRepository.setWeighingFrequency(frequency)
        val notificationsEnabled = preferencesRepository.notificationsEnabled.first()
        if (!notificationsEnabled) return
        val hour = preferencesRepository.notificationHour.first()
        val minute = preferencesRepository.notificationMinute.first()
        val dayOfWeek = preferencesRepository.notificationDayOfWeek.first()
        reminderScheduler.scheduleForFrequency(frequency, dayOfWeek, hour, minute)
    }
}
