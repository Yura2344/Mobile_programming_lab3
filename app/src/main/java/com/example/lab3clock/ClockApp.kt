package com.example.lab3clock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.lab3clock.TimerService.Companion.CHANNEL_ID

class ClockApp: Application() {
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Clock app notification channel", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

    }
}