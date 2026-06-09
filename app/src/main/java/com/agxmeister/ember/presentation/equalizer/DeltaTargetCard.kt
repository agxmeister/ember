package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor
import kotlin.math.abs

@Composable
internal fun DeltaTargetCard(
    modifier: Modifier = Modifier,
    weeklyAvg: Double?,
    targetKg: Double,
    tolerance: Double,
    weightUnit: WeightUnit,
) {
    val darkTheme = isSystemInDarkTheme()
    StatCard(
        modifier = modifier,
        label = appString(R.string.stat_delta_target),
    ) {
        val deltaDisplay = weeklyAvg?.let { abs(weightUnit.scaleDiff(it - targetKg)) }
        val deltaColor = weeklyAvg?.let { w ->
            closenessColor((1.0 - abs(w - targetKg) / tolerance).coerceIn(0.0, 1.0).toFloat(), darkTheme)
        } ?: MaterialTheme.colorScheme.onSurface
        StatValueRow(
            value = deltaDisplay?.let { "%.1f".format(it) } ?: "−",
            unit = deltaDisplay?.let { weightUnit.label },
            color = deltaColor,
        )
    }
}
