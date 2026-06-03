package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.model.AlgorithmConfig
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetAlgorithmConfigUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(config: AlgorithmConfig) {
        preferencesRepository.setAlgorithmConfig(config)
    }
}
