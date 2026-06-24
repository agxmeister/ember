package com.agxmeister.ember.data.analytics

import com.aptabase.Aptabase
import com.agxmeister.ember.domain.analytics.AnalyticsEvent
import com.agxmeister.ember.domain.analytics.AnalyticsTracker
import com.agxmeister.ember.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AptabaseAnalyticsTracker @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : AnalyticsTracker {
    override suspend fun track(event: AnalyticsEvent) {
        if (!preferencesRepository.analyticsEnabled.first()) return
        Aptabase.instance.trackEvent(event.name, event.properties)
    }
}
