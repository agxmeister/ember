package com.agxmeister.ember.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val dayStartHour: Int = 7,
    val dayStartMinute: Int = 0,
    val clusteringEnabled: Boolean = true,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onWeightChanged(weightKg: Double) = _uiState.update { it.copy(weightKg = weightKg) }
    fun onDayStartHourChanged(hour: Int) = _uiState.update { it.copy(dayStartHour = hour) }
    fun onDayStartMinuteChanged(minute: Int) = _uiState.update { it.copy(dayStartMinute = minute) }
    fun onClusteringEnabledChanged(enabled: Boolean) = _uiState.update { it.copy(clusteringEnabled = enabled) }
    fun onNextStep() = _uiState.update { it.copy(step = it.step + 1) }

    fun complete() {
        val state = _uiState.value
        viewModelScope.launch {
            completeOnboarding(state.weightKg, state.dayStartHour, state.dayStartMinute, state.clusteringEnabled)
        }
    }
}
