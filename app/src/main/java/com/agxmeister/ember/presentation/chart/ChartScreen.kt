package com.agxmeister.ember.presentation.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.DailyAverage
import com.agxmeister.ember.domain.model.DayCluster
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val clusterColors = mapOf(
    DayCluster.Eos to Color(0xFFFFA726),
    DayCluster.Helios to Color(0xFFFFD54F),
    DayCluster.Hesperus to Color(0xFFAB47BC),
    DayCluster.Selene to Color(0xFF5C6BC0),
)

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
                CombinedClusterChart(clusters = state.clusters)
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
private fun CombinedClusterChart(clusters: List<Cluster>) {
    val nonEmpty = clusters.filter { it.measurements.isNotEmpty() }
    if (nonEmpty.isEmpty()) return

    // Use the first cluster's dates as the shared x-axis reference.
    // Clusters are measured on the same days so indices align across series.
    val referenceDates = remember(clusters) {
        nonEmpty.firstOrNull()?.measurements?.map { m ->
            m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
        } ?: emptyList()
    }

    val modelProducer = remember(clusters) { CartesianChartModelProducer() }

    LaunchedEffect(clusters) {
        modelProducer.runTransaction {
            lineSeries {
                nonEmpty.forEach { cluster ->
                    series(cluster.measurements.map { it.weightKg })
                }
            }
        }
    }

    val eosLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Eos]!!))
    )
    val heliosLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Helios]!!))
    )
    val hesperusLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Hesperus]!!))
    )
    val seleneLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Selene]!!))
    )

    val lineByCluster = mapOf(
        DayCluster.Eos to eosLine,
        DayCluster.Helios to heliosLine,
        DayCluster.Hesperus to hesperusLine,
        DayCluster.Selene to seleneLine,
    )
    val seriesLines = nonEmpty.map { lineByCluster[it.dayCluster]!! }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(*seriesLines.toTypedArray()),
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ -> referenceDates.getOrNull(x.toInt())?.toString() ?: "" }
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    )

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        nonEmpty.forEach { cluster ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(clusterColors[cluster.dayCluster] ?: Color.Gray, CircleShape)
                )
                Text(text = cluster.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
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
