package com.agxmeister.ember.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.presentation.common.IntWheelPicker
import com.agxmeister.ember.presentation.theme.EmberTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import com.agxmeister.ember.presentation.appString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
        when (state.step) {
            0 -> WeightStep(
                weightKg = state.weightKg,
                weightUnit = state.weightUnit,
                onWeightChanged = viewModel::onWeightChanged,
                onUnitChanged = viewModel::onWeightUnitChanged,
                onNext = viewModel::onNextStep,
            )
            1 -> GoalTargetStep(
                weightKg = state.weightKg,
                goalTargetKg = state.goalTargetKg,
                weightUnit = state.weightUnit,
                onGoalTargetChanged = viewModel::onGoalTargetChanged,
                onNext = viewModel::onNextStep,
            )
            2 -> FrequencyStep(
                frequency = state.weighingFrequency,
                onFrequencyChanged = viewModel::onWeighingFrequencyChanged,
                onNext = viewModel::onNextStep,
            )
            3 -> TimeStep(
                frequency = state.weighingFrequency,
                dayStartHour = state.dayStartHour,
                dayStartMinute = state.dayStartMinute,
                notificationDayOfWeek = state.notificationDayOfWeek,
                notificationHour = state.notificationHour,
                notificationMinute = state.notificationMinute,
                onDayStartHourChanged = viewModel::onDayStartHourChanged,
                onDayStartMinuteChanged = viewModel::onDayStartMinuteChanged,
                onNotificationDayOfWeekChanged = viewModel::onNotificationDayOfWeekChanged,
                onNotificationHourChanged = viewModel::onNotificationHourChanged,
                onNotificationMinuteChanged = viewModel::onNotificationMinuteChanged,
                onNext = viewModel::onNextStep,
            )
            4 -> ClusteringStep(
                clusteringEnabled = state.clusteringEnabled,
                onClusteringEnabledChanged = viewModel::onClusteringEnabledChanged,
                onDone = {
                    viewModel.complete()
                    onComplete()
                },
            )
        }
    }
    }
}

@Composable
private fun ColumnScope.WeightStep(
    weightKg: Double,
    weightUnit: WeightUnit,
    onWeightChanged: (Double) -> Unit,
    onUnitChanged: (WeightUnit) -> Unit,
    onNext: () -> Unit,
) {
    var text by rememberSaveable(weightUnit) {
        mutableStateOf(weightUnit.fromKg(weightKg).toInt().toString())
    }
    val isValid = text.isNotEmpty() && text.toIntOrNull()?.let { it in weightUnit.displayRange } == true
    val borderColor = MaterialTheme.colorScheme.outline

    Text(appString(R.string.onboarding_welcome), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        appString(R.string.onboarding_current_weight_question),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    Spacer(Modifier.weight(1f))
    BasicTextField(
        value = text,
        onValueChange = { input ->
            if (input.length <= 3 && input.all { it.isDigit() }) {
                text = input
                input.toIntOrNull()?.let { onWeightChanged(weightUnit.toKg(it.toDouble())) }
            }
        },
        textStyle = MaterialTheme.typography.displayLarge.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .width(220.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            },
    )
    Spacer(Modifier.height(16.dp))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.width(200.dp)) {
        WeightUnit.entries.forEachIndexed { index, unit ->
            SegmentedButton(
                selected = weightUnit == unit,
                onClick = { onUnitChanged(unit) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = WeightUnit.entries.size),
            ) { Text(unit.label) }
        }
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, enabled = isValid, modifier = Modifier.fillMaxWidth()) {
        Text(appString(R.string.label_next))
    }
}

