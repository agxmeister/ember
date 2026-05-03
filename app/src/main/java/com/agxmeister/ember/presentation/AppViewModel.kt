package com.agxmeister.ember.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.ThemeMode
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.IsOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    isOnboardingCompleted: IsOnboardingCompletedUseCase,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val isOnboardingCompleted: StateFlow<Boolean?> = isOnboardingCompleted()
        .map { it as Boolean? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isDarkTheme: StateFlow<Boolean> = preferencesRepository.themeMode
        .map { mode ->
            when (mode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.Auto -> {
                    val hour = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).hour
                    hour < 7 || hour >= 21
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
