package com.agxmeister.ember.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun cancel(clusterLabel: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workTagFor(clusterLabel))
    }

    private fun workTagFor(clusterLabel: String) = "reminder_$clusterLabel"
}
