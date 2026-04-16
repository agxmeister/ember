package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetWeightUnitUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(unit: WeightUnit) {
        preferencesRepository.setWeightUnit(unit)
    }
}
