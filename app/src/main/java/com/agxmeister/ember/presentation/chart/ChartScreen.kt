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
import kotlinx.datetime.LocalDate
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
                when {
                    state.showChart -> CombinedClusterChart(
                        clusters = state.clusters,
                        median = state.median,
                        trend = state.trend,
                    )
                    state.median != null -> MedianDisplay(median = state.median, trend = state.trend)
                    else -> Text(
                        text = "Keep measuring! You need at least 3 measurements this week to see the chart, and 5 in total for the median.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                when {
                    state.showChart -> DailyAverageChart(
                        dailyAverages = state.dailyAverages,
                        median = state.median,
                        trend = state.trend,
                    )
                    state.median != null -> MedianDisplay(median = state.median, trend = state.trend)
                    else -> Text(
                        text = "Keep measuring! You need at least 3 measurements this week to see the chart, and 5 in total for the median.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MedianDisplay(median: Double, trend: Double?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Current median",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${DecimalFormat("0.0").format(median)} kg",
            style = MaterialTheme.typography.displaySmall,
        )
        if (trend != null) {
            Text(
                text = "${formatTrend(trend)} vs prev. week",
                style = MaterialTheme.typography.bodyMedium,
                color = if (trend <= 0) Color(0xFF4CAF50) else Color(0xFFE53935),
            )
        }
    }
}

@Composable
private fun CombinedClusterChart(clusters: List<Cluster>, median: Double?, trend: Double?) {
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
                if (median != null) it[medianKey] = median
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
    referenceDates: List<LocalDate>,
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

// — Preview helpers —

private fun buildClusters(vararg specs: Pair<DayCluster, List<Measurement>>): List<Cluster> =
    specs.map { (cluster, measurements) -> Cluster(dayCluster = cluster, measurements = measurements) }

// 14 days of data across all clusters (chart + median + trend)
private fun previewClusters14Days(vararg included: DayCluster): List<Cluster> {
    val now = Clock.System.now()
    val baseWeights = mapOf(
        DayCluster.Eos to 80.0,
        DayCluster.Helios to 81.5,
        DayCluster.Hesperus to 82.0,
        DayCluster.Selene to 83.0,
    )
    val fluctuation = listOf(0.0, 0.3, -0.1, 0.4, 0.1, -0.2, 0.5, -0.3, 0.2, -0.4, 0.1, -0.1, 0.3, -0.2)
    return included.map { cluster ->
        Cluster(
            dayCluster = cluster,
            measurements = (0..13).map { day ->
                Measurement(
                    weightKg = baseWeights[cluster]!! + fluctuation[day] + day * -0.05,
                    timestamp = now - day.days,
                )
            },
        )
    }
}

// 4 measurements all within last 7 days → chart visible, < 5 total → no median
private fun previewClustersChartOnly(): List<Cluster> {
    val now = Clock.System.now()
    return buildClusters(
        DayCluster.Eos to (0..3).map { day ->
            Measurement(weightKg = 80.0 - day * 0.1, timestamp = now - day.days)
        }
    )
}

// 1 recent + 5 old (> 14 days ago) → >= 5 total → median; < 3 recent → no chart; 0 in prev week → no trend
private fun previewClustersMedianOnly(): List<Cluster> {
    val now = Clock.System.now()
    return buildClusters(
        DayCluster.Eos to listOf(
            Measurement(weightKg = 79.5, timestamp = now - 2.days),
        ) + (0..4).map { i ->
            Measurement(weightKg = 80.0 + i * 0.2, timestamp = now - (20 + i).days)
        }
    )
}

// 1 recent + 3 in prev week + 2 old → >= 5 total → median; < 3 recent → no chart; 3 in prev week → trend
private fun previewClustersMedianOnlyWithTrend(): List<Cluster> {
    val now = Clock.System.now()
    return buildClusters(
        DayCluster.Eos to listOf(
            Measurement(weightKg = 79.5, timestamp = now - 2.days),
        ) + (0..2).map { i ->
            Measurement(weightKg = 80.2 + i * 0.1, timestamp = now - (8 + i).days)
        } + (0..1).map { i ->
            Measurement(weightKg = 80.5, timestamp = now - (20 + i).days)
        }
    )
}

// 5 in last 7 days + 2 older than 14 days → >= 5 total → median; >= 3 recent → chart; 0 in prev week → no trend
private fun previewClustersChartAndMedianNoTrend(): List<Cluster> {
    val now = Clock.System.now()
    return buildClusters(
        DayCluster.Eos to (0..4).map { day ->
            Measurement(weightKg = 80.0 - day * 0.1, timestamp = now - day.days)
        } + (0..1).map { i ->
            Measurement(weightKg = 80.8, timestamp = now - (20 + i).days)
        }
    )
}

private fun previewModel(clusters: List<Cluster>): CartesianChartModel =
    CartesianChartModel(
        LineCartesianLayerModel.build {
            clusters.filter { it.measurements.isNotEmpty() }.forEach { cluster ->
                series(cluster.measurements.sortedBy { it.timestamp }.map { it.weightKg })
            }
        }
    )

private fun previewDates(clusters: List<Cluster>): List<LocalDate> =
    clusters.firstOrNull()?.measurements
        ?.sortedBy { it.timestamp }
        ?.map { m -> m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date }
        ?: emptyList()

private fun previewMedian(clusters: List<Cluster>): Double {
    val allWeights = clusters.flatMap { it.measurements }.map { it.weightKg }.sorted()
    return if (allWeights.size % 2 == 0) (allWeights[allWeights.size / 2 - 1] + allWeights[allWeights.size / 2]) / 2.0
    else allWeights[allWeights.size / 2]
}

private fun previewTrend(clusters: List<Cluster>): Double {
    val now = Clock.System.now()
    val oneWeekAgo = now - 7.days
    val twoWeeksAgo = now - 14.days
    val medianForRange = { from: kotlinx.datetime.Instant, to: kotlinx.datetime.Instant ->
        val weights = clusters.flatMap { it.measurements }
            .filter { it.timestamp >= from && it.timestamp <= to }
            .map { it.weightKg }.sorted()
        if (weights.isEmpty()) null
        else if (weights.size % 2 == 0) (weights[weights.size / 2 - 1] + weights[weights.size / 2]) / 2.0
        else weights[weights.size / 2]
    }
    val current = medianForRange(oneWeekAgo, now) ?: 0.0
    val previous = medianForRange(twoWeeksAgo, oneWeekAgo) ?: 0.0
    return current - previous
}

// — Previews —

@Preview(showBackground = true, name = "Empty — no data")
@Composable
private fun EmptyPreview() {
    EmberTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data yet. Start by adding your weight on the Home screen.")
        }
    }
}

