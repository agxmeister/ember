package com.agxmeister.ember.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agxmeister.ember.domain.model.Measurement
import com.agxmeister.ember.domain.model.WeightUnit
import com.agxmeister.ember.presentation.home.WeightWheelPicker
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingReplace by viewModel.pendingReplace.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.pendingDelete.collectAsStateWithLifecycle()
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    var isEditing by remember { mutableStateOf(false) }
    var editingMeasurement by remember { mutableStateOf<Measurement?>(null) }

    LaunchedEffect(uiState.selectedDate) {
        if (uiState.selectedDate == null) {
            isEditing = false
            editingMeasurement = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveEvents.collect {
            isEditing = false
            editingMeasurement = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }
            val monthLabel = remember(uiState.displayYear, uiState.displayMonth) {
                LocalDate(uiState.displayYear, uiState.displayMonth, 1).month.name
                    .lowercase()
                    .replaceFirstChar { it.uppercaseChar() }
            }
            Text(
                text = "$monthLabel ${uiState.displayYear}",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = viewModel::nextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        ) {
            listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val firstDay = LocalDate(uiState.displayYear, uiState.displayMonth, 1)
        val daysInMonth = firstDay.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).dayOfMonth
        val startOffset = firstDay.dayOfWeek.value - 1
        val rows = (startOffset + daysInMonth + 6) / 7

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNumber = row * 7 + col - startOffset + 1
                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val date = LocalDate(uiState.displayYear, uiState.displayMonth, dayNumber)
                        DayCell(
                            dayNumber = dayNumber,
                            hasRecord = date in uiState.measurementDates,
                            isToday = date == today,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectDate(date) },
                        )
                    }
                }
            }
        }
    }

    uiState.selectedDate?.let { date ->
        ModalBottomSheet(onDismissRequest = viewModel::dismissSheet) {
            if (isEditing) {
                MeasurementEditForm(
                    date = date,
                    measurement = editingMeasurement,
                    weightUnit = uiState.weightUnit,
                    defaultWeightKg = editingMeasurement?.weightKg
                        ?: uiState.selectedDateMeasurements.lastOrNull()?.weightKg
                        ?: uiState.defaultWeightKg,
                    dayStartHour = uiState.dayStartHour,
                    dayStartMinute = uiState.dayStartMinute,
                    onSave = { id, weightKg, timestamp ->
                        viewModel.requestSave(id, weightKg, timestamp)
                    },
                    onCancel = {
                        isEditing = false
                        editingMeasurement = null
                    },
                )
            } else {
                MeasurementListContent(
                    date = date,
                    measurements = uiState.selectedDateMeasurements,
                    weightUnit = uiState.weightUnit,
                    onEdit = { m ->
                        editingMeasurement = m
                        isEditing = true
                    },
                    onDelete = { m -> viewModel.requestDelete(m) },
                    onAdd = {
                        editingMeasurement = null
                        isEditing = true
                    },
                )
            }
        }
    }

    pendingDelete?.let { delete ->
        val shortDate = shortDateLabel(delete.date)
        val deleteMessage = if (delete.clusterName != null) {
            "Delete measurement for ${delete.clusterName} on $shortDate?"
        } else {
            "Delete measurement for $shortDate?"
        }
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            text = { Text(deleteMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Cancel") }
            },
        )
    }

    pendingReplace?.let { replace ->
        val shortDate = shortDateLabel(replace.date)
        val replaceMessage = if (replace.clusterName != null) {
            "A measurement for ${replace.clusterName} on $shortDate already exists. Replace it?"
        } else {
            "A measurement for $shortDate already exists. Replace it?"
        }
        AlertDialog(
            onDismissRequest = viewModel::cancelReplace,
            text = { Text(replaceMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmReplace) { Text("Replace") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelReplace) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DayCell(
    dayNumber: Int,
    hasRecord: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                when {
                    hasRecord -> Modifier.background(primary)
                    isToday -> Modifier.border(1.dp, primary, CircleShape)
                    else -> Modifier
                }
            ),
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = when {
                hasRecord -> onPrimary
                isToday -> primary
                else -> onSurface
            },
        )
    }
}

@Composable
private fun MeasurementListContent(
    date: LocalDate,
    measurements: List<Measurement>,
    weightUnit: WeightUnit,
    onEdit: (Measurement) -> Unit,
    onDelete: (Measurement) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        if (measurements.isEmpty()) {
            Text(
                text = dateLabel(date),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Text(
                text = "No measurements",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        } else {
            measurements.forEach { m ->
                MeasurementRow(
                    date = date,
                    measurement = m,
                    weightUnit = weightUnit,
                    onEdit = { onEdit(m) },
                    onDelete = { onDelete(m) },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add measurement")
        }
    }
}

@Composable
private fun MeasurementRow(
    date: LocalDate,
    measurement: Measurement,
    weightUnit: WeightUnit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val time = measurement.timestamp.toLocalDateTime(tz).time
    val weight = weightUnit.fromKg(measurement.weightKg)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${dateLabel(date)}  %02d:%02d".format(time.hour, time.minute),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
        Text(
            text = "%.1f ${weightUnit.label}".format(weight),
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementEditForm(
    date: LocalDate,
    measurement: Measurement?,
    weightUnit: WeightUnit,
    defaultWeightKg: Double,
    dayStartHour: Int,
    dayStartMinute: Int,
    onSave: (id: Long, weightKg: Double, timestamp: Instant) -> Unit,
    onCancel: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val initialTime = measurement?.timestamp?.toLocalDateTime(tz)?.time ?: run {
        val totalMinutes = dayStartHour * 60 + dayStartMinute + 15
        LocalTime(totalMinutes / 60 % 24, totalMinutes % 60)
    }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    var selectedWeightKg by remember { mutableStateOf(defaultWeightKg) }
    var timeEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = dateLabel(date),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        WeightWheelPicker(
            initialWeightKg = defaultWeightKg,
            unit = weightUnit,
            onWeightKgChanged = { selectedWeightKg = it },
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            TimeInput(
                state = timePickerState,
                modifier = Modifier.scale(0.8f),
            )
            if (!timeEditing) {
                Column(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable { timeEditing = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "%02d:%02d".format(timePickerState.hour, timePickerState.minute),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "tap to change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val dateTime = LocalDateTime(date, LocalTime(timePickerState.hour, timePickerState.minute))
                    onSave(measurement?.id ?: 0L, selectedWeightKg, dateTime.toInstant(tz))
                },
            ) { Text("Save") }
        }
    }
}

private fun dateLabel(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }
    return "${date.dayOfMonth} $month ${date.year}"
}

private fun shortDateLabel(date: LocalDate): String {
    val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }
    return "$month ${date.dayOfMonth}"
}
