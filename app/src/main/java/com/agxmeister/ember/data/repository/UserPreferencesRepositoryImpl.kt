package com.agxmeister.ember.data.repository

import com.agxmeister.ember.data.local.preferences.UserPreferencesDataStore
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) : UserPreferencesRepository {
    override val isOnboardingCompleted: Flow<Boolean> = dataStore.isOnboardingCompleted
    override val initialWeightKg: Flow<Double> = dataStore.initialWeightKg
    override val dayStartHour: Flow<Int> = dataStore.dayStartHour
    override val dayStartMinute: Flow<Int> = dataStore.dayStartMinute
    override val notificationHour: Flow<Int> = dataStore.notificationHour
    override val notificationMinute: Flow<Int> = dataStore.notificationMinute
    override val clusteringEnabled: Flow<Boolean> = dataStore.clusteringEnabled
    override val weightGoal: Flow<WeightGoal> = dataStore.weightGoal
    override val weightUnit: Flow<WeightUnit> = dataStore.weightUnit

    override suspend fun saveOnboardingData(
        weightKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        notificationHour: Int,
        notificationMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
        weightUnit: WeightUnit,
    ) {
        dataStore.saveOnboardingData(weightKg, dayStartHour, dayStartMinute, notificationHour, notificationMinute, clusteringEnabled, weightGoal, weightUnit)
    }

    override suspend fun setClusteringEnabled(enabled: Boolean) {
        dataStore.setClusteringEnabled(enabled)
    }

    override suspend fun setWeightGoal(goal: WeightGoal) {
        dataStore.setWeightGoal(goal)
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.setWeightUnit(unit)
    }

    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        dataStore.setNotificationTime(hour, minute)
    }
}
