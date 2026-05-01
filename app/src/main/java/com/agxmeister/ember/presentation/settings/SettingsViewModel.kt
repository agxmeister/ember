package com.agxmeister.ember.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetClusterTrendsUseCase
import com.agxmeister.ember.domain.usecase.SetClusteringEnabledUseCase
import com.agxmeister.ember.domain.usecase.SetNotificationDayOfWeekUseCase
import com.agxmeister.ember.domain.usecase.SetNotificationTimeUseCase
import com.agxmeister.ember.domain.usecase.SetNotificationsEnabledUseCase
import com.agxmeister.ember.domain.usecase.SetGoalTargetUseCase
import com.agxmeister.ember.domain.usecase.SetWeightUnitUseCase
import com.agxmeister.ember.domain.usecase.SetWeighingFrequencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getClusterTrends: GetClusterTrendsUseCase,
    preferencesRepository: UserPreferencesRepository,
    private val setClusteringEnabled: SetClusteringEnabledUseCase,
    private val setGoalTarget: SetGoalTargetUseCase,
    private val setWeightUnit: SetWeightUnitUseCase,
    private val setNotificationTime: SetNotificationTimeUseCase,
    private val setNotificationsEnabled: SetNotificationsEnabledUseCase,
    private val setWeighingFrequency: SetWeighingFrequencyUseCase,
    private val setNotificationDayOfWeek: SetNotificationDayOfWeekUseCase,
) : ViewModel() {

    val clusters: StateFlow<List<Cluster>> = getClusterTrends()
        .map { it.filter { cluster -> cluster.measurements.isNotEmpty() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val clusteringEnabled: StateFlow<Boolean> = preferencesRepository.clusteringEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val weightUnit: StateFlow<WeightUnit> = preferencesRepository.weightUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightUnit.Kg,
        )

    val notificationHour: StateFlow<Int> = preferencesRepository.notificationHour
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 8,
        )

    val notificationMinute: StateFlow<Int> = preferencesRepository.notificationMinute
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 15,
        )

    val weighingFrequency: StateFlow<WeighingFrequency> = preferencesRepository.weighingFrequency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeighingFrequency.Daily,
        )

    val notificationDayOfWeek: StateFlow<Int> = preferencesRepository.notificationDayOfWeek
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1,
        )

    val initialWeightKg: StateFlow<Double> = preferencesRepository.initialWeightKg
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 70.0,
        )

    val goalTargetKg: StateFlow<Double> = preferencesRepository.goalTargetKg
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0,
        )

    fun onClusteringEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { setClusteringEnabled(enabled) }
    }

    fun onWeightUnitChanged(unit: WeightUnit) {
        viewModelScope.launch { setWeightUnit(unit) }
    }

    fun onNotificationsEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { setNotificationsEnabled(enabled) }
    }

    fun onNotificationTimeChanged(hour: Int, minute: Int) {
        viewModelScope.launch { setNotificationTime(hour, minute) }
    }

    fun onWeighingFrequencyChanged(frequency: WeighingFrequency) {
        viewModelScope.launch { setWeighingFrequency(frequency) }
    }

    fun onNotificationDayOfWeekChanged(dayOfWeek: Int) {
        viewModelScope.launch { setNotificationDayOfWeek(dayOfWeek) }
    }

    fun onGoalTargetChanged(targetKg: Double) {
        viewModelScope.launch { setGoalTarget(targetKg) }
    }
}
