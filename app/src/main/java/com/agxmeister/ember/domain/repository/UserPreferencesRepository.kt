package com.agxmeister.ember.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    val initialWeightKg: Flow<Double>
    suspend fun saveOnboardingData(weightKg: Double, dayStartHour: Int, dayStartMinute: Int)
}
