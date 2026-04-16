package com.agxmeister.ember.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.DailyAverage
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
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
    data class Clustered(
        val clusters: List<Cluster>,
        val showChart: Boolean,
        val median: Double?,
        val trend: Double?,
        val weightGoal: WeightGoal,
        val weightUnit: WeightUnit,
    ) : ChartUiState()
    data class Classic(
        val dailyAverages: List<DailyAverage>,
        val showChart: Boolean,
        val median: Double?,
        val trend: Double?,
        val weightGoal: WeightGoal,
        val weightUnit: WeightUnit,
    ) : ChartUiState()
}

@HiltViewModel
class ChartViewModel @Inject constructor(
    getClusterTrends: GetClusterTrendsUseCase,
    getDailyAverages: GetDailyAveragesUseCase,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<ChartUiState> = combine(
        preferencesRepository.clusteringEnabled,
        preferencesRepository.weightGoal,
        preferencesRepository.weightUnit,
        getClusterTrends(),
        getDailyAverages(),
    ) { clusteringEnabled, weightGoal, weightUnit, clusters, dailyAverages ->
        val now = Clock.System.now()
        val oneWeekAgo = now - 7.days
        val twoWeeksAgo = now - 14.days

        val allMeasurements = clusters.flatMap { it.measurements }
        val totalCount = allMeasurements.size
        val recentCount = allMeasurements.count { it.timestamp >= oneWeekAgo }
        val previousWeekCount = allMeasurements.count { it.timestamp >= twoWeeksAgo && it.timestamp < oneWeekAgo }

        val showChart = recentCount >= 3
        val showMedian = totalCount >= 5
        val showTrend = previousWeekCount >= 1

        if (clusteringEnabled) {
            val nonEmpty = clusters.filter { it.measurements.isNotEmpty() }
            if (nonEmpty.isEmpty()) {
                ChartUiState.Empty
            } else {
                val median = if (showMedian) {
                    nonEmpty.periodMedian(oneWeekAgo, now)
                        ?: nonEmpty.map { it.measurements.map { m -> m.weightKg }.median() }.average()
                } else null
                val trend = if (showTrend && median != null) {
                    nonEmpty.periodMedian(twoWeeksAgo, oneWeekAgo)?.let { median - it }
                } else null
                ChartUiState.Clustered(nonEmpty, showChart, median, trend, weightGoal, weightUnit)
            }
        } else {
            if (dailyAverages.isEmpty()) {
                ChartUiState.Empty
            } else {
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val oneWeekAgoDate = today.minus(DatePeriod(days = 7))
                val twoWeeksAgoDate = today.minus(DatePeriod(days = 14))
                val currentWeek = dailyAverages.filter { it.date >= oneWeekAgoDate }
                val median = if (showMedian) {
                    if (currentWeek.isNotEmpty()) currentWeek.map { it.weightKg }.median()
                    else dailyAverages.map { it.weightKg }.median()
                } else null
                val trend = if (showTrend && median != null) {
                    val previousWeek = dailyAverages.filter { it.date >= twoWeeksAgoDate && it.date < oneWeekAgoDate }
                    if (previousWeek.isNotEmpty()) median - previousWeek.map { it.weightKg }.median() else null
                } else null
                ChartUiState.Classic(dailyAverages, showChart, median, trend, weightGoal, weightUnit)
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
