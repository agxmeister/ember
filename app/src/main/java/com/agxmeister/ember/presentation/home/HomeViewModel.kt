package com.agxmeister.ember.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.AddMeasurementUseCase
import com.agxmeister.ember.domain.usecase.GetCurrentClusterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentCluster: Cluster? = null,
    val defaultWeightKg: Double = 70.0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentCluster: GetCurrentClusterUseCase,
    private val addMeasurement: AddMeasurementUseCase,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getCurrentCluster(),
        preferencesRepository.initialWeightKg,
    ) { cluster, savedInitialWeight ->
        HomeUiState(
            currentCluster = cluster,
            defaultWeightKg = cluster?.measurements
                ?.maxByOrNull { it.timestamp }
                ?.weightKg
                ?: savedInitialWeight,
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
}
