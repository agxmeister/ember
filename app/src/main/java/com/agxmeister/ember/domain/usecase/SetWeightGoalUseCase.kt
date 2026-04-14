package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetWeightGoalUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(goal: WeightGoal) {
        preferencesRepository.setWeightGoal(goal)
    }
}
