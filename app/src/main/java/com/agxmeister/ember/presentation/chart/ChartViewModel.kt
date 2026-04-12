package com.agxmeister.ember.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.DailyAverage
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.GetClusterTrendsUseCase
import com.agxmeister.ember.domain.usecase.GetDailyAveragesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class ChartUiState {
    data object Empty : ChartUiState()
    data class Clustered(val clusters: List<Cluster>) : ChartUiState()
    data class Classic(val dailyAverages: List<DailyAverage>) : ChartUiState()
}

@HiltViewModel
class ChartViewModel @Inject constructor(
    getClusterTrends: GetClusterTrendsUseCase,
    getDailyAverages: GetDailyAveragesUseCase,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<ChartUiState> = combine(
        preferencesRepository.clusteringEnabled,
        getClusterTrends(),
        getDailyAverages(),
    ) { clusteringEnabled, clusters, dailyAverages ->
        if (clusteringEnabled) {
            val nonEmpty = clusters.filter { it.measurements.isNotEmpty() }
            if (nonEmpty.isEmpty()) ChartUiState.Empty else ChartUiState.Clustered(nonEmpty)
        } else {
            if (dailyAverages.isEmpty()) ChartUiState.Empty else ChartUiState.Classic(dailyAverages)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartUiState.Empty,
    )
}
