package com.agxmeister.ember.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(clusterLabel: String, initialDelayMillis: Long) {
        val tag = workTagFor(clusterLabel)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .setInputData(workDataOf(ReminderWorker.KEY_CLUSTER_LABEL to clusterLabel))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleForTime(notificationHour: Int, notificationMinute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notificationHour)
            set(Calendar.MINUTE, notificationMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis
        cancelScheduled()
        schedule(SCHEDULED_LABEL, delayMs)
    }

    // dayOfWeek: ISO 8601 — Monday=1 … Sunday=7
    fun scheduleWeeklyForTime(dayOfWeek: Int, notificationHour: Int, notificationMinute: Int) {
        val calendarDay = dayOfWeek % 7 + 1 // converts ISO to Calendar.DAY_OF_WEEK (Sun=1…Sat=7)
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, calendarDay)
            set(Calendar.HOUR_OF_DAY, notificationHour)
            set(Calendar.MINUTE, notificationMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.WEEK_OF_YEAR, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis
        cancelScheduled()
        val tag = workTagFor(SCHEDULED_LABEL)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .setInputData(workDataOf(ReminderWorker.KEY_CLUSTER_LABEL to SCHEDULED_LABEL))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.UPDATE, request)
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
