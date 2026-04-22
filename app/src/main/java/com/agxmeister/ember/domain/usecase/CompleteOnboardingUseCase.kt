package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(
        weightKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
        weightUnit: WeightUnit,
    ) {
        val notifyTotalMinutes = (dayStartHour * 60 + dayStartMinute + 15) % (24 * 60)
        val notificationHour = notifyTotalMinutes / 60
        val notificationMinute = notifyTotalMinutes % 60
        preferencesRepository.saveOnboardingData(
            weightKg, dayStartHour, dayStartMinute,
            notificationHour, notificationMinute,
            clusteringEnabled, weightGoal, weightUnit,
        )
        reminderScheduler.scheduleForTime(notificationHour, notificationMinute)
    }
}
