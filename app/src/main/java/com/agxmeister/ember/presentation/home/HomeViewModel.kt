package com.agxmeister.ember.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.Language
import com.agxmeister.ember.domain.model.ThemeMode
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.AddMeasurementUseCase
import com.agxmeister.ember.domain.usecase.GetCurrentClusterUseCase
import com.agxmeister.ember.domain.usecase.GetDailyCandlesUseCase
import com.agxmeister.ember.domain.usecase.HasRecentMeasurementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.math.abs

data class HomeUiState(
    val currentCluster: Cluster? = null,
    val defaultWeightKg: Double? = null,
    val todayWeightKg: Double? = null,
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val isRechecking: Boolean = false,
    val isWeekly: Boolean = false,
    val targetKg: Double = 70.0,
    val tolerance: Double = 10.0,
    val themeMode: ThemeMode = ThemeMode.Auto,
    val language: Language = Language.En,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentCluster: GetCurrentClusterUseCase,
    private val addMeasurement: AddMeasurementUseCase,
    private val hasRecentMeasurement: HasRecentMeasurementUseCase,
    private val getDailyCandles: GetDailyCandlesUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            getCurrentCluster(),
            preferencesRepository.initialWeightKg,
            hasRecentMeasurement(),
            preferencesRepository.weightUnit,
            preferencesRepository.goalTargetKg,
        ) { cluster, savedInitialWeight, isRechecking, weightUnit, targetKg ->
            val tolerance = abs(savedInitialWeight - targetKg).coerceAtLeast(0.1)
            HomeUiState(
                currentCluster = cluster,
                defaultWeightKg = cluster.measurements
                    .maxByOrNull { it.timestamp }
                    ?.weightKg
                    ?: savedInitialWeight,
                weightUnit = weightUnit,
                isRechecking = isRechecking,
                targetKg = targetKg,
                tolerance = tolerance,
            )
        },
        getDailyCandles(),
        preferencesRepository.themeMode,
        preferencesRepository.language,
        preferencesRepository.weighingFrequency,
    ) { state, candles, themeMode, language, frequency ->
        val previousWeight = if (state.currentCluster?.measurements.isNullOrEmpty()) {
            candles.filter { it.date < today }.maxByOrNull { it.date }?.close
        } else null
        state.copy(
            todayWeightKg = candles.find { it.date == today }?.close,
            defaultWeightKg = previousWeight ?: state.defaultWeightKg,
            themeMode = themeMode,
            language = language,
            isWeekly = frequency == WeighingFrequency.Weekly,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun save(weightKg: Double) {
        viewModelScope.launch {
            addMeasurement(weightKg)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
    }
}
