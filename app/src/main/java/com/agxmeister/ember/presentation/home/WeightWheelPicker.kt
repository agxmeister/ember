package com.agxmeister.ember.presentation.home

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
import androidx.compose.ui.unit.dp

private const val STEP = 0.1
private const val MIN_WEIGHT = 30.0
private const val MAX_WEIGHT = 300.0
private val ITEM_HEIGHT = 56.dp

private fun weightToIndex(weight: Double): Int =
    ((weight - MIN_WEIGHT) / STEP).toInt().coerceIn(0, itemCount() - 1)

private fun indexToWeight(index: Int): Double =
    MIN_WEIGHT + index * STEP

private fun itemCount(): Int =
    ((MAX_WEIGHT - MIN_WEIGHT) / STEP).toInt() + 1

@Composable
fun WeightWheelPicker(
    initialWeight: Double,
    onWeightChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = weightToIndex(initialWeight))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + 1
        }
    }

    LaunchedEffect(selectedIndex) {
        onWeightChanged(indexToWeight(selectedIndex))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(ITEM_HEIGHT * 3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(itemCount()) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "%.1f kg".format(indexToWeight(index)),
                        style = if (index == selectedIndex) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (index == selectedIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        },
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = ITEM_HEIGHT))
        HorizontalDivider(modifier = Modifier.padding(bottom = ITEM_HEIGHT))
    }
}
