package com.agxmeister.ember.presentation.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.DailyAverage
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChartScreen(viewModel: ChartViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ChartUiState.Empty -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data yet. Start by adding your weight on the Home screen.")
            }
        }
        is ChartUiState.Clustered -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text("Weight Trends", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                state.clusters.forEach { cluster ->
                    ClusterChart(cluster = cluster)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        is ChartUiState.Classic -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text("Weight Trends", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                DailyAverageChart(dailyAverages = state.dailyAverages)
            }
        }
    }
}

@Composable
private fun ClusterChart(cluster: Cluster) {
    if (cluster.measurements.isEmpty()) return

    val modelProducer = remember(cluster) { CartesianChartModelProducer() }

    val xToDayIndex = remember(cluster) {
        cluster.measurements.mapIndexed { index, m ->
            val date = m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
            index to date
        }
    }

    LaunchedEffect(cluster) {
        modelProducer.runTransaction {
            lineSeries {
                series(cluster.measurements.map { it.weightKg })
            }
        }
    }

    Text(
        text = cluster.label,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ ->
                    xToDayIndex.getOrNull(x.toInt())?.second?.toString() ?: ""
                }
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}

@Composable
private fun DailyAverageChart(dailyAverages: List<DailyAverage>) {
    if (dailyAverages.isEmpty()) return

    val modelProducer = remember(dailyAverages) { CartesianChartModelProducer() }

    LaunchedEffect(dailyAverages) {
        modelProducer.runTransaction {
            lineSeries {
                series(dailyAverages.map { it.weightKg })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ ->
                    dailyAverages.getOrNull(x.toInt())?.date?.toString() ?: ""
                }
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}
