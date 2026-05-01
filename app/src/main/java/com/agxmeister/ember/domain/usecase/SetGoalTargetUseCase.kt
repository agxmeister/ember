package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetGoalTargetUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(targetKg: Double) {
        val initialWeightKg = preferencesRepository.initialWeightKg.first()
        val derivedGoal = if (targetKg < initialWeightKg) WeightGoal.Decrease else WeightGoal.Increase
        preferencesRepository.setGoalTargetKg(targetKg)
        preferencesRepository.setWeightGoal(derivedGoal)
    }
}
