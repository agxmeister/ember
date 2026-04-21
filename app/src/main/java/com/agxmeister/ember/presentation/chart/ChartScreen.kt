package com.agxmeister.ember.presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.DailyCandle
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.theme.EmberTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.candlestickSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.absolute
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.DecimalFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Duration.Companion.days

// ──── Chart layout ────
private const val WINDOW_DAYS = 7
private const val LAST_INDEX = WINDOW_DAYS - 1
private val ChartHeight = 300.dp
private val CandleBodyThickness = 8.dp

// ──── Colors ────
private val ProgressColor = Color(0xFF4CAF50)
private val RegressColor = Color(0xFFE53935)
private val NeutralCandleColor = Color(0xFF9E9E9E)
private val MedianLineColor = Color(0xFF29B6F6)

// ──── Formatters ────
private val oneDecimal = DecimalFormat("0.0")
private val weightValueFormatter = CartesianValueFormatter.decimal(DecimalFormat("0.0;−0.0"))
private val yStepKey = ExtraStore.Key<Double>()

// 20% vertical padding above and below the data extents keeps candles visually centered.
private val weightRangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        minY - yPadding(minY, maxY)
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        maxY + yPadding(minY, maxY)
    private fun yPadding(minY: Double, maxY: Double) = (maxY - minY).coerceAtLeast(1.0) * 0.2
}

private fun monthAbbr(date: LocalDate): String =
    date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

private fun formatTrend(trend: Double): String {
    val formatted = oneDecimal.format(abs(trend))
    return if (trend < 0) "−$formatted" else "+$formatted"
}

private fun yStepFor(amplitude: Double): Double = when {
    amplitude < 1.0 -> 0.2
    amplitude < 3.0 -> 0.5
    amplitude < 7.0 -> 1.0
    else -> 2.0
}

// Vico derives minX/maxX from actual data. To always render the full WINDOW_DAYS span
// we anchor the median line at x=0 and x=LAST_INDEX using the nearest known median value.
private fun anchoredMedianPoints(points: List<Pair<Int, Double>>): List<Pair<Int, Double>> {
    if (points.isEmpty()) return emptyList()
    return buildList {
        if (points.first().first > 0) add(0 to points.first().second)
        addAll(points)
        if (points.last().first < LAST_INDEX) add(LAST_INDEX to points.last().second)
    }
}

@Composable
fun ChartScreen(onNavigateToHome: () -> Unit = {}, viewModel: ChartViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ChartUiState.Empty -> EmptyChartScreen(onNavigateToHome)
        is ChartUiState.Candle -> Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            when {
                state.showChart -> CandleChart(
                    candles = state.candles,
                    showMedianLine = state.showMedianLine,
                    weightGoal = state.weightGoal,
                    weightUnit = state.weightUnit,
                )
                state.median != null -> MedianDisplay(
                    median = state.median,
                    trend = state.trend,
                    weightGoal = state.weightGoal,
                    weightUnit = state.weightUnit,
                )
                else -> WarmUpScreen(
                    candles = state.candles,
                    recentCount = state.recentCount,
                    weightUnit = state.weightUnit,
                )
            }
        }
    }
}

@Composable
private fun EmptyChartScreen(onNavigateToHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 256.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "No data yet. Start by adding your weight on the Home screen.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onNavigateToHome) { Text("Go to Home") }
        }
    }
}

@Composable
private fun MedianDisplay(median: Double, trend: Double?, weightGoal: WeightGoal, weightUnit: WeightUnit = WeightUnit.Kg) {
    val displayMedian = weightUnit.fromKg(median)
    val displayTrend = trend?.let { weightUnit.scaleDiff(it) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Current median",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${oneDecimal.format(displayMedian)} ${weightUnit.label}",
            style = MaterialTheme.typography.displaySmall,
        )
        if (displayTrend != null) {
            val isProgress = when (weightGoal) {
                WeightGoal.Decrease -> displayTrend <= 0
                WeightGoal.Increase -> displayTrend >= 0
            }
            Text(
                text = "${formatTrend(displayTrend)} vs prev. week",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isProgress) ProgressColor else RegressColor,
            )
        }
    }
}

