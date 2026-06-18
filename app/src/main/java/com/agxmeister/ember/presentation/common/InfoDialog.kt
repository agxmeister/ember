package com.agxmeister.ember.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString
import com.agxmeister.ember.presentation.theme.InfoIconSize

/** Whether help/info ("?") affordances are shown; toggled in settings, provided at the app root. */
val LocalHelpIconsVisible = staticCompositionLocalOf { true }

/** Help keys the user has already opened; such icons render dimmed. Provided at the app root. */
val LocalSeenHelpKeys = staticCompositionLocalOf { emptySet<String>() }

/** Records that a help key has been opened so it dims and stays dimmed across sessions. */
val LocalMarkHelpSeen = staticCompositionLocalOf<(String) -> Unit> { {} }

/** Standard informational dialog with a single dismiss ("OK") action. */
@Composable
fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(appString(R.string.label_ok)) }
        },
        title = { Text(title) },
        text = { Text(text) },
    )
}

/**
 * Tappable help/info ("?"/"i") affordance sized consistently across the app. Prominent until the
 * user opens it ([helpKey] is recorded), then dimmed; the seen state persists across sessions.
 */
@Composable
fun InfoIcon(
    onClick: () -> Unit,
    helpKey: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.HelpOutline,
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    seenTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
) {
    if (!LocalHelpIconsVisible.current) return
    val seen = helpKey in LocalSeenHelpKeys.current
    val markSeen = LocalMarkHelpSeen.current
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier.size(InfoIconSize).clickable {
            markSeen(helpKey)
            onClick()
        },
        tint = if (seen) seenTint else tint,
    )
}
