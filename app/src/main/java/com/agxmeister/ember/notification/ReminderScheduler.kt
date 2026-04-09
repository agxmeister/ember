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

    fun scheduleForTime(dayStartHour: Int, dayStartMinute: Int) {
        val notifyTotalMinutes = (dayStartHour * 60 + dayStartMinute + 15) % (24 * 60)
        val notifyHour = notifyTotalMinutes / 60
        val notifyMinute = notifyTotalMinutes % 60

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notifyHour)
            set(Calendar.MINUTE, notifyMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis
        val label = "%02d:%02d".format(notifyHour, notifyMinute)
        cancel(MORNING_REMINDER_LABEL)
        schedule(label, delayMs)
    }

    fun cancel(clusterLabel: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workTagFor(clusterLabel))
    }

    private fun workTagFor(clusterLabel: String) = "reminder_$clusterLabel"

    companion object {
        const val MORNING_REMINDER_LABEL = "morning"
    }
}
