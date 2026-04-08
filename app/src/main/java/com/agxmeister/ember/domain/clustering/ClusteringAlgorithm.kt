package com.agxmeister.ember.domain.clustering

import com.agxmeister.ember.domain.model.Cluster
import com.agxmeister.ember.domain.model.Measurement
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

private const val CLUSTER_WINDOW_MINUTES = 90

object ClusteringAlgorithm {

    fun cluster(measurements: List<Measurement>): List<Cluster> {
        if (measurements.isEmpty()) return emptyList()

        val sorted = measurements.sortedBy { it.minuteOfDay() }

        val groups = mutableListOf<MutableList<Measurement>>()
        var current = mutableListOf(sorted.first())

        for (measurement in sorted.drop(1)) {
            val gap = measurement.minuteOfDay() - current.last().minuteOfDay()
            if (gap > CLUSTER_WINDOW_MINUTES) {
                groups.add(current)
                current = mutableListOf(measurement)
            } else {
                current.add(measurement)
            }
        }
        groups.add(current)

        return groups.map { group ->
            val median = group.map { it.minuteOfDay() }.average().toInt()
            Cluster(
                label = formatMinuteOfDay(median),
                medianMinuteOfDay = median,
                measurements = group.sortedBy { it.timestamp },
            )
        }
    }

    fun nearestCluster(clusters: List<Cluster>, minuteOfDay: Int): Cluster? {
        return clusters.minByOrNull { cluster ->
            val diff = abs(cluster.medianMinuteOfDay - minuteOfDay)
            minOf(diff, 1440 - diff)
        }
    }

    private fun Measurement.minuteOfDay(): Int {
        val time = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).time
        return time.hour * 60 + time.minute
    }

    private fun formatMinuteOfDay(minutes: Int): String {
        val hour = minutes / 60
        val min = minutes % 60
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "~%d:%02d %s".format(displayHour, min, amPm)
    }
}