@Preview(showBackground = true, name = "Chart only — < 5 total, no median")
@Composable
private fun ChartOnlyPreview() {
    val clusters = previewClustersChartOnly()
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ClusterChartContent(
                nonEmpty = clusters,
                referenceDates = previewDates(clusters),
                median = null,
                trend = null,
                model = previewModel(clusters),
            )
        }
    }
}

@Preview(showBackground = true, name = "Median only — no chart, no trend")
@Composable
private fun MedianOnlyNoTrendPreview() {
    val clusters = previewClustersMedianOnly()
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MedianDisplay(median = previewMedian(clusters), trend = null)
        }
    }
}

@Preview(showBackground = true, name = "Median only — no chart, with trend")
@Composable
private fun MedianOnlyWithTrendPreview() {
    val clusters = previewClustersMedianOnlyWithTrend()
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MedianDisplay(median = previewMedian(clusters), trend = previewTrend(clusters))
        }
    }
}

@Preview(showBackground = true, name = "Chart + median — no trend")
@Composable
private fun ChartAndMedianNoTrendPreview() {
    val clusters = previewClustersChartAndMedianNoTrend()
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ClusterChartContent(
                nonEmpty = clusters,
                referenceDates = previewDates(clusters),
                median = previewMedian(clusters),
                trend = null,
                model = previewModel(clusters),
            )
        }
    }
}

@Preview(showBackground = true, name = "Chart + median + trend — all clusters")
@Composable
private fun ChartAndMedianWithTrendAllClustersPreview() {
    val clusters = previewClusters14Days(*DayCluster.entries.toTypedArray())
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

@Preview(showBackground = true, name = "Chart + median + trend — two clusters")
@Composable
private fun ChartAndMedianWithTrendTwoClustersPreview() {
    val clusters = previewClusters14Days(DayCluster.Eos, DayCluster.Selene)
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
private fun DailyAverageChart(dailyAverages: List<DailyAverage>, median: Double?, trend: Double?) {
    if (dailyAverages.isEmpty()) return

    val modelProducer = remember(dailyAverages) { CartesianChartModelProducer() }

    LaunchedEffect(dailyAverages, median, trend) {
        modelProducer.runTransaction {
            lineSeries {
                series(dailyAverages.map { it.weightKg })
            }
            extras {
                if (median != null) it[medianKey] = median
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
            decorations = listOfNotNull(medianDecoration),
        ),
        modelProducer = modelProducer,
        scrollState = scrollState,
        zoomState = zoomState,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}
