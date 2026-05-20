package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class ResetDataUseCase @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke() {
        measurementRepository.deleteAll()
        preferencesRepository.resetOnboarding()
    }
}
