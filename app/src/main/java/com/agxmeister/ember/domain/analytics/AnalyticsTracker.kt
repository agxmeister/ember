package com.agxmeister.ember.domain.analytics

interface AnalyticsTracker {
    suspend fun track(event: AnalyticsEvent)
}