@Composable
private fun ColumnScope.GoalTargetStep(
    weightKg: Double,
    goalTargetKg: Double,
    weightUnit: WeightUnit,
    onGoalTargetChanged: (Double) -> Unit,
    onNext: () -> Unit,
) {
    val currentDisplay = weightUnit.fromKg(weightKg).toInt()
    val targetDisplay = weightUnit.fromKg(goalTargetKg).toInt()
    val diffKg = goalTargetKg - weightKg
    val diffDisplay = weightUnit.scaleDiff(kotlin.math.abs(diffKg)).toInt()

    Text(appString(R.string.onboarding_target_weight_title), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        appString(R.string.onboarding_goal_question),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    if (diffDisplay > 0) {
        val hint = if (diffKg < 0) appString(R.string.onboarding_lose, diffDisplay, weightUnit.label)
                   else appString(R.string.onboarding_gain, diffDisplay, weightUnit.label)
        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    } else {
        Text(
            appString(R.string.onboarding_same_weight),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
    }
    Spacer(Modifier.weight(1f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IntWheelPicker(
            initialValue = targetDisplay,
            range = weightUnit.displayRange,
            label = { "$it ${weightUnit.label}" },
            onValueChanged = { onGoalTargetChanged(weightUnit.toKg(it.toDouble())) },
            modifier = Modifier.width(200.dp),
        )
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(appString(R.string.label_next))
    }
}

@Composable
private fun GoalOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FrequencyStep(
    frequency: WeighingFrequency,
    onFrequencyChanged: (WeighingFrequency) -> Unit,
    onNext: () -> Unit,
) {
    Text(appString(R.string.onboarding_how_often), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        appString(R.string.onboarding_frequency_description),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    Spacer(Modifier.weight(1f))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoalOption(
            label = appString(R.string.label_daily),
            description = appString(R.string.onboarding_daily_description),
            selected = frequency == WeighingFrequency.Daily,
            onClick = { onFrequencyChanged(WeighingFrequency.Daily) },
        )
        GoalOption(
            label = appString(R.string.label_weekly),
            description = appString(R.string.onboarding_weekly_description),
            selected = frequency == WeighingFrequency.Weekly,
            onClick = { onFrequencyChanged(WeighingFrequency.Weekly) },
        )
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(appString(R.string.label_next))
    }
}

@Composable
private fun ColumnScope.TimeStep(
    frequency: WeighingFrequency,
    dayStartHour: Int,
    dayStartMinute: Int,
    notificationDayOfWeek: Int,
    notificationHour: Int,
    notificationMinute: Int,
    onDayStartHourChanged: (Int) -> Unit,
    onDayStartMinuteChanged: (Int) -> Unit,
    onNotificationDayOfWeekChanged: (Int) -> Unit,
    onNotificationHourChanged: (Int) -> Unit,
    onNotificationMinuteChanged: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val dayLabels = listOf(
        appString(R.string.day_mon),
        appString(R.string.day_tue),
        appString(R.string.day_wed),
        appString(R.string.day_thu),
        appString(R.string.day_fri),
        appString(R.string.day_sat),
        appString(R.string.day_sun),
    )

    if (frequency == WeighingFrequency.Weekly) {
        Text(appString(R.string.onboarding_weekly_reminder_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            appString(R.string.onboarding_weekly_day_time),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(32.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = notificationDayOfWeek == index + 1,
                    onClick = { onNotificationDayOfWeekChanged(index + 1) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = dayLabels.size),
                ) { Text(label) }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntWheelPicker(
                initialValue = notificationHour,
                range = 0..23,
                label = { "%02d".format(it) },
                onValueChanged = onNotificationHourChanged,
                modifier = Modifier.width(100.dp),
            )
            Text(
                ":",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            IntWheelPicker(
                initialValue = notificationMinute,
                range = 0..59,
                label = { "%02d".format(it) },
                onValueChanged = onNotificationMinuteChanged,
                modifier = Modifier.width(100.dp),
            )
        }
    } else {
        Text(appString(R.string.onboarding_morning_reminder_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            appString(R.string.onboarding_day_start_question),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            appString(R.string.onboarding_reminder_15_min),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntWheelPicker(
                initialValue = dayStartHour,
                range = 0..23,
                label = { "%02d".format(it) },
                onValueChanged = onDayStartHourChanged,
                modifier = Modifier.width(100.dp),
            )
            Text(
                ":",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            IntWheelPicker(
                initialValue = dayStartMinute,
                range = 0..59,
                label = { "%02d".format(it) },
                onValueChanged = onDayStartMinuteChanged,
                modifier = Modifier.width(100.dp),
            )
        }
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(appString(R.string.label_next))
    }
}

@Composable
private fun ColumnScope.ClusteringStep(
    clusteringEnabled: Boolean,
    onClusteringEnabledChanged: (Boolean) -> Unit,
    onDone: () -> Unit,
) {
    Text(appString(R.string.onboarding_smart_tracking), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        appString(R.string.onboarding_clustering_description),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    Spacer(Modifier.weight(1f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(appString(R.string.label_clustering), style = MaterialTheme.typography.bodyLarge)
            Text(
                if (clusteringEnabled) appString(R.string.label_clustering_on) else appString(R.string.label_clustering_off),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        Switch(
            checked = clusteringEnabled,
            onCheckedChange = onClusteringEnabledChanged,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(appString(R.string.onboarding_get_started))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeightStepPreview() {
    EmberTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WeightStep(
                weightKg = 75.0,
                weightUnit = WeightUnit.Kg,
                onWeightChanged = {},
                onUnitChanged = {},
                onNext = {},
            )
        }
    }
}
