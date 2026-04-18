package com.agxmeister.ember.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.usecase.GetMeasurementDatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class CalendarUiState(
    val measurementDates: Set<LocalDate>,
    val displayYear: Int,
    val displayMonth: Int,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getMeasurementDates: GetMeasurementDatesUseCase,
) : ViewModel() {

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _displayYearMonth = MutableStateFlow(today.year to today.monthNumber)

    val uiState: StateFlow<CalendarUiState> = combine(
        getMeasurementDates(),
        _displayYearMonth,
    ) { dates, (year, month) ->
        CalendarUiState(dates, year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(emptySet(), today.year, today.monthNumber),
    )

    fun previousMonth() {
        _displayYearMonth.update { (year, month) ->
            if (month == 1) year - 1 to 12 else year to month - 1
        }
    }

    fun nextMonth() {
        _displayYearMonth.update { (year, month) ->
            if (month == 12) year + 1 to 1 else year to month + 1
        }
    }
}
