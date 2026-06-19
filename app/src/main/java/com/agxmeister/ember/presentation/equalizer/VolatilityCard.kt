package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        helpKey = "stat_volatility",
    ) {
        val display = volatilityKg?.let { weightUnit.scaleDiff(it) }
        val color = volatilityKg?.let {
            closenessColor((1.0 - it / VOLATILITY_REFERENCE_KG).coerceIn(0.0, 1.0).toFloat(), darkTheme)
        } ?: MaterialTheme.colorScheme.onSurface
        StatValueRow(
            value = display?.let { formatVolatility(it) } ?: "−",
            unit = display?.let { weightUnit.label },
            color = color,
        )
    }
}

private fun formatVolatility(value: Double): String =
    "%.2f".format(value).let { if (value < 1.0) it.removePrefix("0") else it }
