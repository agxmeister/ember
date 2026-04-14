package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
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
    ) {
        preferencesRepository.saveOnboardingData(weightKg, dayStartHour, dayStartMinute, clusteringEnabled, weightGoal)
        reminderScheduler.scheduleForTime(dayStartHour, dayStartMinute)
    }
}
