package com.agxmeister.ember.data.repository

import com.agxmeister.ember.data.local.preferences.UserPreferencesDataStore
import com.agxmeister.ember.domain.model.AlgorithmConfig
import com.agxmeister.ember.domain.model.Language
import com.agxmeister.ember.domain.model.ThemeMode
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    override val notificationsEnabled: Flow<Boolean> = dataStore.notificationsEnabled
    override val clusteringEnabled: Flow<Boolean> = dataStore.clusteringEnabled
    override val helpIconsVisible: Flow<Boolean> = dataStore.helpIconsVisible
    override val analyticsEnabled: Flow<Boolean> = dataStore.analyticsEnabled
    override val seenHelpKeys: Flow<Set<String>> = dataStore.seenHelpKeys
    override val weightGoal: Flow<WeightGoal> = dataStore.weightGoal
    override val weightUnit: Flow<WeightUnit> = dataStore.weightUnit
    override val weighingFrequency: Flow<WeighingFrequency> = dataStore.weighingFrequency
    override val notificationDayOfWeek: Flow<Int> = dataStore.notificationDayOfWeek
    override val goalTargetKg: Flow<Double> = dataStore.goalTargetKg
    override val goalStartDate: Flow<String> = dataStore.goalStartDate
    override val themeMode: Flow<ThemeMode> = dataStore.themeMode
    override val language: Flow<Language> = dataStore.language
    override val algorithmConfig: Flow<AlgorithmConfig> = combine(
        dataStore.regressionIntervalDays,
        dataStore.minClusterSize,
        dataStore.streakWindow,
        dataStore.scoreWindow,
        dataStore.volatilityWindow,
        dataStore.staleCutoffPeriods,
        dataStore.maxGapDays,
    ) { values ->
        AlgorithmConfig(values[0], values[1], values[2], values[3], values[4], values[5], values[6])
    }

    override suspend fun saveOnboardingData(
        weightKg: Double,
        goalTargetKg: Double,
        goalStartDate: String,
        dayStartHour: Int,
        dayStartMinute: Int,
        notificationHour: Int,
        notificationMinute: Int,
        clusteringEnabled: Boolean,
        weightGoal: WeightGoal,
        weightUnit: WeightUnit,
        weighingFrequency: WeighingFrequency,
        notificationDayOfWeek: Int,
    ) {
        dataStore.saveOnboardingData(
            weightKg, goalTargetKg, goalStartDate, dayStartHour, dayStartMinute,
            notificationHour, notificationMinute,
            clusteringEnabled, weightGoal, weightUnit,
            weighingFrequency, notificationDayOfWeek,
        )
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.setNotificationsEnabled(enabled)
    }

    override suspend fun setClusteringEnabled(enabled: Boolean) {
        dataStore.setClusteringEnabled(enabled)
    }

    override suspend fun setHelpIconsVisible(visible: Boolean) {
        dataStore.setHelpIconsVisible(visible)
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.setAnalyticsEnabled(enabled)
    }

    override suspend fun markHelpSeen(key: String) {
        dataStore.markHelpSeen(key)
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

    override suspend fun setWeighingFrequency(frequency: WeighingFrequency) {
        dataStore.setWeighingFrequency(frequency)
    }

    override suspend fun setNotificationDayOfWeek(dayOfWeek: Int) {
        dataStore.setNotificationDayOfWeek(dayOfWeek)
    }

    override suspend fun setGoalTargetKg(targetKg: Double) {
        dataStore.setGoalTargetKg(targetKg)
    }

    override suspend fun setInitialWeightKg(weightKg: Double) {
        dataStore.setInitialWeightKg(weightKg)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.setThemeMode(mode)
    }

    override suspend fun setLanguage(language: Language) {
        dataStore.setLanguage(language)
    }

    override suspend fun setGoalStartDate(date: String) {
        dataStore.setGoalStartDate(date)
    }

    override suspend fun setAlgorithmConfig(config: AlgorithmConfig) {
        dataStore.setRegressionIntervalDays(config.regressionIntervalDays)
        dataStore.setMinClusterSize(config.minClusterSize)
        dataStore.setStreakWindow(config.streakWindow)
        dataStore.setScoreWindow(config.scoreWindow)
        dataStore.setVolatilityWindow(config.volatilityWindow)
        dataStore.setStaleCutoffPeriods(config.staleCutoffPeriods)
        dataStore.setMaxGapDays(config.maxGapDays)
    }

    override suspend fun resetOnboarding() {
        dataStore.resetOnboarding()
    }
}
