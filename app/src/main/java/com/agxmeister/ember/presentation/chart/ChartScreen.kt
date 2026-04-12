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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.presentation.theme.EmberTheme
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
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.DecimalFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

// Adds 20 % of the data spread as padding above and below, keeping the axis
// tightly zoomed on actual weight values rather than starting from zero.
private val weightValueFormatter = CartesianValueFormatter.decimal(DecimalFormat("#;−#"))

private val weightRangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val padding = (maxY - minY).coerceAtLeast(1.0) * 0.2
        return minY - padding
    }
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val padding = (maxY - minY).coerceAtLeast(1.0) * 0.2
        return maxY + padding
    }
}

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

    ClusterChartContent(
        nonEmpty = nonEmpty,
        referenceDates = referenceDates,
        modelProducer = modelProducer,
    )
}

@Composable
private fun ClusterChartContent(
    nonEmpty: List<Cluster>,
    referenceDates: List<kotlinx.datetime.LocalDate>,
    modelProducer: CartesianChartModelProducer? = null,
    model: CartesianChartModel? = null,
) {
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

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(*seriesLines.toTypedArray()),
            rangeProvider = weightRangeProvider,
        ),
        startAxis = VerticalAxis.rememberStart(valueFormatter = weightValueFormatter),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = { _, x, _ ->
                    val date = referenceDates.getOrNull(x.toInt())
                    if (date != null) {
                        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                        "$month ${date.dayOfMonth}"
                    } else ""
                }
        ),
    )

    if (modelProducer != null) {
        CartesianChartHost(
            chart = chart,
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(300.dp),
        )
    } else if (model != null) {
        CartesianChartHost(
            chart = chart,
            model = model,
            modifier = Modifier.fillMaxWidth().height(300.dp),
        )
    }

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

private fun previewClusters(vararg included: DayCluster): List<Cluster> {
    val now = Clock.System.now()
    val baseWeights = mapOf(
        DayCluster.Eos to 80.0,
        DayCluster.Helios to 81.5,
        DayCluster.Hesperus to 82.0,
        DayCluster.Selene to 83.0,
    )
    // Daily fluctuation pattern: up/down noise on top of a gentle downward trend
    val fluctuation = listOf(0.0, 0.3, -0.1, 0.4, 0.1, -0.2, 0.5, -0.3, 0.2, -0.4, 0.1, -0.1, 0.3, -0.2)
    val trend = -0.05 // kg per day
    return included.map { cluster ->
        Cluster(
            dayCluster = cluster,
            measurements = (0..13).map { day ->
                Measurement(
                    weightKg = baseWeights[cluster]!! + fluctuation[day] + day * trend,
                    timestamp = now - day.days,
                )
            },
        )
    }
}

private fun previewModel(clusters: List<Cluster>): CartesianChartModel =
    CartesianChartModel(
        LineCartesianLayerModel.build {
            clusters.forEach { cluster -> series(cluster.measurements.map { it.weightKg }) }
        }
    )

private fun previewDates(clusters: List<Cluster>) =
    clusters.firstOrNull()?.measurements?.map { m ->
        m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
    } ?: emptyList()

@Preview(showBackground = true)
@Composable
private fun AllClustersPreview() {
    val clusters = previewClusters(*DayCluster.entries.toTypedArray())
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ClusterChartContent(
                nonEmpty = clusters,
                referenceDates = previewDates(clusters),
                model = previewModel(clusters),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoClustersPreview() {
    val clusters = previewClusters(DayCluster.Eos, DayCluster.Selene)
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ClusterChartContent(
                nonEmpty = clusters,
                referenceDates = previewDates(clusters),
                model = previewModel(clusters),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneClusterPreview() {
    val clusters = previewClusters(DayCluster.Helios)
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ClusterChartContent(
                nonEmpty = clusters,
                referenceDates = previewDates(clusters),
                model = previewModel(clusters),
            )
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
            startAxis = VerticalAxis.rememberStart(valueFormatter = weightValueFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ ->
                    val date = dailyAverages.getOrNull(x.toInt())?.date
                    if (date != null) {
                        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                        "$month ${date.dayOfMonth}"
                    } else ""
                }
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}
