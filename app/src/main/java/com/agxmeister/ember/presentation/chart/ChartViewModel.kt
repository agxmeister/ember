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
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

sealed class ChartUiState {
    data object Empty : ChartUiState()
    data class Clustered(val clusters: List<Cluster>, val median: Double, val trend: Double?) : ChartUiState()
    data class Classic(val dailyAverages: List<DailyAverage>, val median: Double, val trend: Double?) : ChartUiState()
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
        val now = Clock.System.now()
        val oneWeekAgo = now - 7.days
        val twoWeeksAgo = now - 14.days

        if (clusteringEnabled) {
            val nonEmpty = clusters.filter { it.measurements.isNotEmpty() }
            if (nonEmpty.isEmpty()) {
                ChartUiState.Empty
            } else {
                val currentMedian = nonEmpty.periodMedian(oneWeekAgo, now)
                    ?: nonEmpty.map { it.measurements.map { m -> m.weightKg }.median() }.average()
                val previousMedian = nonEmpty.periodMedian(twoWeeksAgo, oneWeekAgo)
                ChartUiState.Clustered(nonEmpty, currentMedian, previousMedian?.let { currentMedian - it })
            }
        } else {
            if (dailyAverages.isEmpty()) {
                ChartUiState.Empty
            } else {
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val oneWeekAgoDate = today.minus(DatePeriod(days = 7))
                val twoWeeksAgoDate = today.minus(DatePeriod(days = 14))
                val currentWeek = dailyAverages.filter { it.date >= oneWeekAgoDate }
                val previousWeek = dailyAverages.filter { it.date >= twoWeeksAgoDate && it.date < oneWeekAgoDate }
                val currentMedian = if (currentWeek.isNotEmpty()) currentWeek.map { it.weightKg }.median()
                                    else dailyAverages.map { it.weightKg }.median()
                val previousMedian = if (previousWeek.isNotEmpty()) previousWeek.map { it.weightKg }.median() else null
                ChartUiState.Classic(dailyAverages, currentMedian, previousMedian?.let { currentMedian - it })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartUiState.Empty,
    )
}

private fun List<Cluster>.periodMedian(from: Instant, to: Instant): Double? {
    val clusterMedians = mapNotNull { cluster ->
        val weights = cluster.measurements
            .filter { it.timestamp >= from && it.timestamp <= to }
            .map { it.weightKg }
        if (weights.isEmpty()) null else weights.median()
    }
    return if (clusterMedians.isEmpty()) null else clusterMedians.average()
}

private fun List<Double>.median(): Double {
    val sorted = sorted()
    return if (sorted.size % 2 == 0) {
        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
    } else {
        sorted[sorted.size / 2]
    }
}
