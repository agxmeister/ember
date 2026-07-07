package com.agxmeister.ember.presentation.equalizer

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.agxmeister.ember.R
import com.agxmeister.ember.presentation.appString

@Composable
internal fun trendPendingText(pending: TrendPending, @StringRes notEnoughRes: Int): String =
    when (pending) {
        is TrendPending.NotEnoughData -> appString(notEnoughRes, pending.measurementsNeeded)
        is TrendPending.GapTooBig -> appString(R.string.trends_trend_gap_info, pending.measurementsNeeded)
    }

/** Same as [trendPendingText] but without the trailing count, for split explanation|count layouts. */
@Composable
internal fun trendPendingExplanation(pending: TrendPending, @StringRes notEnoughRes: Int): String =
    when (pending) {
        is TrendPending.NotEnoughData -> appString(notEnoughRes)
        is TrendPending.GapTooBig -> appString(R.string.trends_trend_gap_explanation)
    }
