package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import javax.inject.Inject

class SetNotificationTimeUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        preferencesRepository.setNotificationTime(hour, minute)
        reminderScheduler.scheduleForTime(hour, minute)
    }
}
