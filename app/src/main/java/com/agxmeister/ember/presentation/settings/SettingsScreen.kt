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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
import com.agxmeister.ember.domain.model.Language
import com.agxmeister.ember.domain.model.WeighingFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToOnboarding: (seedMeasures: Boolean) -> Unit = {},
    onNavigateToAdditional: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val helpIconsVisible by viewModel.helpIconsVisible.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val initialWeightKg by viewModel.initialWeightKg.collectAsStateWithLifecycle()
    val goalTargetKg by viewModel.goalTargetKg.collectAsStateWithLifecycle()
    val goalStartDate by viewModel.effectiveGoalStartDate.collectAsStateWithLifecycle()
    val notificationHour by viewModel.notificationHour.collectAsStateWithLifecycle()
    val notificationMinute by viewModel.notificationMinute.collectAsStateWithLifecycle()
    val weighingFrequency by viewModel.weighingFrequency.collectAsStateWithLifecycle()
    val notificationDayOfWeek by viewModel.notificationDayOfWeek.collectAsStateWithLifecycle()
    val accentCloseness by viewModel.accentCloseness.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showGoalSheet by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val dayLabels = listOf(
        appString(R.string.day_mon),
        appString(R.string.day_tue),
        appString(R.string.day_wed),
        appString(R.string.day_thu),
        appString(R.string.day_fri),
        appString(R.string.day_sat),
        appString(R.string.day_sun),
    )

    SettingsAccentTheme(accentCloseness) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            appString(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(appString(R.string.settings_display))
        SettingsToggleRow(
            title = appString(R.string.settings_help_icons),
            subtitle = if (helpIconsVisible) appString(R.string.label_on) else appString(R.string.label_off),
            checked = helpIconsVisible,
            onCheckedChange = viewModel::onHelpIconsVisibleChanged,
        )
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_language))
        TappableSetting(
            value = language.nativeName,
            onClick = { showLanguageDialog = true },
        )
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_reminder))
        SettingsToggleRow(
            title = appString(R.string.settings_reminders),
            subtitle = if (notificationsEnabled) appString(R.string.label_on) else appString(R.string.label_off),
            checked = notificationsEnabled,
            onCheckedChange = viewModel::onNotificationsEnabledChanged,
        )
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
            TappableSetting(
                value = appString(R.string.settings_notification_time, notificationHour, notificationMinute),
                onClick = { showTimePicker = true },
            )
        }
        SettingsDivider()

        SettingsSectionHeader(appString(R.string.settings_goal))
        val effectiveTargetKg = if (goalTargetKg > 0) goalTargetKg else initialWeightKg
        val goalStartLabel = goalStartDate.ifEmpty { null }?.let {
            runCatching {
                LocalDate.parse(it).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            }.getOrNull()
        } ?: goalStartDate
        TappableSetting(
            value = appString(
                R.string.settings_goal_summary,
                goalStartLabel,
                weightUnit.fromKg(initialWeightKg).toInt(),
                weightUnit.label,
                weightUnit.fromKg(effectiveTargetKg).toInt(),
                weightUnit.label,
            ),
            onClick = { showGoalSheet = true },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            appString(R.string.settings_start_over),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .clickable { showResetDialog = true }
                .padding(vertical = 4.dp),
        )
        SettingsDivider()

        SettingsNavLabel(
            text = appString(R.string.settings_go_to_additional),
            onClick = onNavigateToAdditional,
        )

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(appString(R.string.settings_language)) },
                text = {
                    Column {
                        Language.entries.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onLanguageChanged(lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = language == lang,
                                    onClick = {
                                        viewModel.onLanguageChanged(lang)
                                        showLanguageDialog = false
                                    },
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lang.nativeName, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(appString(R.string.label_cancel))
                    }
                },
            )
        }

        if (showResetDialog) {
            StartOverConfirmDialog(
                onConfirm = {
                    showResetDialog = false
                    viewModel.onResetData()
                    onNavigateToOnboarding(false)
                },
                onDismiss = { showResetDialog = false },
            )
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
            val savedDate = goalStartDate.ifEmpty { LocalDate.now().toString() }
            val initialDateMillis = remember(savedDate) {
                runCatching {
                    LocalDate.parse(savedDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                }.getOrElse { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
            }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
            var showDatePicker by remember { mutableStateOf(false) }
            val borderColor = MaterialTheme.colorScheme.outline
            val initialInUnit = initialText.toIntOrNull()
            val targetInUnit = targetText.toIntOrNull()
            val isValid = initialInUnit?.let { it in weightUnit.displayRange } == true &&
                targetInUnit?.let { it in weightUnit.displayRange } == true
            val selectedDateLabel = datePickerState.selectedDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            } ?: savedDate
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
                        Text(
                            appString(R.string.settings_initial_weight),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Text(
                            appString(R.string.settings_target_weight),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Text(
                            appString(R.string.settings_goal_start_date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                    .width(80.dp)
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
                                    .width(80.dp)
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showDatePicker = true },
                        ) {
                            Text(
                                selectedDateLabel,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                appString(R.string.label_tap_to_adjust),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            val isoDate = datePickerState.selectedDateMillis?.let {
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                            } ?: savedDate
                            viewModel.onGoalChanged(
                                weightUnit.toKg(initialInUnit!!.toDouble()),
                                weightUnit.toKg(targetInUnit!!.toDouble()),
                                isoDate,
                            )
                            showGoalSheet = false
                        },
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(appString(R.string.label_save)) }
                }
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text(appString(R.string.label_ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text(appString(R.string.label_cancel)) }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

    }
    }
}
