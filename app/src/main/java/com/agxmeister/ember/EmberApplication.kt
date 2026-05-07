package com.agxmeister.ember

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.agxmeister.ember.notification.NOTIFICATION_CHANNEL_ID
import com.agxmeister.ember.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EmberApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
