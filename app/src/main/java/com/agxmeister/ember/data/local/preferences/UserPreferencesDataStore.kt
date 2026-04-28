package com.agxmeister.ember.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val initialWeightKey = doublePreferencesKey("initial_weight_kg")
    private val dayStartHourKey = intPreferencesKey("day_start_hour")
    private val dayStartMinuteKey = intPreferencesKey("day_start_minute")
    private val notificationHourKey = intPreferencesKey("notification_hour")
    private val notificationMinuteKey = intPreferencesKey("notification_minute")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val clusteringEnabledKey = booleanPreferencesKey("clustering_enabled")
    private val weightGoalKey = stringPreferencesKey("weight_goal")
    private val weightUnitKey = stringPreferencesKey("weight_unit")

    val isOnboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { it[onboardingCompletedKey] ?: false }

    val initialWeightKg: Flow<Double> =
        context.dataStore.data.map { it[initialWeightKey] ?: 70.0 }

    val dayStartHour: Flow<Int> =
        context.dataStore.data.map { it[dayStartHourKey] ?: 8 }

    val dayStartMinute: Flow<Int> =
        context.dataStore.data.map { it[dayStartMinuteKey] ?: 0 }

    val notificationHour: Flow<Int> =
        context.dataStore.data.map { it[notificationHourKey] ?: 8 }

    val notificationMinute: Flow<Int> =
        context.dataStore.data.map { it[notificationMinuteKey] ?: 15 }

    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[notificationsEnabledKey] ?: true }

    val clusteringEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[clusteringEnabledKey] ?: true }

    val weightGoal: Flow<WeightGoal> =
        context.dataStore.data.map { prefs ->
            when (prefs[weightGoalKey]) {
                WeightGoal.Increase.name -> WeightGoal.Increase
                else -> WeightGoal.Decrease
            }
        }

    val weightUnit: Flow<WeightUnit> =
        context.dataStore.data.map { prefs ->
            when (prefs[weightUnitKey]) {
                WeightUnit.Lbs.name -> WeightUnit.Lbs
                else -> WeightUnit.Kg
            }
        }

    suspend fun saveOnboardingData(
        weightKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        notificationHour: Int,
        notificationMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
        weightUnit: WeightUnit,
    ) {
        context.dataStore.edit { prefs ->
            prefs[initialWeightKey] = weightKg
            prefs[dayStartHourKey] = dayStartHour
            prefs[dayStartMinuteKey] = dayStartMinute
            prefs[notificationHourKey] = notificationHour
            prefs[notificationMinuteKey] = notificationMinute
            prefs[clusteringEnabledKey] = clusteringEnabled
            prefs[weightGoalKey] = weightGoal.name
            prefs[weightUnitKey] = weightUnit.name
            prefs[onboardingCompletedKey] = true
        }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[notificationHourKey] = hour
            prefs[notificationMinuteKey] = minute
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[notificationsEnabledKey] = enabled
        }
    }

    suspend fun setClusteringEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[clusteringEnabledKey] = enabled
        }
    }

    suspend fun setWeightGoal(goal: WeightGoal) {
        context.dataStore.edit { prefs ->
            prefs[weightGoalKey] = goal.name
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { prefs ->
            prefs[weightUnitKey] = unit.name
        }
    }
}
