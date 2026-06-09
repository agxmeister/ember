package com.agxmeister.ember.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.agxmeister.ember.domain.model.WeighingFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(clusterLabel: String, initialDelayMillis: Long, repeatIntervalDays: Long = 1) {
        val tag = workTagFor(clusterLabel)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(repeatIntervalDays, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .setInputData(workDataOf(ReminderWorker.KEY_CLUSTER_LABEL to clusterLabel))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    /** Schedules the reminder according to the user's weighing frequency. */
    fun scheduleForFrequency(frequency: WeighingFrequency, dayOfWeek: Int, hour: Int, minute: Int) {
        if (frequency == WeighingFrequency.Weekly) scheduleWeeklyForTime(dayOfWeek, hour, minute)
        else scheduleForTime(hour, minute)
    }

    fun scheduleForTime(notificationHour: Int, notificationMinute: Int) {
        val target = nextOccurrence(notificationHour, notificationMinute, dayOfWeek = null)
        cancelScheduled()
        schedule(SCHEDULED_LABEL, target, repeatIntervalDays = 1)
    }

    // dayOfWeek: ISO 8601 — Monday=1 … Sunday=7
    fun scheduleWeeklyForTime(dayOfWeek: Int, notificationHour: Int, notificationMinute: Int) {
        val target = nextOccurrence(notificationHour, notificationMinute, dayOfWeek)
        cancelScheduled()
        schedule(SCHEDULED_LABEL, target, repeatIntervalDays = 7)
    }

    /**
     * Delay in milliseconds until the next [hour]:[minute]. When [dayOfWeek] (ISO 8601,
     * Monday=1…Sunday=7) is given, the target is the next occurrence on that weekday.
     */
    private fun nextOccurrence(hour: Int, minute: Int, dayOfWeek: Int?): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            if (dayOfWeek != null) set(Calendar.DAY_OF_WEEK, dayOfWeek % 7 + 1) // ISO → Calendar (Sun=1…Sat=7)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(if (dayOfWeek != null) Calendar.WEEK_OF_YEAR else Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    fun cancelScheduled() {
        cancel(MORNING_REMINDER_LABEL)
        cancel(SCHEDULED_LABEL)
    }

    fun cancel(clusterLabel: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workTagFor(clusterLabel))
    }

    private fun workTagFor(clusterLabel: String) = "reminder_$clusterLabel"

    companion object {
        const val MORNING_REMINDER_LABEL = "morning"
        private const val SCHEDULED_LABEL = "scheduled"
    }
}
