package com.agxmeister.ember.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    val initialWeightKg: Flow<Double>
    val dayStartHour: Flow<Int>
    val clusteringEnabled: Flow<Boolean>
    suspend fun saveOnboardingData(weightKg: Double, dayStartHour: Int, dayStartMinute: Int, clusteringEnabled: Boolean)
    suspend fun setClusteringEnabled(enabled: Boolean)
}
