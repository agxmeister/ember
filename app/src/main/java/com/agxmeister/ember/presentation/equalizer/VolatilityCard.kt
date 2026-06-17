package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor

private const val VOLATILITY_REFERENCE_KG = 1.0

@Composable
internal fun VolatilityCard(
    modifier: Modifier = Modifier,
    volatilityKg: Double?,
    weightUnit: WeightUnit,
    isWeekly: Boolean,
) {
    val darkTheme = isSystemInDarkTheme()
    StatCard(
        modifier = modifier,
        label = appString(R.string.stat_volatility),
        info = appString(if (isWeekly) R.string.stat_volatility_info_weekly else R.string.stat_volatility_info_daily),
    ) {
        val display = volatilityKg?.let { weightUnit.scaleDiff(it) }
        val color = volatilityKg?.let {
            closenessColor((1.0 - it / VOLATILITY_REFERENCE_KG).coerceIn(0.0, 1.0).toFloat(), darkTheme)
        } ?: MaterialTheme.colorScheme.onSurface
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (display != null) {
                VariationArrows(color = color, modifier = Modifier.padding(end = 5.dp))
            }
            StatValueRow(
                value = display?.let { formatVolatility(it) } ?: "−",
                unit = display?.let { weightUnit.label },
                color = color,
            )
        }
    }
}

private fun formatVolatility(value: Double): String =
    "%.2f".format(value).let { if (value < 1.0) it.removePrefix("0") else it }

@Composable
private fun VariationArrows(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 11.dp, height = 24.dp)) {
        val w = size.width
        val h = size.height
        val gap = 3.dp.toPx()
        val halfH = (h - gap) / 2f
        val up = Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(0f, halfH)
            lineTo(w, halfH)
            close()
        }
        drawPath(up, color)
        val down = Path().apply {
            moveTo(0f, halfH + gap)
            lineTo(w, halfH + gap)
            lineTo(w / 2f, h)
            close()
        }
        drawPath(down, color)
    }
}
