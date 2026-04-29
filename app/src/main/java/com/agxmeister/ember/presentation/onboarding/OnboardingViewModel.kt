package com.agxmeister.ember.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 0,
    val weightKg: Double = 62.0,
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val weightGoal: WeightGoal = WeightGoal.Decrease,
    val weighingFrequency: WeighingFrequency = WeighingFrequency.Daily,
    val dayStartHour: Int = 8,
    val dayStartMinute: Int = 0,
    val notificationDayOfWeek: Int = 1,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val clusteringEnabled: Boolean = true,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onWeightChanged(weightKg: Double) = _uiState.update { it.copy(weightKg = weightKg) }
    fun onWeightUnitChanged(unit: WeightUnit) = _uiState.update { it.copy(weightUnit = unit) }
    fun onWeightGoalChanged(goal: WeightGoal) = _uiState.update { it.copy(weightGoal = goal) }
    fun onWeighingFrequencyChanged(frequency: WeighingFrequency) = _uiState.update { it.copy(weighingFrequency = frequency) }
    fun onDayStartHourChanged(hour: Int) = _uiState.update { it.copy(dayStartHour = hour) }
    fun onDayStartMinuteChanged(minute: Int) = _uiState.update { it.copy(dayStartMinute = minute) }
    fun onNotificationDayOfWeekChanged(dayOfWeek: Int) = _uiState.update { it.copy(notificationDayOfWeek = dayOfWeek) }
    fun onNotificationHourChanged(hour: Int) = _uiState.update { it.copy(notificationHour = hour) }
    fun onNotificationMinuteChanged(minute: Int) = _uiState.update { it.copy(notificationMinute = minute) }
    fun onClusteringEnabledChanged(enabled: Boolean) = _uiState.update { it.copy(clusteringEnabled = enabled) }
    fun onNextStep() = _uiState.update { it.copy(step = it.step + 1) }

    fun complete() {
        val state = _uiState.value
        viewModelScope.launch {
            completeOnboarding(
                weightKg = state.weightKg,
                dayStartHour = state.dayStartHour,
                dayStartMinute = state.dayStartMinute,
                clusteringEnabled = state.clusteringEnabled,
                weightGoal = state.weightGoal,
                weightUnit = state.weightUnit,
                weighingFrequency = state.weighingFrequency,
                notificationDayOfWeek = state.notificationDayOfWeek,
                notificationHour = state.notificationHour,
                notificationMinute = state.notificationMinute,
            )
        }
    }
}
