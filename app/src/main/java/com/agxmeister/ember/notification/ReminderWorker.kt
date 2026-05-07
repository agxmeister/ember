package com.agxmeister.ember.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

const val NOTIFICATION_CHANNEL_ID = "ember_reminders"
private const val NOTIFICATION_ID = 1001

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        val clusterLabel = inputData.getString(KEY_CLUSTER_LABEL) ?: "weigh-in"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(com.agxmeister.ember.R.string.notification_title))
            .setContentText(context.getString(com.agxmeister.ember.R.string.notification_text, clusterLabel))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val KEY_CLUSTER_LABEL = "cluster_label"
    }
}
