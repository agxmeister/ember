package com.agxmeister.ember.domain.clustering

import com.agxmeister.ember.domain.model.DayCluster
import com.agxmeister.ember.domain.model.Measurement
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val OFFSET_SPAN_DAYS = 28
private const val MIN_CLUSTER_SIZE = 14

class MeasurementNormalizer(
    val homeCluster: DayCluster?,
    private val offsets: Map<DayCluster, Double>,
    private val clusterOf: ((Measurement) -> DayCluster)?,
) {
    fun normalize(m: Measurement): Double =
        m.weightKg + (offsets[clusterOf?.invoke(m)] ?: 0.0)

    fun selectClose(ms: List<Measurement>): Measurement {
        val sorted = ms.sortedBy { it.timestamp }
        return if (homeCluster != null && clusterOf != null)
            sorted.find { clusterOf(it) == homeCluster } ?: sorted.last()
        else
            sorted.last()
    }

    companion object {
        fun build(
            measurements: List<Measurement>,
            dayStartHour: Int,
            clusteringEnabled: Boolean,
        ): MeasurementNormalizer {
            if (!clusteringEnabled || measurements.isEmpty()) {
                return MeasurementNormalizer(null, emptyMap(), null)
            }

            val tz = TimeZone.currentSystemDefault()
            val clusterOf: (Measurement) -> DayCluster = { m ->
                val t = m.timestamp.toLocalDateTime(tz).time
                ClusteringAlgorithm.assign(t.hour * 60 + t.minute, dayStartHour)
            }

            val byCluster = measurements.groupBy { clusterOf(it) }
            val homeCluster = byCluster.maxByOrNull { it.value.size }!!.key

            val dates = measurements.map { it.timestamp.toLocalDateTime(tz).date }
            val spanDays = dates.max().toEpochDays() - dates.min().toEpochDays()

            val offsets: Map<DayCluster, Double> = if (spanDays >= OFFSET_SPAN_DAYS) {
                val qualifiedAvg = byCluster
                    .filter { (_, ms) -> ms.size >= MIN_CLUSTER_SIZE }
                    .mapValues { (_, ms) -> ms.map { it.weightKg }.average() }
                val homeAvg = qualifiedAvg[homeCluster]!!
                DayCluster.entries.associateWith { cluster ->
                    homeAvg - (qualifiedAvg[cluster] ?: homeAvg)
                }
            } else {
                emptyMap()
            }

            return MeasurementNormalizer(homeCluster, offsets, clusterOf)
        }
    }
}
