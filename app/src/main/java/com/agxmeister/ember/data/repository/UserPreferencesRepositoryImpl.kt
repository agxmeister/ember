package com.agxmeister.ember.data.repository

import com.agxmeister.ember.data.local.preferences.UserPreferencesDataStore
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) : UserPreferencesRepository {
    override val isOnboardingCompleted: Flow<Boolean> = dataStore.isOnboardingCompleted
    override val initialWeightKg: Flow<Double> = dataStore.initialWeightKg
    override val dayStartHour: Flow<Int> = dataStore.dayStartHour
    override val clusteringEnabled: Flow<Boolean> = dataStore.clusteringEnabled

    override suspend fun saveOnboardingData(weightKg: Double, dayStartHour: Int, dayStartMinute: Int, clusteringEnabled: Boolean) {
        dataStore.saveOnboardingData(weightKg, dayStartHour, dayStartMinute, clusteringEnabled)
    }

    override suspend fun setClusteringEnabled(enabled: Boolean) {
        dataStore.setClusteringEnabled(enabled)
    }
}
