package com.agxmeister.ember.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.clustering.ClusteringAlgorithm
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import com.agxmeister.ember.domain.usecase.DeleteMeasurementUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementDatesUseCase
import com.agxmeister.ember.domain.usecase.GetMeasurementsForDateUseCase
import com.agxmeister.ember.domain.usecase.SaveMeasurementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class CalendarUiState(
    val measurementDates: Set<LocalDate>,
    val displayYear: Int,
    val displayMonth: Int,
    val selectedDate: LocalDate? = null,
    val selectedDateMeasurements: List<Measurement> = emptyList(),
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val defaultWeightKg: Double = 70.0,
    val dayStartHour: Int = 7,
    val dayStartMinute: Int = 0,
)

data class PendingReplace(
    val existingId: Long,
    val weightKg: Double,
    val timestamp: Instant,
    val clusterName: String?,
    val date: LocalDate,
)

data class PendingDelete(
    val id: Long,
    val clusterName: String?,
    val date: LocalDate,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    getMeasurementDates: GetMeasurementDatesUseCase,
    private val getMeasurementsForDate: GetMeasurementsForDateUseCase,
    private val saveUseCase: SaveMeasurementUseCase,
    private val deleteUseCase: DeleteMeasurementUseCase,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val _displayYearMonth = MutableStateFlow(today.year to today.monthNumber)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _pendingReplace = MutableStateFlow<PendingReplace?>(null)
    private val _pendingDelete = MutableStateFlow<PendingDelete?>(null)
    private val _saveEvents = MutableSharedFlow<Unit>()

    val pendingReplace: StateFlow<PendingReplace?> = _pendingReplace.asStateFlow()
    val pendingDelete: StateFlow<PendingDelete?> = _pendingDelete.asStateFlow()
    val saveEvents: SharedFlow<Unit> = _saveEvents.asSharedFlow()

    private val selectedDateMeasurements: Flow<List<Measurement>> =
        _selectedDate.flatMapLatest { date ->
            if (date != null) getMeasurementsForDate(date) else flowOf(emptyList())
        }

    private val dayStart = combine(
        preferencesRepository.dayStartHour,
        preferencesRepository.dayStartMinute,
    ) { h, m -> h to m }

    val uiState: StateFlow<CalendarUiState> = combine(
        combine(getMeasurementDates(), _displayYearMonth, selectedDateMeasurements) { a, b, c -> Triple(a, b, c) },
        combine(preferencesRepository.weightUnit, preferencesRepository.initialWeightKg, dayStart) { u, w, ds -> Triple(u, w, ds) },
    ) { (dates, yearMonth, measurements), (weightUnit, defaultWeightKg, dayStart) ->
        val (year, month) = yearMonth
        val (dayStartHour, dayStartMinute) = dayStart
        CalendarUiState(
            measurementDates = dates,
            displayYear = year,
            displayMonth = month,
            selectedDate = _selectedDate.value,
            selectedDateMeasurements = measurements,
            weightUnit = weightUnit,
            defaultWeightKg = defaultWeightKg,
            dayStartHour = dayStartHour,
            dayStartMinute = dayStartMinute,
        )
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

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _pendingReplace.value = null
        _pendingDelete.value = null
    }

    fun dismissSheet() {
        _selectedDate.value = null
        _pendingReplace.value = null
        _pendingDelete.value = null
    }

    fun requestSave(id: Long, weightKg: Double, timestamp: Instant) {
        viewModelScope.launch {
            if (id != 0L) {
                saveUseCase(id, weightKg, timestamp)
                _saveEvents.emit(Unit)
                return@launch
            }
            val clusteringEnabled = preferencesRepository.clusteringEnabled.first()
            val tz = TimeZone.currentSystemDefault()
            val existing: Measurement?
            val clusterName: String?
            if (clusteringEnabled) {
                val dayStartHour = preferencesRepository.dayStartHour.first()
                val newTime = timestamp.toLocalDateTime(tz)
                val newCluster = ClusteringAlgorithm.assign(newTime.hour * 60 + newTime.minute, dayStartHour)
                existing = uiState.value.selectedDateMeasurements.firstOrNull { m ->
                    val mTime = m.timestamp.toLocalDateTime(tz)
                    ClusteringAlgorithm.assign(mTime.hour * 60 + mTime.minute, dayStartHour) == newCluster
                }
                clusterName = newCluster.label
            } else {
                existing = uiState.value.selectedDateMeasurements.firstOrNull()
                clusterName = null
            }
            if (existing != null) {
                _pendingReplace.value = PendingReplace(existing.id, weightKg, timestamp, clusterName, _selectedDate.value!!)
            } else {
                saveUseCase(id, weightKg, timestamp)
                _saveEvents.emit(Unit)
            }
        }
    }

    fun confirmReplace() {
        viewModelScope.launch {
            val pending = _pendingReplace.value ?: return@launch
            saveUseCase(pending.existingId, pending.weightKg, pending.timestamp)
            _pendingReplace.value = null
            _saveEvents.emit(Unit)
        }
    }

    fun cancelReplace() {
        _pendingReplace.value = null
    }

    fun requestDelete(measurement: Measurement) {
        viewModelScope.launch {
            val clusteringEnabled = preferencesRepository.clusteringEnabled.first()
            val clusterName = if (clusteringEnabled) {
                val dayStartHour = preferencesRepository.dayStartHour.first()
                val tz = TimeZone.currentSystemDefault()
                val mTime = measurement.timestamp.toLocalDateTime(tz)
                ClusteringAlgorithm.assign(mTime.hour * 60 + mTime.minute, dayStartHour).label
            } else null
            _pendingDelete.value = PendingDelete(measurement.id, clusterName, _selectedDate.value!!)
        }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            val pending = _pendingDelete.value ?: return@launch
            deleteUseCase(pending.id)
            _pendingDelete.value = null
        }
    }

    fun cancelDelete() {
        _pendingDelete.value = null
    }
}
