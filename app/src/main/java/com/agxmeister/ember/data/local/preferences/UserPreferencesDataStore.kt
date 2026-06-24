package com.agxmeister.ember.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.agxmeister.ember.domain.model.Language
import com.agxmeister.ember.domain.model.ThemeMode
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val initialWeightKey = doublePreferencesKey("initial_weight_kg")
    private val goalTargetKgKey = doublePreferencesKey("goal_target_kg")
    private val dayStartHourKey = intPreferencesKey("day_start_hour")
    private val dayStartMinuteKey = intPreferencesKey("day_start_minute")
    private val notificationHourKey = intPreferencesKey("notification_hour")
    private val notificationMinuteKey = intPreferencesKey("notification_minute")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val clusteringEnabledKey = booleanPreferencesKey("clustering_enabled")
    private val helpIconsVisibleKey = booleanPreferencesKey("help_icons_visible")
    private val analyticsEnabledKey = booleanPreferencesKey("analytics_enabled")
    private val seenHelpKeysKey = stringSetPreferencesKey("seen_help_keys")
    private val weightGoalKey = stringPreferencesKey("weight_goal")
    private val weightUnitKey = stringPreferencesKey("weight_unit")
    private val weighingFrequencyKey = stringPreferencesKey("weighing_frequency")
    private val notificationDayOfWeekKey = intPreferencesKey("notification_day_of_week")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language_explicit")
    private val goalStartDateKey = stringPreferencesKey("goal_start_date")
    private val regressionIntervalDaysKey = intPreferencesKey("regression_interval_days")
    private val minClusterSizeKey = intPreferencesKey("min_cluster_size")
    private val streakTrendWindowKey = intPreferencesKey("streak_trend_window")
    private val scoreWindowKey = intPreferencesKey("score_window")
    private val volatilityWindowKey = intPreferencesKey("volatility_window")

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

    val helpIconsVisible: Flow<Boolean> =
        context.dataStore.data.map { it[helpIconsVisibleKey] ?: true }

    val analyticsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[analyticsEnabledKey] ?: true }

    val seenHelpKeys: Flow<Set<String>> =
        context.dataStore.data.map { it[seenHelpKeysKey] ?: emptySet() }

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

    val weighingFrequency: Flow<WeighingFrequency> =
        context.dataStore.data.map { prefs ->
            when (prefs[weighingFrequencyKey]) {
                WeighingFrequency.Weekly.name -> WeighingFrequency.Weekly
                else -> WeighingFrequency.Daily
            }
        }

    val notificationDayOfWeek: Flow<Int> =
        context.dataStore.data.map { it[notificationDayOfWeekKey] ?: 1 }

    val goalTargetKg: Flow<Double> =
        context.dataStore.data.map { it[goalTargetKgKey] ?: 0.0 }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeModeKey]) {
            ThemeMode.Light.name -> ThemeMode.Light
            ThemeMode.Dark.name -> ThemeMode.Dark
            else -> ThemeMode.Auto
        }
    }

    val language: Flow<Language> = context.dataStore.data.map { prefs ->
        val saved = prefs[languageKey]
        if (saved != null) {
            Language.entries.find { it.name == saved } ?: Language.En
        } else {
            val systemCode = Locale.getDefault().language
            Language.entries.find { it.code == systemCode } ?: Language.En
        }
    }

    val goalStartDate: Flow<String> =
        context.dataStore.data.map { it[goalStartDateKey] ?: "" }

    val regressionIntervalDays: Flow<Int> =
        context.dataStore.data.map { it[regressionIntervalDaysKey] ?: 28 }

    val minClusterSize: Flow<Int> =
        context.dataStore.data.map { it[minClusterSizeKey] ?: 14 }

    val streakTrendWindow: Flow<Int> =
        context.dataStore.data.map { it[streakTrendWindowKey] ?: 14 }

    val scoreWindow: Flow<Int> =
        context.dataStore.data.map { it[scoreWindowKey] ?: 14 }

    val volatilityWindow: Flow<Int> =
        context.dataStore.data.map { it[volatilityWindowKey] ?: 14 }

    suspend fun saveOnboardingData(
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
        context.dataStore.edit { prefs ->
            prefs[initialWeightKey] = weightKg
            prefs[goalTargetKgKey] = goalTargetKg
            prefs[goalStartDateKey] = goalStartDate
            prefs[dayStartHourKey] = dayStartHour
            prefs[dayStartMinuteKey] = dayStartMinute
            prefs[notificationHourKey] = notificationHour
            prefs[notificationMinuteKey] = notificationMinute
            prefs[clusteringEnabledKey] = clusteringEnabled
            prefs[weightGoalKey] = weightGoal.name
            prefs[weightUnitKey] = weightUnit.name
            prefs[weighingFrequencyKey] = weighingFrequency.name
            prefs[notificationDayOfWeekKey] = notificationDayOfWeek
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

    suspend fun setHelpIconsVisible(visible: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[helpIconsVisibleKey] = visible
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[analyticsEnabledKey] = enabled
        }
    }

    suspend fun markHelpSeen(key: String) {
        context.dataStore.edit { prefs ->
            prefs[seenHelpKeysKey] = (prefs[seenHelpKeysKey] ?: emptySet()) + key
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

    suspend fun setWeighingFrequency(frequency: WeighingFrequency) {
        context.dataStore.edit { prefs ->
            prefs[weighingFrequencyKey] = frequency.name
        }
    }

    suspend fun setNotificationDayOfWeek(dayOfWeek: Int) {
        context.dataStore.edit { prefs ->
            prefs[notificationDayOfWeekKey] = dayOfWeek
        }
    }

    suspend fun setGoalTargetKg(targetKg: Double) {
        context.dataStore.edit { prefs ->
            prefs[goalTargetKgKey] = targetKg
        }
    }

    suspend fun setInitialWeightKg(weightKg: Double) {
        context.dataStore.edit { prefs ->
            prefs[initialWeightKey] = weightKg
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { prefs ->
            prefs[languageKey] = language.name
        }
    }

    suspend fun setGoalStartDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[goalStartDateKey] = date
        }
    }

    suspend fun setRegressionIntervalDays(days: Int) {
        context.dataStore.edit { it[regressionIntervalDaysKey] = days }
    }

    suspend fun setMinClusterSize(size: Int) {
        context.dataStore.edit { it[minClusterSizeKey] = size }
    }

    suspend fun setScoreWindow(window: Int) {
        context.dataStore.edit { it[scoreWindowKey] = window }
    }

    suspend fun setStreakTrendWindow(window: Int) {
        context.dataStore.edit { it[streakTrendWindowKey] = window }
    }

    suspend fun setVolatilityWindow(window: Int) {
        context.dataStore.edit { it[volatilityWindowKey] = window }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[onboardingCompletedKey] = false
        }
    }
}
