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
import com.agxmeister.ember.domain.model.WeightUnit

private val ITEM_HEIGHT = 56.dp
val WeightPickerHeight = ITEM_HEIGHT * 3
private const val MIN_KG = 30.0
private const val MAX_KG = 300.0

@Composable
fun WeightWheelPicker(
    initialWeightKg: Double,
    unit: WeightUnit,
    onWeightKgChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minValue = unit.fromKg(MIN_KG)
    val step = unit.step
    val count = ((unit.fromKg(MAX_KG) - minValue) / step).toInt() + 1

    fun toIndex(kg: Double): Int = ((unit.fromKg(kg) - minValue) / step).toInt().coerceIn(0, count - 1)
    fun fromIndex(index: Int): Double = unit.toKg(minValue + index * step)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (toIndex(initialWeightKg) - 1).coerceAtLeast(0))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + 1
        }
    }

    LaunchedEffect(selectedIndex) {
        onWeightKgChanged(fromIndex(selectedIndex))
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
            items(count) { index ->
                val displayValue = minValue + index * step
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "%.1f ${unit.label}".format(displayValue),
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