@Composable
private fun CandleChart(
    candles: List<DailyCandle>,
    showMedianLine: Boolean,
    weightGoal: WeightGoal,
    weightUnit: WeightUnit = WeightUnit.Kg,
) {
    if (candles.isEmpty()) return

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val allDates = remember(today) { (LAST_INDEX downTo 0).map { today.minus(DatePeriod(days = it)) } }
    val dateToIndex = remember(allDates) { allDates.withIndex().associate { (i, d) -> d to i } }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(candles, weightUnit) {
        val indexed = candles.mapNotNull { c -> dateToIndex[c.date]?.let { i -> i to c } }
        if (indexed.isEmpty()) return@LaunchedEffect
        modelProducer.populateFrom(indexed, weightUnit)
    }

    CandleChartContent(
        allDates = allDates,
        showMedianLine = showMedianLine,
        weightGoal = weightGoal,
        modelProducer = modelProducer,
    )
}

private suspend fun CartesianChartModelProducer.populateFrom(
    indexed: List<Pair<Int, DailyCandle>>,
    weightUnit: WeightUnit,
) {
    val highs = indexed.map { weightUnit.fromKg(it.second.high) }
    val lows = indexed.map { weightUnit.fromKg(it.second.low) }
    val medianPoints = anchoredMedianPoints(
        indexed.map { it.first to weightUnit.fromKg(it.second.rollingMedian) }
    )
    runTransaction {
        candlestickSeries(
            x = indexed.map { it.first },
            opening = indexed.map { weightUnit.fromKg(it.second.open) },
            closing = indexed.map { weightUnit.fromKg(it.second.close) },
            low = lows,
            high = highs,
        )
        lineSeries {
            series(x = medianPoints.map { it.first }, y = medianPoints.map { it.second })
        }
        extras { it[yStepKey] = yStepFor(highs.max() - lows.min()) }
    }
}

@Composable
private fun CandleChartContent(
    allDates: List<LocalDate>,
    showMedianLine: Boolean,
    weightGoal: WeightGoal,
    modelProducer: CartesianChartModelProducer,
) {
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberCandlestickCartesianLayer(
                candleProvider = rememberCandleProvider(weightGoal),
                rangeProvider = weightRangeProvider,
            ),
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(rememberMedianLine(showMedianLine)),
                rangeProvider = weightRangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = weightValueFormatter,
                itemPlacer = VerticalAxis.ItemPlacer.step(step = { it.getOrNull(yStepKey) }),
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(textSize = 10.sp),
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                    shiftExtremeLines = false,
                    spacing = { 1 },
                    addExtremeLabelPadding = false,
                ),
                valueFormatter = { _, x, _ ->
                    allDates.getOrNull(x.toInt())?.let { "${monthAbbr(it)} ${it.dayOfMonth}" } ?: ""
                },
            ),
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(
            initialScroll = Scroll.Absolute.End,
            autoScroll = Scroll.Absolute.End,
            autoScrollCondition = AutoScrollCondition.OnModelGrowth,
        ),
        zoomState = rememberVicoZoomState(initialZoom = Zoom.max(Zoom.x(8.0), Zoom.Content)),
        modifier = Modifier.fillMaxWidth().height(ChartHeight),
    )
}

@Composable
private fun rememberCandleProvider(weightGoal: WeightGoal): CandlestickCartesianLayer.CandleProvider {
    val (bullishColor, bearishColor) = when (weightGoal) {
        WeightGoal.Decrease -> RegressColor to ProgressColor
        WeightGoal.Increase -> ProgressColor to RegressColor
    }
    val bullishBody = rememberLineComponent(fill = fill(bullishColor), thickness = CandleBodyThickness)
    val bearishBody = rememberLineComponent(fill = fill(bearishColor), thickness = CandleBodyThickness)
    val neutralBody = rememberLineComponent(fill = fill(NeutralCandleColor), thickness = CandleBodyThickness)
    return remember(bullishBody, bearishBody, neutralBody) {
        CandlestickCartesianLayer.CandleProvider.absolute(
            bullish = CandlestickCartesianLayer.Candle(bullishBody),
            neutral = CandlestickCartesianLayer.Candle(neutralBody),
            bearish = CandlestickCartesianLayer.Candle(bearishBody),
        )
    }
}

