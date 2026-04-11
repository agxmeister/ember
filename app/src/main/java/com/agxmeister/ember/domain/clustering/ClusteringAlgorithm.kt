package com.agxmeister.ember.domain.clustering

import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.DayCluster
import com.agxmeister.ember.domain.model.Measurement
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Day window: [dayStartHour-1, dayStartHour+16), total 17h = 1020 min → 4 slots of 255 min each.
// Night (remaining 7h = 420 min) is split at its midpoint (210 min after window end):
//   closer to window end  → Selene
//   closer to window start → Eos
private const val CLUSTER_MINUTES = 255    // 1020 / 4
private const val WINDOW_MINUTES = 1020   // 17 * 60
private const val NIGHT_MID_OFFSET = 1230 // 1020 + 420 / 2

object ClusteringAlgorithm {

    fun assign(minuteOfDay: Int, dayStartHour: Int): DayCluster {
        val windowStart = ((dayStartHour - 1) * 60 + 1440) % 1440
        val offset = (minuteOfDay - windowStart + 1440) % 1440
        return when {
            offset < CLUSTER_MINUTES -> DayCluster.Eos
            offset < CLUSTER_MINUTES * 2 -> DayCluster.Helios
            offset < CLUSTER_MINUTES * 3 -> DayCluster.Hesperus
            offset < WINDOW_MINUTES -> DayCluster.Selene
            offset < NIGHT_MID_OFFSET -> DayCluster.Selene
            else -> DayCluster.Eos
        }
    }

    fun cluster(measurements: List<Measurement>, dayStartHour: Int): List<Cluster> {
        val grouped = measurements.groupBy { m ->
            val t = m.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).time
            assign(t.hour * 60 + t.minute, dayStartHour)
        }
        return DayCluster.entries.map { dayCluster ->
            Cluster(
                dayCluster = dayCluster,
                measurements = (grouped[dayCluster] ?: emptyList()).sortedBy { it.timestamp },
            )
        }
    }
}
