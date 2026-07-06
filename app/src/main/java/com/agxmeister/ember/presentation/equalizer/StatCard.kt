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
    helpKey: String? = null,
    pendingInfo: String? = null,
    content: @Composable () -> Unit,
) {
    var showInfo by remember { mutableStateOf(false) }
    val effectiveInfo = pendingInfo ?: info
    if (showInfo && effectiveInfo != null) {
        InfoDialog(title = label, text = effectiveInfo, onDismiss = { showInfo = false })
    }
    StatCardSurface(modifier = modifier) {
        CardLabelRow(
            label = label,
            modifier = Modifier.height(InfoIconSize),
            onInfo = if (effectiveInfo != null) ({ showInfo = true }) else null,
            helpKey = helpKey,
            pending = pendingInfo != null,
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
