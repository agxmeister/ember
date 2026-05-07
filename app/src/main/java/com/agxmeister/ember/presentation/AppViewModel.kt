package com.agxmeister.ember.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Language
import com.agxmeister.ember.domain.model.ThemeMode
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.HasRecentMeasurementUseCase
import com.agxmeister.ember.domain.usecase.IsOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    isOnboardingCompleted: IsOnboardingCompletedUseCase,
    hasRecentMeasurement: HasRecentMeasurementUseCase,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val isOnboardingCompleted: StateFlow<Boolean?> = isOnboardingCompleted()
        .map { it as Boolean? }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _hasCheckedIn = MutableStateFlow<Boolean?>(null)
    val hasCheckedIn: StateFlow<Boolean?> = _hasCheckedIn

    init {
        viewModelScope.launch {
            _hasCheckedIn.value = hasRecentMeasurement().first()
        }
    }

    val language: StateFlow<Language> = preferencesRepository.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, Language.En)

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
