package com.agxmeister.ember.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetClusterTrendsUseCase
import com.agxmeister.ember.domain.usecase.SetClusteringEnabledUseCase
import com.agxmeister.ember.domain.usecase.SetNotificationTimeUseCase
import com.agxmeister.ember.domain.usecase.SetNotificationsEnabledUseCase
import com.agxmeister.ember.domain.usecase.SetWeightGoalUseCase
import com.agxmeister.ember.domain.usecase.SetWeightUnitUseCase
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
    private val setWeightGoal: SetWeightGoalUseCase,
    private val setWeightUnit: SetWeightUnitUseCase,
    private val setNotificationTime: SetNotificationTimeUseCase,
    private val setNotificationsEnabled: SetNotificationsEnabledUseCase,
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

    val weightGoal: StateFlow<WeightGoal> = preferencesRepository.weightGoal
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightGoal.Decrease,
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

    fun onClusteringEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { setClusteringEnabled(enabled) }
    }

    fun onWeightGoalChanged(goal: WeightGoal) {
        viewModelScope.launch { setWeightGoal(goal) }
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
}
