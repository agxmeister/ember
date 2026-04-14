package com.agxmeister.ember.domain.repository

import com.agxmeister.ember.domain.model.WeightGoal
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    val initialWeightKg: Flow<Double>
    val dayStartHour: Flow<Int>
    val clusteringEnabled: Flow<Boolean>
    val weightGoal: Flow<WeightGoal>
    suspend fun saveOnboardingData(
        weightKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
    )
    suspend fun setClusteringEnabled(enabled: Boolean)
    suspend fun setWeightGoal(goal: WeightGoal)
}
