package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.notification.ReminderScheduler
import java.time.LocalDate
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(
        weightKg: Double,
        goalTargetKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        clusteringEnabled: Boolean,
        weightUnit: WeightUnit,
        weighingFrequency: WeighingFrequency,
        notificationDayOfWeek: Int,
        notificationHour: Int,
        notificationMinute: Int,
        goalStartDate: String = LocalDate.now().toString(),
    ) {
        val derivedGoal = if (goalTargetKg < weightKg) WeightGoal.Decrease else WeightGoal.Increase
        val (finalHour, finalMinute) = if (weighingFrequency == WeighingFrequency.Daily) {
            val totalMinutes = (dayStartHour * 60 + dayStartMinute + 15) % (24 * 60)
            totalMinutes / 60 to totalMinutes % 60
        } else {
            notificationHour to notificationMinute
        }
        preferencesRepository.saveOnboardingData(
            weightKg, goalTargetKg, goalStartDate,
            dayStartHour, dayStartMinute,
            finalHour, finalMinute,
            clusteringEnabled, derivedGoal, weightUnit,
            weighingFrequency, notificationDayOfWeek,
        )
        reminderScheduler.scheduleForFrequency(weighingFrequency, notificationDayOfWeek, finalHour, finalMinute)
    }
}
