package com.agxmeister.ember.domain.analytics

import com.agxmeister.ember.domain.model.WeighingFrequency

sealed interface AnalyticsEvent {
    val name: String
    val properties: Map<String, Any>

    data class WeighInLogged(
        val frequency: WeighingFrequency,
        val clusteringEnabled: Boolean,
        val loggingCoverage: Int,
    ) : AnalyticsEvent {
        override val name = "weighin_logged"
        override val properties = mapOf(
            "mode" to frequency.name.lowercase(),
            "tracking_mode" to if (clusteringEnabled) "smart" else "raw",
            "coverage" to loggingCoverage,
        )
    }
}
