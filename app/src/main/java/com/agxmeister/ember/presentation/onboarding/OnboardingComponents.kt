package com.agxmeister.ember.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agxmeister.ember.presentation.common.IntWheelPicker

/** Shared onboarding chrome: dark accent theme, page padding, and the step progress bar. */
@Composable
internal fun OnboardingScaffold(
    totalSteps: Int,
    currentStep: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = onBg,
            onPrimary = bg,
            secondaryContainer = Color(0xFF282828),
            onSecondaryContainer = Color.White,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepProgressBar(totalSteps = totalSteps, currentStep = currentStep)
            Spacer(Modifier.height(32.dp))
            content()
        }
    }
}

/** Title + supporting subtitle shown at the top of each onboarding step. */
@Composable
internal fun StepHeader(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        subtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
}

/** Centered HH:MM wheel pickers. */
@Composable
internal fun TimePickerRow(
    hour: Int,
    minute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IntWheelPicker(
            initialValue = hour,
            range = 0..23,
            label = { "%02d".format(it) },
            onValueChanged = onHourChanged,
            modifier = Modifier.width(100.dp),
        )
        Text(
            ":",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        IntWheelPicker(
            initialValue = minute,
            range = 0..59,
            label = { "%02d".format(it) },
            onValueChanged = onMinuteChanged,
            modifier = Modifier.width(100.dp),
        )
    }
}

@Composable
private fun StepProgressBar(totalSteps: Int, currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index < currentStep) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}
