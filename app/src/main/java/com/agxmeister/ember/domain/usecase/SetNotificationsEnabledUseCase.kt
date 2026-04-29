package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetNotificationsEnabledUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
        if (enabled) {
            val hour = preferencesRepository.notificationHour.first()
            val minute = preferencesRepository.notificationMinute.first()
            val frequency = preferencesRepository.weighingFrequency.first()
            if (frequency == WeighingFrequency.Weekly) {
                val dayOfWeek = preferencesRepository.notificationDayOfWeek.first()
                reminderScheduler.scheduleWeeklyForTime(dayOfWeek, hour, minute)
            } else {
                reminderScheduler.scheduleForTime(hour, minute)
            }
        } else {
            reminderScheduler.cancelScheduled()
        }
    }
}
