package com.agxmeister.ember.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.usecase.IsOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    isOnboardingCompleted: IsOnboardingCompletedUseCase,
) : ViewModel() {

    val isOnboardingCompleted: StateFlow<Boolean?> = isOnboardingCompleted()
        .map { it as Boolean? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
