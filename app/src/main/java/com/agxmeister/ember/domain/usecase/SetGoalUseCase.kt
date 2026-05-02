package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetGoalUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(initialWeightKg: Double, targetKg: Double) {
        val derivedGoal = if (targetKg < initialWeightKg) WeightGoal.Decrease else WeightGoal.Increase
        preferencesRepository.setInitialWeightKg(initialWeightKg)
        preferencesRepository.setGoalTargetKg(targetKg)
        preferencesRepository.setWeightGoal(derivedGoal)
    }
}
