package com.agxmeister.ember.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.MeasurementRepository
import com.agxmeister.ember.domain.usecase.CompleteOnboardingUseCase
import com.agxmeister.ember.domain.usecase.ImportMeasurementsUseCase
import com.agxmeister.ember.presentation.SeedMeasuresCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeedMeasuresOnboardingUiState(
    val step: Int = 0,
    val weightKg: Double = 62.0,
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val goalTargetKg: Double = 62.0,
    val weighingFrequency: WeighingFrequency = WeighingFrequency.Daily,
    val dayStartHour: Int = 8,
    val dayStartMinute: Int = 0,
    val notificationDayOfWeek: Int = 1,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val clusteringEnabled: Boolean = true,
    val measuresText: String = "",
    val completed: Boolean = false,
)

@HiltViewModel
class SeedMeasuresOnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase,
    private val importMeasurements: ImportMeasurementsUseCase,
    private val measurementRepository: MeasurementRepository,
    private val seedMeasuresCoordinator: SeedMeasuresCoordinator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeedMeasuresOnboardingUiState())
    val uiState: StateFlow<SeedMeasuresOnboardingUiState> = _uiState.asStateFlow()

    fun onWeightChanged(weightKg: Double) = _uiState.update { it.copy(weightKg = weightKg, goalTargetKg = weightKg) }
    fun onWeightUnitChanged(unit: WeightUnit) = _uiState.update { it.copy(weightUnit = unit) }
    fun onGoalTargetChanged(targetKg: Double) = _uiState.update { it.copy(goalTargetKg = targetKg) }
    fun onWeighingFrequencyChanged(f: WeighingFrequency) = _uiState.update { it.copy(weighingFrequency = f) }
    fun onDayStartHourChanged(hour: Int) = _uiState.update { it.copy(dayStartHour = hour) }
    fun onDayStartMinuteChanged(minute: Int) = _uiState.update { it.copy(dayStartMinute = minute) }
    fun onNotificationDayOfWeekChanged(dow: Int) = _uiState.update { it.copy(notificationDayOfWeek = dow) }
    fun onNotificationHourChanged(hour: Int) = _uiState.update { it.copy(notificationHour = hour) }
    fun onNotificationMinuteChanged(minute: Int) = _uiState.update { it.copy(notificationMinute = minute) }
    fun onClusteringEnabledChanged(enabled: Boolean) = _uiState.update { it.copy(clusteringEnabled = enabled) }
    fun onMeasuresTextChanged(text: String) = _uiState.update { it.copy(measuresText = text) }
    fun onNextStep() = _uiState.update { it.copy(step = it.step + 1) }

    fun complete() {
        val state = _uiState.value
        viewModelScope.launch {
            completeOnboarding(
                weightKg = state.weightKg,
                goalTargetKg = state.goalTargetKg,
                dayStartHour = state.dayStartHour,
                dayStartMinute = state.dayStartMinute,
                clusteringEnabled = state.clusteringEnabled,
                weightUnit = state.weightUnit,
                weighingFrequency = state.weighingFrequency,
                notificationDayOfWeek = state.notificationDayOfWeek,
                notificationHour = state.notificationHour,
                notificationMinute = state.notificationMinute,
            )
            seedMeasuresCoordinator.consume()
            measurementRepository.deleteAll()
            val values = state.measuresText.split(",").mapNotNull { it.trim().toDoubleOrNull() }
            if (values.isNotEmpty()) {
                importMeasurements(values, state.weightUnit)
            }
            _uiState.update { it.copy(completed = true) }
        }
    }
}
