package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
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
        weighingFrequency: WeighingFrequency,
        notificationDayOfWeek: Int,
        notificationHour: Int,
        notificationMinute: Int,
    ) {
        val (finalHour, finalMinute) = if (weighingFrequency == WeighingFrequency.Daily) {
            val totalMinutes = (dayStartHour * 60 + dayStartMinute + 15) % (24 * 60)
            totalMinutes / 60 to totalMinutes % 60
        } else {
            notificationHour to notificationMinute
        }
        preferencesRepository.saveOnboardingData(
            weightKg, dayStartHour, dayStartMinute,
            finalHour, finalMinute,
            clusteringEnabled, weightGoal, weightUnit,
            weighingFrequency, notificationDayOfWeek,
        )
        if (weighingFrequency == WeighingFrequency.Daily) {
            reminderScheduler.scheduleForTime(finalHour, finalMinute)
        } else {
            reminderScheduler.scheduleWeeklyForTime(notificationDayOfWeek, finalHour, finalMinute)
        }
    }
}
