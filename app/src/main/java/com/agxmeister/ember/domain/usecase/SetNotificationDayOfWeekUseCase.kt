package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetNotificationDayOfWeekUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(dayOfWeek: Int) {
        preferencesRepository.setNotificationDayOfWeek(dayOfWeek)
        val hour = preferencesRepository.notificationHour.first()
        val minute = preferencesRepository.notificationMinute.first()
        reminderScheduler.scheduleWeeklyForTime(dayOfWeek, hour, minute)
    }
}
