package com.agxmeister.ember.presentation.onboarding

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.agxmeister.ember.domain.model.WeightGoal
import com.agxmeister.ember.presentation.theme.EmberTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state.step) {
            0 -> WeightStep(
                weightKg = state.weightKg,
                onWeightChanged = viewModel::onWeightChanged,
                onNext = viewModel::onNextStep,
            )
            1 -> GoalStep(
                goal = state.weightGoal,
                onGoalChanged = viewModel::onWeightGoalChanged,
                onNext = viewModel::onNextStep,
            )
            2 -> TimeStep(
                hour = state.dayStartHour,
                minute = state.dayStartMinute,
                onHourChanged = viewModel::onDayStartHourChanged,
                onMinuteChanged = viewModel::onDayStartMinuteChanged,
                onNext = viewModel::onNextStep,
            )
            3 -> ClusteringStep(
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

@Composable
private fun ColumnScope.WeightStep(
    weightKg: Double,
    onWeightChanged: (Double) -> Unit,
    onNext: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(weightKg.toInt().toString()) }
    val isValid = text.isNotEmpty() && text.toIntOrNull()?.let { it in 30..300 } == true
    val borderColor = MaterialTheme.colorScheme.outline

    Text("Welcome to Ember", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        "What's your current weight, roughly?",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    Spacer(Modifier.weight(1f))
    BasicTextField(
        value = text,
        onValueChange = { input ->
            if (input.length <= 3 && input.all { it.isDigit() }) {
                text = input
                input.toIntOrNull()?.let { onWeightChanged(it.toDouble()) }
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
    Spacer(Modifier.height(8.dp))
    Text(
        text = "kg",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    )
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, enabled = isValid, modifier = Modifier.fillMaxWidth()) {
        Text("Next")
    }
}

@Composable
private fun ColumnScope.GoalStep(
    goal: WeightGoal,
    onGoalChanged: (WeightGoal) -> Unit,
    onNext: () -> Unit,
) {
    Text("Your goal", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        "This lets Ember colour your trends correctly — green for progress, red for the wrong direction.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    Spacer(Modifier.weight(1f))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoalOption(
            label = "Lose weight",
            description = "I want to decrease my weight",
            selected = goal == WeightGoal.Decrease,
            onClick = { onGoalChanged(WeightGoal.Decrease) },
        )
        GoalOption(
            label = "Gain weight",
            description = "I want to increase my weight",
            selected = goal == WeightGoal.Increase,
            onClick = { onGoalChanged(WeightGoal.Increase) },
        )
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text("Next")
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
private fun ColumnScope.TimeStep(
    hour: Int,
    minute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Text("Morning reminder", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        "When do you usually start your day?",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
    Text(
        "You'll be reminded to weigh in 15 minutes later.",
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
    Spacer(Modifier.weight(1f))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text("Next")
    }
}

@Composable
private fun ColumnScope.ClusteringStep(
    clusteringEnabled: Boolean,
    onClusteringEnabledChanged: (Boolean) -> Unit,
    onDone: () -> Unit,
) {
    Text("Smart tracking", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        "Ember can group your measurements by time of day — morning, midday, evening, and night. " +
            "This lets you compare like with like, giving you a clearer picture of your actual trend " +
            "rather than natural daily fluctuations.",
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
            Text("Time-of-day clustering", style = MaterialTheme.typography.bodyLarge)
            Text(
                if (clusteringEnabled) "On — trends shown per time slot" else "Off — daily averages shown",
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
        Text("Get started")
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
                onWeightChanged = {},
                onNext = {},
            )
        }
    }
}

private val ITEM_HEIGHT: Dp = 56.dp

@Composable
private fun IntWheelPicker(
    initialValue: Int,
    range: IntRange,
    label: (Int) -> String,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = range.toList()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (initialValue - range.first).coerceIn(0, items.size - 1))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }

    LaunchedEffect(selectedIndex) {
        val value = items.getOrNull(selectedIndex) ?: items.last()
        onValueChanged(value)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(ITEM_HEIGHT * 3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(items.size) { idx ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label(items[idx]),
                        style = if (idx == selectedIndex) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (idx == selectedIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        },
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = ITEM_HEIGHT))
        HorizontalDivider(modifier = Modifier.padding(bottom = ITEM_HEIGHT))
    }
}
