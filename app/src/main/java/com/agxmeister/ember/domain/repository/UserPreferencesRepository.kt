package com.agxmeister.ember.domain.repository

import com.agxmeister.ember.domain.model.Language
import com.agxmeister.ember.domain.model.ThemeMode
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
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
    val weighingFrequency: Flow<WeighingFrequency>
    val notificationDayOfWeek: Flow<Int>
    val goalTargetKg: Flow<Double>
    val themeMode: Flow<ThemeMode>
    val language: Flow<Language>
    suspend fun saveOnboardingData(
        weightKg: Double,
        goalTargetKg: Double,
        dayStartHour: Int,
        dayStartMinute: Int,
        notificationHour: Int,
        notificationMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
        weightUnit: WeightUnit,
        weighingFrequency: WeighingFrequency,
        notificationDayOfWeek: Int,
    )
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setClusteringEnabled(enabled: Boolean)
    suspend fun setWeightGoal(goal: WeightGoal)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setNotificationTime(hour: Int, minute: Int)
    suspend fun setWeighingFrequency(frequency: WeighingFrequency)
    suspend fun setNotificationDayOfWeek(dayOfWeek: Int)
    suspend fun setGoalTargetKg(targetKg: Double)
    suspend fun setInitialWeightKg(weightKg: Double)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: Language)
}
