package com.agxmeister.ember.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.usecase.GetClusterTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getClusterTrends: GetClusterTrendsUseCase,
) : ViewModel() {

    val clusters: StateFlow<List<Cluster>> = getClusterTrends()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
