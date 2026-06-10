package com.agxmeister.ember.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString

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
