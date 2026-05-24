package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.common.IntWheelPicker
import com.agxmeister.ember.presentation.home.WeightWheelPicker
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun EqualizerEditDrawer(
    editState: EqualizerEditState,
    accentColor: Color,
    onSave: (id: Long, weightKg: Double, timestamp: Instant) -> Unit,
    onDelete: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val initialTime = editState.existingMeasurement?.timestamp?.let {
        it.toLocalDateTime(tz).time
    } ?: LocalTime(editState.defaultHour, editState.defaultMinute)
    var selectedWeightKg by remember { mutableStateOf(editState.defaultWeightKg) }
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }
    var selectedDow by remember { mutableStateOf(editState.date.dayOfWeek.value) }
    var timeEditing by remember { mutableStateOf(false) }

    val displayDate = if (editState.isWeekly && editState.weekStart != null) {
        editState.weekStart.plus(DatePeriod(days = selectedDow - 1))
    } else {
        editState.date
    }
    val dow = displayDate.dayOfWeek.name.take(3)
    val mon = displayDate.month.name.take(3)
    val day = displayDate.dayOfMonth.toString().padStart(2, '0')
    val pickerHeight = 168.dp

    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$dow  $mon $day  ${displayDate.year}",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    color = onSurface.copy(alpha = 0.80f),
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
            if (editState.existingMeasurement != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = appString(R.string.cd_delete),
                        tint = onSurface.copy(alpha = 0.50f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        val dowNames = listOf(
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
                primary = onSurface,
                onPrimary = MaterialTheme.colorScheme.surface,
                onSurface = onSurface,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WeightWheelPicker(
                    initialWeightKg = editState.defaultWeightKg,
                    unit = editState.weightUnit,
                    onWeightKgChanged = { selectedWeightKg = it },
                    modifier = Modifier.weight(1f),
                )

                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(pickerHeight)
                        .background(onSurface.copy(alpha = 0.12f)),
                )

                if (editState.isWeekly && !timeEditing) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(pickerHeight)
                            .clickable { timeEditing = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = appString(R.string.trends_day_and_time),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.40f),
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${dowNames[selectedDow - 1]}  %02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = appString(R.string.trends_tap_to_change),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.35f),
                            ),
                        )
                    }
                } else if (editState.isWeekly) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IntWheelPicker(
                            initialValue = editState.date.dayOfWeek.value,
                            range = 1..7,
                            label = { dowNames[it - 1] },
                            onValueChanged = { selectedDow = it },
                            modifier = Modifier.weight(1f),
                            dividerEndPadding = 0.dp,
                        )
                        Text(
                            text = " ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface.copy(alpha = 0.60f),
                        )
                        IntWheelPicker(
                            initialValue = initialTime.hour,
                            range = 0..23,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedHour = it },
                            modifier = Modifier.weight(1f),
                            dividerEndPadding = 0.dp,
                            dividerStartPadding = 0.dp,
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface.copy(alpha = 0.60f),
                        )
                        IntWheelPicker(
                            initialValue = initialTime.minute,
                            range = 0..59,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedMinute = it },
                            modifier = Modifier.weight(1f),
                            dividerStartPadding = 0.dp,
                        )
                    }
                } else if (!timeEditing) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(pickerHeight)
                            .clickable { timeEditing = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = appString(R.string.trends_taken_at),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.40f),
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "%02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = appString(R.string.trends_tap_to_change),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = onSurface.copy(alpha = 0.35f),
                            ),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IntWheelPicker(
                            initialValue = initialTime.hour,
                            range = 0..23,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedHour = it },
                            modifier = Modifier.weight(1f),
                            dividerEndPadding = 0.dp,
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            color = onSurface.copy(alpha = 0.60f),
                        )
                        IntWheelPicker(
                            initialValue = initialTime.minute,
                            range = 0..59,
                            label = { "%02d".format(it) },
                            onValueChanged = { selectedMinute = it },
                            modifier = Modifier.weight(1f),
                            dividerStartPadding = 0.dp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val dateTime = LocalDateTime(displayDate, LocalTime(selectedHour, selectedMinute))
                onSave(
                    editState.existingMeasurement?.id ?: 0L,
                    selectedWeightKg,
                    dateTime.toInstant(tz),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
        ) {
            Text(
                text = appString(R.string.label_save),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A0A0A),
                ),
            )
        }
    }
}
