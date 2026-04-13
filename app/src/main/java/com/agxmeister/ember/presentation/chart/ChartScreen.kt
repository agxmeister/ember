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
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.common.Position
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.DecimalFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
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

private val medianKey = ExtraStore.Key<Double>()
private val trendKey = ExtraStore.Key<Double>()

private val trendFormat = DecimalFormat("0.0")

private fun formatTrend(trend: Double): String {
    val formatted = trendFormat.format(abs(trend))
    return if (trend < 0) "−$formatted" else "+$formatted"
}

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
                CombinedClusterChart(clusters = state.clusters, median = state.median, trend = state.trend)
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
                DailyAverageChart(dailyAverages = state.dailyAverages, median = state.median, trend = state.trend)
            }
        }
    }
}

@Composable
private fun CombinedClusterChart(clusters: List<Cluster>, median: Double, trend: Double?) {
    val nonEmpty = clusters.filter { it.measurements.isNotEmpty() }
    if (nonEmpty.isEmpty()) return

    val referenceDates = remember(clusters) {
        nonEmpty.firstOrNull()?.measurements
            ?.sortedBy { it.timestamp }
            ?.map { m -> m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            ?: emptyList()
    }

    val modelProducer = remember(clusters) { CartesianChartModelProducer() }

    LaunchedEffect(clusters, median, trend) {
        modelProducer.runTransaction {
            lineSeries {
                nonEmpty.forEach { cluster ->
                    series(cluster.measurements.map { it.weightKg })
                }
            }
            extras {
                it[medianKey] = median
                if (trend != null) it[trendKey] = trend
            }
        }
    }

    ClusterChartContent(
        nonEmpty = nonEmpty,
        referenceDates = referenceDates,
        median = median,
        trend = trend,
        modelProducer = modelProducer,
    )
}

@Composable
private fun ClusterChartContent(
    nonEmpty: List<Cluster>,
    referenceDates: List<kotlinx.datetime.LocalDate>,
    median: Double? = null,
    trend: Double? = null,
    modelProducer: CartesianChartModelProducer? = null,
    model: CartesianChartModel? = null,
) {
    val sorted = remember(nonEmpty) {
        nonEmpty.map { it.copy(measurements = it.measurements.sortedBy { m -> m.timestamp }) }
    }
    val pointConnector = remember { LineCartesianLayer.PointConnector.cubic() }
    val eosLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Eos]!!)),
        pointConnector = pointConnector,
    )
    val heliosLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Helios]!!)),
        pointConnector = pointConnector,
    )
    val hesperusLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Hesperus]!!)),
        pointConnector = pointConnector,
    )
    val seleneLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(clusterColors[DayCluster.Selene]!!)),
        pointConnector = pointConnector,
    )

    val lineByCluster = mapOf(
        DayCluster.Eos to eosLine,
        DayCluster.Helios to heliosLine,
        DayCluster.Hesperus to hesperusLine,
        DayCluster.Selene to seleneLine,
    )
    val seriesLines = sorted.map { lineByCluster[it.dayCluster]!! }

    val medianColor = Color(0xFFE53935)
    val medianLineComponent = rememberLineComponent(
        fill = fill(medianColor),
        thickness = 2.5.dp,
    )
    val medianLabelComponent = rememberTextComponent(color = medianColor)
    val medianDecoration = if (median != null) {
        remember(median, trend) {
            HorizontalLine(
                y = { it.getOrNull(medianKey) ?: median },
                line = medianLineComponent,
                labelComponent = medianLabelComponent,
                label = { extraStore ->
                    val m = DecimalFormat("0.0").format(extraStore.getOrNull(medianKey) ?: median)
                    val t = extraStore.getOrNull(trendKey) ?: trend
                    if (t != null) "$m, ${formatTrend(t)} vs prev. week" else m
                },
                horizontalLabelPosition = Position.Horizontal.End,
                verticalLabelPosition = Position.Vertical.Top,
            )
        }
    } else null

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
    )
    val zoomState = rememberVicoZoomState(
        initialZoom = Zoom.max(Zoom.x(8.0), Zoom.Content),
    )

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(*seriesLines.toTypedArray()),
            rangeProvider = weightRangeProvider,
        ),
        startAxis = VerticalAxis.rememberStart(valueFormatter = weightValueFormatter),
        bottomAxis = HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(textSize = 10.sp),
            itemPlacer = HorizontalAxis.ItemPlacer.aligned(shiftExtremeLines = false, spacing = { 1 }, addExtremeLabelPadding = false),
            valueFormatter = { _, x, _ ->
                    val date = referenceDates.getOrNull(x.toInt())
                    if (date != null) {
                        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                        "$month ${date.dayOfMonth}"
                    } else ""
                }
        ),
        decorations = listOfNotNull(medianDecoration),
    )

    if (modelProducer != null) {
        CartesianChartHost(
            chart = chart,
            modelProducer = modelProducer,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth().height(300.dp),
        )
    } else if (model != null) {
        CartesianChartHost(
            chart = chart,
            model = model,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth().height(300.dp),
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        sorted.forEach { cluster ->
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
            clusters.forEach { cluster ->
                series(cluster.measurements.sortedBy { it.timestamp }.map { it.weightKg })
            }
        }
    )

private fun previewDates(clusters: List<Cluster>) =
    clusters.firstOrNull()?.measurements
        ?.sortedBy { it.timestamp }
        ?.map { m -> m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date }
        ?: emptyList()

private fun previewMedian(clusters: List<Cluster>): Double {
    val clusterMedians = clusters.filter { it.measurements.isNotEmpty() }.map { cluster ->
        val sorted = cluster.measurements.take(7).map { it.weightKg }.sorted()
        if (sorted.size % 2 == 0) (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        else sorted[sorted.size / 2]
    }
    return if (clusterMedians.isEmpty()) 0.0 else clusterMedians.average()
}

private fun previewTrend(clusters: List<Cluster>): Double {
    val medianForRange = { range: IntRange ->
        val clusterMedians = clusters.filter { it.measurements.isNotEmpty() }.map { cluster ->
            val sorted = cluster.measurements.slice(range).map { it.weightKg }.sorted()
            if (sorted.size % 2 == 0) (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            else sorted[sorted.size / 2]
        }
        if (clusterMedians.isEmpty()) 0.0 else clusterMedians.average()
    }
    return medianForRange(0..6) - medianForRange(7..13)
}

@Preview(showBackground = true)
@Composable
private fun AllClustersPreview() {
    val clusters = previewClusters(*DayCluster.entries.toTypedArray())
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ClusterChartContent(
                nonEmpty = clusters,
                referenceDates = previewDates(clusters),
                median = previewMedian(clusters),
                trend = previewTrend(clusters),
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
                median = previewMedian(clusters),
                trend = previewTrend(clusters),
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
                median = previewMedian(clusters),
                trend = previewTrend(clusters),
                model = previewModel(clusters),
            )
        }
    }
}

@Composable
private fun DailyAverageChart(dailyAverages: List<DailyAverage>, median: Double, trend: Double?) {
    if (dailyAverages.isEmpty()) return

    val modelProducer = remember(dailyAverages) { CartesianChartModelProducer() }

    LaunchedEffect(dailyAverages, median, trend) {
        modelProducer.runTransaction {
            lineSeries {
                series(dailyAverages.map { it.weightKg })
            }
            extras {
                it[medianKey] = median
                if (trend != null) it[trendKey] = trend
            }
        }
    }

    val medianColor = Color(0xFFE53935)
    val medianLineComponent = rememberLineComponent(
        fill = fill(medianColor),
        thickness = 2.5.dp,
    )
    val medianLabelComponent = rememberTextComponent(color = medianColor)
    val medianDecoration = remember(median, trend) {
        HorizontalLine(
            y = { it.getOrNull(medianKey) ?: median },
            line = medianLineComponent,
            labelComponent = medianLabelComponent,
            label = { extraStore ->
                val m = DecimalFormat("0.0").format(extraStore.getOrNull(medianKey) ?: median)
                val t = extraStore.getOrNull(trendKey) ?: trend
                if (t != null) "$m, ${formatTrend(t)} vs prev. week" else m
            },
            horizontalLabelPosition = Position.Horizontal.End,
            verticalLabelPosition = Position.Vertical.Top,
        )
    }

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
    )
    val zoomState = rememberVicoZoomState(
        initialZoom = Zoom.max(Zoom.x(8.0), Zoom.Content),
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(pointConnector = remember { LineCartesianLayer.PointConnector.cubic() })
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = weightValueFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(textSize = 10.sp),
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(shiftExtremeLines = false, spacing = { 1 }, addExtremeLabelPadding = false),
                valueFormatter = { _, x, _ ->
                    val date = dailyAverages.getOrNull(x.toInt())?.date
                    if (date != null) {
                        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                        "$month ${date.dayOfMonth}"
                    } else ""
                }
            ),
            decorations = listOf(medianDecoration),
        ),
        modelProducer = modelProducer,
        scrollState = scrollState,
        zoomState = zoomState,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )

}
