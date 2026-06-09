package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetNotificationTimeUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        preferencesRepository.setNotificationTime(hour, minute)
        val frequency = preferencesRepository.weighingFrequency.first()
        val dayOfWeek = preferencesRepository.notificationDayOfWeek.first()
        reminderScheduler.scheduleForFrequency(frequency, dayOfWeek, hour, minute)
    }
}
