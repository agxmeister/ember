package com.agxmeister.ember.presentation.equalizer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agxmeister.ember.presentation.common.InfoDialog
import com.agxmeister.ember.presentation.theme.InfoIconSize

@Composable
internal fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    info: String? = null,
    content: @Composable () -> Unit,
) {
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo && info != null) {
        InfoDialog(title = label, text = info, onDismiss = { showInfo = false })
    }
    StatCardSurface(modifier = modifier) {
        CardLabelRow(
            label = label,
            modifier = Modifier.height(InfoIconSize),
            onInfo = if (info != null) ({ showInfo = true }) else null,
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
