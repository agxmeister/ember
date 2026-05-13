package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import com.agxmeister.ember.presentation.appString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.R
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.domain.model.WeighingFrequency
import com.agxmeister.ember.presentation.theme.closenessColor

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

    val darkTheme = isSystemInDarkTheme()
    val accentCloseness by viewModel.accentCloseness.collectAsStateWithLifecycle()
    val accentColor = closenessColor(accentCloseness, darkTheme)
    val accentDim = Color.hsl(8f + accentCloseness * 112f, saturation = 0.60f, lightness = 0.15f)

    var showTimePicker by remember { mutableStateOf(false) }
    var showGoalSheet by remember { mutableStateOf(false) }

    val dayLabels = listOf(
        appString(R.string.day_mon),
        appString(R.string.day_tue),
        appString(R.string.day_wed),
        appString(R.string.day_thu),
        appString(R.string.day_fri),
        appString(R.string.day_sat),
        appString(R.string.day_sun),
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = accentColor,
            onPrimary = Color(0xFF0A0A0A),
            secondaryContainer = accentDim,
            onSecondaryContainer = accentColor,
        ),
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(appString(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        Text(appString(R.string.settings_unit), style = MaterialTheme.typography.titleMedium)
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

        Text(appString(R.string.settings_goal), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val effectiveTargetKg = if (goalTargetKg > 0) goalTargetKg else initialWeightKg
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showGoalSheet = true }
                .padding(vertical = 8.dp),
        ) {
            Text(
                appString(
                    R.string.settings_goal_summary,
                    weightUnit.fromKg(initialWeightKg).toInt(),
                    weightUnit.label,
                    weightUnit.fromKg(effectiveTargetKg).toInt(),
                    weightUnit.label,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                appString(R.string.label_tap_to_adjust),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(appString(R.string.settings_tracking_mode), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appString(R.string.label_clustering), style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (clusteringEnabled) appString(R.string.label_clustering_on) else appString(R.string.label_clustering_off),
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

        Text(appString(R.string.settings_weighing_frequency), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = weighingFrequency == WeighingFrequency.Daily,
                onClick = { viewModel.onWeighingFrequencyChanged(WeighingFrequency.Daily) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text(appString(R.string.label_daily)) }
            SegmentedButton(
                selected = weighingFrequency == WeighingFrequency.Weekly,
                onClick = { viewModel.onWeighingFrequencyChanged(WeighingFrequency.Weekly) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text(appString(R.string.label_weekly)) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(appString(R.string.settings_reminder), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appString(R.string.settings_reminders), style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (notificationsEnabled) appString(R.string.label_on) else appString(R.string.label_off),
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
                    appString(R.string.label_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    dayLabels.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = notificationDayOfWeek == index + 1,
                            onClick = { viewModel.onNotificationDayOfWeekChanged(index + 1) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = dayLabels.size),
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
                    appString(R.string.settings_notification_time, notificationHour, notificationMinute),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    appString(R.string.label_tap_to_adjust),
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
                    }) { Text(appString(R.string.label_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(appString(R.string.label_cancel)) }
                },
                text = { TimePicker(state = timePickerState) },
            )
        }

        if (showGoalSheet) {
            var initialText by remember { mutableStateOf(weightUnit.fromKg(initialWeightKg).toInt().toString()) }
            var targetText by remember { mutableStateOf(weightUnit.fromKg(effectiveTargetKg).toInt().toString()) }
            val borderColor = MaterialTheme.colorScheme.outline
            val initialInUnit = initialText.toIntOrNull()
            val targetInUnit = targetText.toIntOrNull()
            val isValid = initialInUnit?.let { it in weightUnit.displayRange } == true &&
                targetInUnit?.let { it in weightUnit.displayRange } == true
            ModalBottomSheet(
                onDismissRequest = { showGoalSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Text(appString(R.string.settings_adjust_goal), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                appString(R.string.settings_initial_weight),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            BasicTextField(
                                value = initialText,
                                onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) initialText = it },
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .width(120.dp)
                                    .drawBehind {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 1.dp.toPx(),
                                        )
                                    },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(weightUnit.label, style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                appString(R.string.settings_target_weight),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            BasicTextField(
                                value = targetText,
                                onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) targetText = it },
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .width(120.dp)
                                    .drawBehind {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 1.dp.toPx(),
                                        )
                                    },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(weightUnit.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.onGoalChanged(
                                weightUnit.toKg(initialInUnit!!.toDouble()),
                                weightUnit.toKg(targetInUnit!!.toDouble()),
                            )
                            showGoalSheet = false
                        },
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(appString(R.string.label_save)) }
                }
            }
        }
    }
    }
}
