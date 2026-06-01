package com.agxmeister.ember.presentation.equalizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
internal fun EqualizerCard(
    modifier: Modifier = Modifier,
    days: List<EqualizerDayData>,
    targetKg: Double,
    tolerance: Double,
    weightUnit: WeightUnit,
    today: LocalDate,
    selectedDate: LocalDate?,
    isWeekly: Boolean,
    trendLine: TrendLineData?,
    canScrollLeft: Boolean,
    canScrollRight: Boolean,
    todayColumnProgress: Float = 1f,
    onDayToggle: (LocalDate) -> Unit,
    onScroll: (Int) -> Unit,
) {
    val weights = days.mapNotNull { it.rawWeightKg ?: it.weightKg }
    val allValues = if (weights.isEmpty()) listOf(targetKg - 2.0, targetKg + 2.0) else weights + targetKg
    val minW = allValues.min()
    val maxW = allValues.max()
    val span = (maxW - minW).coerceAtLeast(4.0)
    val pad = (span * 0.18).coerceAtLeast(1.5)
    val yMin = floor((minW - pad) * 2) / 2
    val yMax = ceil((maxW + pad) * 2) / 2

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val coroutineScope = rememberCoroutineScope()
    val animatedOffset = remember { Animatable(0f) }

    val tgtPrefix = appString(R.string.trends_tgt_prefix)
    val darkTheme = isSystemInDarkTheme()
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val onCard = MaterialTheme.colorScheme.onSurface
    val dimColor = onCard.copy(alpha = 0.18f)
    val onSurfaceArrow = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(days, canScrollLeft, canScrollRight) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val velocityTracker = VelocityTracker()
                            velocityTracker.addPosition(down.uptimeMillis, down.position)
                            val startX = down.position.x
                            val startY = down.position.y
                            var isHorizontal = false
                            var isVertical = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                val dx = change.position.x - startX
                                val dy = change.position.y - startY
                                if (!isHorizontal && !isVertical && (abs(dx) > 8.dp.toPx() || abs(dy) > 8.dp.toPx())) {
                                    if (abs(dx) > abs(dy)) isHorizontal = true else isVertical = true
                                }
                                if (isHorizontal) change.consume()
                                if (!change.pressed) {
                                    if (!isHorizontal && !isVertical) {
                                        val leftPad = 8.dp.toPx()
                                        val innerWidth = size.width.toFloat() - leftPad * 2
                                        val colWidth = innerWidth / 14f
                                        val colIdx = ((startX - leftPad) / colWidth).toInt()
                                        if (colIdx in 0..13) onDayToggle(days[colIdx].date)
                                    } else if (isHorizontal) {
                                        val velocity = velocityTracker.calculateVelocity()
                                        val colWidth = (size.width - 16.dp.toPx()) / 14f
                                        val rawDelta = (abs(velocity.x) / colWidth * 0.4f).roundToInt().coerceIn(1, 10)
                                        val totalDx = change.position.x - startX
                                        if (totalDx > 0 && canScrollLeft) {
                                            onScroll(rawDelta)
                                            coroutineScope.launch {
                                                animatedOffset.snapTo(-rawDelta * colWidth)
                                                animatedOffset.animateTo(0f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                                            }
                                        } else if (totalDx < 0 && canScrollRight) {
                                            onScroll(-rawDelta)
                                            coroutineScope.launch {
                                                animatedOffset.snapTo(rawDelta * colWidth)
                                                animatedOffset.animateTo(0f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    },
            ) {
                translate(left = animatedOffset.value) {
                    val leftPad = 8.dp.toPx()
                    val rightPad = 8.dp.toPx()
                    val topPad = 20.dp.toPx()
                    val bottomPad = 28.dp.toPx()

                    val innerWidth = size.width - leftPad - rightPad
                    val innerHeight = size.height - topPad - bottomPad
                    val colWidth = innerWidth / 14f

                    val dashUnit = innerHeight / 48f
                    val dashH = dashUnit * 0.68f
                    val maxDashW = (colWidth * 0.72f).coerceAtMost(10.dp.toPx())

                    val dashAreaBottom = size.height - bottomPad
                    val dashAreaTop = dashAreaBottom - innerHeight

                    val targetFrac = ((targetKg - yMin) / (yMax - yMin)).coerceIn(0.0, 1.0).toFloat()
                    val targetY = dashAreaBottom - targetFrac * innerHeight
                    val tgtLabelText = "$tgtPrefix %.1f".format(weightUnit.fromKg(targetKg))
                    val tgtLayout = textMeasurer.measure(
                        tgtLabelText,
                        TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = onCard.copy(alpha = 0.60f)),
                    )
                    val tgtLabelW = tgtLayout.size.width.toFloat() + 8.dp.toPx()
                    val lineEndX = size.width - rightPad - tgtLabelW

                    days.forEachIndexed { idx, day ->
                        val colLeft = leftPad + idx * colWidth
                        val colCenterX = colLeft + colWidth / 2f
                        val isToday = day.date == today
                        val isSelected = day.date == selectedDate
                        val displayWeight = day.rawWeightKg ?: day.weightKg
                        val hasData = displayWeight != null

                        val c = if (hasData) {
                            (1.0 - abs(displayWeight!! - targetKg) / tolerance).coerceIn(0.0, 1.0).toFloat()
                        } else 0f
                        val litColor = closenessColor(c, darkTheme)

                        val litCount = if (hasData) {
                            val frac = (displayWeight!! - yMin) / (yMax - yMin)
                            val full = (frac * 48).roundToInt().coerceIn(1, 48)
                            if (isToday) (full * todayColumnProgress).roundToInt().coerceIn(0, full) else full
                        } else 0

                        if (isSelected && !isToday) {
                            drawRect(
                                color = onCard.copy(alpha = 0.04f),
                                topLeft = Offset(colLeft + 1.dp.toPx(), dashAreaTop - 4.dp.toPx()),
                                size = Size(colWidth - 2.dp.toPx(), innerHeight + 8.dp.toPx()),
                            )
                        }

                        val dashW = maxDashW
                        val dashX = colLeft + (colWidth - dashW) / 2f

                        for (di in 0 until 48) {
                            val isLit = di < litCount
                            val isTip = isLit && di == litCount - 1 && (isToday || isSelected)
                            val yBottom = dashAreaBottom - di * dashUnit
                            val yTop = yBottom - dashH

                            if (isLit) {
                                val w = if (isTip) dashW * 1.18f else dashW
                                val x = if (isTip) dashX - (w - dashW) / 2f else dashX
                                drawRect(color = litColor.copy(alpha = 0.14f), topLeft = Offset(x - 3f, yTop - 2f), size = Size(w + 6f, dashH + 4f))
                                drawRect(color = litColor.copy(alpha = 0.28f), topLeft = Offset(x - 1f, yTop - 1f), size = Size(w + 2f, dashH + 2f))
                                drawRect(color = litColor, topLeft = Offset(x, yTop), size = Size(w, dashH))
                            } else {
                                drawRect(color = dimColor, topLeft = Offset(dashX, yTop), size = Size(dashW, dashH))
                            }
                        }

                        val glyphY = topPad / 2f
                        when {
                            isSelected -> {
                                val r = 4.dp.toPx()
                                val path = Path().apply {
                                    moveTo(colCenterX, glyphY - r)
                                    lineTo(colCenterX + r, glyphY)
                                    lineTo(colCenterX, glyphY + r)
                                    lineTo(colCenterX - r, glyphY)
                                    close()
                                }
                                drawPath(path, color = onCard.copy(alpha = 0.75f))
                            }
                            isToday -> {
                                val todayGreen = Color(0xFF4BB543)
                                drawCircle(color = todayGreen.copy(alpha = 0.22f), radius = 7.dp.toPx(), center = Offset(colCenterX, glyphY))
                                drawCircle(color = todayGreen, radius = 3.5.dp.toPx(), center = Offset(colCenterX, glyphY))
                            }
                        }

                        val dayLabel = if (isWeekly) {
                            day.date.month.name.take(1) + day.date.dayOfMonth.toString().padStart(2, '0')
                        } else {
                            day.date.dayOfMonth.toString().padStart(2, '0')
                        }
                        val labelLayout = textMeasurer.measure(
                            dayLabel,
                            TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (isToday || isSelected) onCard.copy(alpha = 0.90f) else onCard.copy(alpha = 0.28f),
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                        drawText(labelLayout, topLeft = Offset(colCenterX - labelLayout.size.width / 2f, dashAreaBottom + 5.dp.toPx()))
                    }

                    drawLine(
                        color = onCard.copy(alpha = 0.45f),
                        start = Offset(leftPad, targetY),
                        end = Offset(lineEndX, targetY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())),
                    )
                    drawRect(
                        color = cardBg,
                        topLeft = Offset(lineEndX, targetY - tgtLayout.size.height / 2f - 2.dp.toPx()),
                        size = Size(tgtLabelW, tgtLayout.size.height.toFloat() + 4.dp.toPx()),
                    )
                    drawText(
                        tgtLayout,
                        topLeft = Offset(lineEndX + 4.dp.toPx(), targetY - tgtLayout.size.height / 2f),
                    )

                    trendLine?.let { tl ->
                        val startFrac = ((tl.startKg - yMin) / (yMax - yMin)).coerceIn(0.0, 1.0).toFloat()
                        val endFrac = ((tl.endKg - yMin) / (yMax - yMin)).coerceIn(0.0, 1.0).toFloat()
                        val startY = dashAreaBottom - startFrac * innerHeight
                        val endY = dashAreaBottom - endFrac * innerHeight
                        drawLine(
                            color = Color.Red.copy(alpha = 0.75f),
                            start = Offset(leftPad, startY),
                            end = Offset(size.width - rightPad, endY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())),
                        )
                    }
                }
            }
        }

        if (canScrollLeft) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = onSurfaceArrow.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp),
            )
        }
        if (canScrollRight) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = onSurfaceArrow.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp),
            )
        }
    }
}
