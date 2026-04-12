package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetClusteringEnabledUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesRepository.setClusteringEnabled(enabled)
    }
}
