package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.closenessColor

/** A back arrow followed by a screen [title]; [titleModifier] hooks the hidden dev gesture. */
@Composable
internal fun SettingsTopBar(
    title: String,
    onBack: () -> Unit,
    titleModifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = appString(R.string.label_back),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = titleModifier)
    }
}

/** Wraps settings content in the progress-aware accent color scheme. */
@Composable
internal fun SettingsAccentTheme(accentCloseness: Float, content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val accentColor = closenessColor(accentCloseness, darkTheme)
    val accentDim = closenessColor(accentCloseness, darkTheme, saturation = 0.60f, lightness = 0.15f)
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = accentColor,
            onPrimary = Color(0xFF0A0A0A),
            secondaryContainer = accentDim,
            onSecondaryContainer = accentColor,
        ),
        content = content,
    )
}

/** Tappable row that navigates to another settings screen. */
@Composable
internal fun SettingsNavLabel(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Vertical spacing + rule separating two settings sections. */
@Composable
internal fun SettingsDivider() {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider()
    Spacer(Modifier.height(24.dp))
}

/** Section title followed by the standard gap before its content. */
@Composable
internal fun SettingsSectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

/** A tappable row showing a [value] with a "tap to adjust" affordance. */
@Composable
internal fun TappableSetting(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
    ) {
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Text(
            appString(R.string.label_tap_to_adjust),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Confirmation dialog for the destructive "start over" actions. */
@Composable
internal fun StartOverConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(appString(R.string.settings_start_over_confirm_title)) },
        text = { Text(appString(R.string.settings_start_over_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(appString(R.string.label_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(appString(R.string.label_cancel)) }
        },
    )
}

/** A small label above a tappable value, for settings that share a section header. */
@Composable
internal fun LabeledTappableSetting(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
    ) {
        Text("$label: $value", style = MaterialTheme.typography.bodyLarge)
        Text(
            appString(R.string.label_tap_to_adjust),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Dialog for editing a single integer setting constrained to [validRange]. */
@Composable
internal fun IntSettingDialog(
    title: String,
    initialValue: Int,
    validRange: IntRange,
    suffix: String? = null,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    val intValue = text.toIntOrNull()
    val isValid = intValue != null && intValue in validRange
    val borderColor = MaterialTheme.colorScheme.outline
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = text,
                    onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) text = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(64.dp)
                        .drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        },
                )
                if (suffix != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(suffix, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { intValue?.let(onConfirm) }, enabled = isValid) {
                Text(appString(R.string.label_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(appString(R.string.label_cancel)) }
        },
    )
}

/** A labeled row with a subtitle and a trailing toggle switch. */
@Composable
internal fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
