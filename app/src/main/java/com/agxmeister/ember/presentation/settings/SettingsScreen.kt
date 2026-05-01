package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.presentation.common.IntWheelPicker

private val DAY_LABELS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val clusteringEnabled by viewModel.clusteringEnabled.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val initialWeightKg by viewModel.initialWeightKg.collectAsStateWithLifecycle()
    val goalTargetKg by viewModel.goalTargetKg.collectAsStateWithLifecycle()
    val notificationHour by viewModel.notificationHour.collectAsStateWithLifecycle()
    val notificationMinute by viewModel.notificationMinute.collectAsStateWithLifecycle()
    val weighingFrequency by viewModel.weighingFrequency.collectAsStateWithLifecycle()
    val notificationDayOfWeek by viewModel.notificationDayOfWeek.collectAsStateWithLifecycle()

    var showTimePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Unit", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            WeightUnit.entries.forEachIndexed { index, unit ->
                SegmentedButton(
                    selected = weightUnit == unit,
                    onClick = { viewModel.onWeightUnitChanged(unit) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = WeightUnit.entries.size),
                ) { Text(unit.label) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Goal", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val effectiveTargetKg = if (goalTargetKg > 0) goalTargetKg else initialWeightKg
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTargetPicker = true }
                .padding(vertical = 8.dp),
        ) {
            Text(
                "Target weight is ${weightUnit.fromKg(effectiveTargetKg).toInt()} ${weightUnit.label}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "Tap to adjust",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Tracking mode", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Time-of-day clustering", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (clusteringEnabled) "On — trends shown per time slot" else "Off — daily averages shown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = clusteringEnabled,
                onCheckedChange = viewModel::onClusteringEnabledChanged,
                modifier = Modifier.padding(start = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Weighing frequency", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = weighingFrequency == WeighingFrequency.Daily,
                onClick = { viewModel.onWeighingFrequencyChanged(WeighingFrequency.Daily) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text("Daily") }
            SegmentedButton(
                selected = weighingFrequency == WeighingFrequency.Weekly,
                onClick = { viewModel.onWeighingFrequencyChanged(WeighingFrequency.Weekly) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text("Weekly") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Reminder", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Reminders", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (notificationsEnabled) "On" else "Off",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = viewModel::onNotificationsEnabledChanged,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
        if (notificationsEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            if (weighingFrequency == WeighingFrequency.Weekly) {
                Text(
                    "Day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DAY_LABELS.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = notificationDayOfWeek == index + 1,
                            onClick = { viewModel.onNotificationDayOfWeekChanged(index + 1) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = DAY_LABELS.size),
                        ) { Text(label) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    "Notification time is %02d:%02d".format(notificationHour, notificationMinute),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    "Tap to adjust",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = notificationHour,
                initialMinute = notificationMinute,
                is24Hour = true,
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onNotificationTimeChanged(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = { TimePicker(state = timePickerState) },
            )
        }

        if (showTargetPicker) {
            var pendingTargetKg by remember { mutableStateOf(effectiveTargetKg) }
            AlertDialog(
                onDismissRequest = { showTargetPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onGoalTargetChanged(pendingTargetKg)
                        showTargetPicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTargetPicker = false }) { Text("Cancel") }
                },
                text = {
                    IntWheelPicker(
                        initialValue = weightUnit.fromKg(effectiveTargetKg).toInt(),
                        range = weightUnit.displayRange,
                        label = { "$it ${weightUnit.label}" },
                        onValueChanged = { pendingTargetKg = weightUnit.toKg(it.toDouble()) },
                    )
                },
            )
        }
    }
}