// The line series is always registered (it anchors Vico's x-range to the full window).
// When the median should stay hidden, we render it transparent rather than omitting the layer.
@Composable
private fun rememberMedianLine(showMedianLine: Boolean): LineCartesianLayer.Line {
    val color = if (showMedianLine) MedianLineColor else Color.Transparent
    return LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(color)),
        pointConnector = remember { LineCartesianLayer.PointConnector.cubic() },
    )
}

@Composable
private fun WarmUpScreen(candles: List<DailyCandle>, recentCount: Int, weightUnit: WeightUnit) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val motivational = when (recentCount) {
        1 -> "One is done, two to go!"
        2 -> "Only one more to go!"
        else -> "Keep it up!"
    }
    val measurementText = candles
        .sortedByDescending { it.date }
        .take(3)
        .sortedBy { it.date }
        .joinToString(", ") { candle ->
            val weight = oneDecimal.format(weightUnit.fromKg(candle.close))
            "$weight ${dayLabel(today, candle.date)}"
        }

    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 256.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "The chart will appear after 3 check-ins. $motivational",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = measurementText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun dayLabel(today: LocalDate, date: LocalDate): String = when (date) {
    today -> "today"
    today.minus(DatePeriod(days = 1)) -> "yesterday"
    today.minus(DatePeriod(days = 2)) -> "two days ago"
    else -> "${monthAbbr(date)} ${date.dayOfMonth}"
}

private fun previewCandles(days: Int, today: LocalDate): List<DailyCandle> {
    val fluctuation = listOf(0.0, 0.3, -0.2, 0.5, -0.1, 0.4, -0.3)
    return (days - 1 downTo 0).map { daysAgo ->
        val base = 80.0 + fluctuation[daysAgo % fluctuation.size]
        val prevBase = 80.0 + fluctuation[(daysAgo + 1) % fluctuation.size]
        DailyCandle(
            date = today.minus(DatePeriod(days = daysAgo)),
            open = prevBase,
            close = base,
            high = maxOf(prevBase, base) + 0.2,
            low = minOf(prevBase, base) - 0.2,
            rollingMedian = base,
        )
    }
}

// Vico delivers the model by notifying already-subscribed observers.
// CartesianChartHost subscribes after composition, so we must populate
// the producer via LaunchedEffect (after the host subscribes), not before.
// Use the ▶ interactive preview button to see the chart rendered.
@Preview(showBackground = true, name = "Candle chart")
@Composable
private fun CandleChartPreview() {
    EmberTheme {
        val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
        val allDates = remember { (LAST_INDEX downTo 0).map { today.minus(DatePeriod(days = it)) } }
        val candles = remember { previewCandles(7, today) }
        val dateToIndex = remember { allDates.withIndex().associate { (i, d) -> d to i } }
        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(Unit) {
            val indexed = candles.mapNotNull { c -> dateToIndex[c.date]?.let { i -> i to c } }
            if (indexed.isNotEmpty()) modelProducer.populateFrom(indexed, WeightUnit.Kg)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            CandleChartContent(
                allDates = allDates,
                showMedianLine = true,
                weightGoal = WeightGoal.Decrease,
                modelProducer = modelProducer,
            )
        }
    }
}

@Preview(showBackground = true, name = "Warm-up (2 check-ins)")
@Composable
private fun WarmUpPreview() {
    EmberTheme {
        val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
        Column(modifier = Modifier.padding(16.dp)) {
            WarmUpScreen(candles = previewCandles(days = 2, today), recentCount = 2, weightUnit = WeightUnit.Kg)
        }
    }
}

@Preview(showBackground = true, name = "Median display")
@Composable
private fun MedianDisplayPreview() {
    EmberTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MedianDisplay(median = 80.0, trend = -0.5, weightGoal = WeightGoal.Decrease)
        }
    }
}
