package com.agxmeister.ember.domain.repository

import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    val initialWeightKg: Flow<Double>
    val dayStartHour: Flow<Int>
    val dayStartMinute: Flow<Int>
    val notificationHour: Flow<Int>
    val notificationMinute: Flow<Int>
    val notificationsEnabled: Flow<Boolean>
    val clusteringEnabled: Flow<Boolean>
    val weightGoal: Flow<WeightGoal>
    val weightUnit: Flow<WeightUnit>
    suspend fun saveOnboardingData(
        weightKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        notificationHour: Int,
        notificationMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
        weightUnit: WeightUnit,
    )
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setClusteringEnabled(enabled: Boolean)
    suspend fun setWeightGoal(goal: WeightGoal)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setNotificationTime(hour: Int, minute: Int)
}
