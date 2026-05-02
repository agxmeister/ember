package com.agxmeister.ember.presentation.common

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val IntWheelPickerHeight: Dp = 56.dp * 3

@Composable
fun IntWheelPicker(
    initialValue: Int,
    range: IntRange,
    label: (Int) -> String,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dividerStartPadding: Dp = 8.dp,
    dividerEndPadding: Dp = 8.dp,
) {
    val itemHeight: Dp = 56.dp
    val items = range.toList()
    // Pad with one null sentinel at each end so every real value can occupy the center slot.
    val paddedSize = items.size + 2
    val initialScrollIndex = (initialValue - range.first)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex.coerceIn(0, paddedSize - 1))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }

    LaunchedEffect(selectedIndex) {
        val realIndex = selectedIndex - 1
        val value = items.getOrNull(realIndex) ?: return@LaunchedEffect
        onValueChanged(value)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(paddedSize) { paddedIdx ->
                val realIndex = paddedIdx - 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    val value = items.getOrNull(realIndex)
                    if (value != null) {
                        Text(
                            text = label(value),
                            style = if (paddedIdx == selectedIndex) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            color = if (paddedIdx == selectedIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            },
                        )
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = itemHeight).padding(start = dividerStartPadding, end = dividerEndPadding))
        HorizontalDivider(modifier = Modifier.padding(bottom = itemHeight).padding(start = dividerStartPadding, end = dividerEndPadding))
    }
}
