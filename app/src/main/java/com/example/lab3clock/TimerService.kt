package com.example.lab3clock

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val DElAY_MS: Long = 50

        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        const val ACTION_PAUSE = "pause"
    }

    private var binder: TimerBinder = TimerBinder()

    private var milliseconds: Long = 0
    private var startTime: Long = 0
    private var lastRound: Long = 0

    private var isTimerActive: Boolean = false
    private var isTimerPaused: Boolean = false

    var handler: Handler = Handler(Looper.getMainLooper())
        private set

    var roundsList: ArrayList<String> = arrayListOf()
        private set

    private lateinit var scheduledExecutorService: ScheduledExecutorService
    private lateinit var notificationManager: NotificationManager
    private lateinit var timerHandle: ScheduledFuture<*>
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var observers: ArrayList<TimerActivity> = arrayListOf()

    private var seconds: Long = 0
    private var timeCounter: Runnable = Runnable {
        milliseconds = System.currentTimeMillis() - startTime
        if (milliseconds / 1000 != seconds) {
            seconds = milliseconds / 1000
            notificationManager.notify(NOTIFICATION_ID, notification())
        }

        handler.post {
            updateObservers()
        }
    }

    private fun getTimeString(milliseconds: Long, includeMillis: Boolean = true): String {
        return String.format(
            "%02d:%02d:%02d",
            milliseconds / 3_600_000,
            (milliseconds / 60_000) % 60,
            (milliseconds / 1_000) % 60
        ) + if(includeMillis) String.format(".%02d", (milliseconds % 1000) / 10) else ""
    }

    private fun notification(): Notification {
        val startIntent = Intent(this, TimerService::class.java).also {
            it.action = ACTION_START
        }
        val stopIntent = Intent(this, TimerService::class.java).also {
            it.action = ACTION_STOP
        }
        val pauseIntent = Intent(this, TimerService::class.java).also {
            it.action = ACTION_PAUSE
        }

        val pauseAction = PendingIntent.getService(
            this, 0, pauseIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val startAction = PendingIntent.getService(
            this, 0, startIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return notificationBuilder
            .setContentText(getTimeString(milliseconds, false))
            .clearActions()
            .addAction(0, getString(R.string.start_button_text), startAction)
            .addAction(0, getString(R.string.stop_button_text), stopAction)
            .addAction(
                0,
                getString(if (!isTimerPaused) R.string.pause_button_text else R.string.continue_button_text),
                pauseAction
            )
            .build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val activityIntent = Intent(this, TimerActivity::class.java)
        val activityAction = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getTimeString(milliseconds))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(activityAction)

        startForeground(NOTIFICATION_ID, notification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
            ACTION_PAUSE -> {
                Log.i("pauseAction", "pauseAction")
                pauseTimer()
            }
        }

        return START_STICKY
    }

    inner class TimerBinder : Binder() {
        val service: TimerService
            get() = this@TimerService
    }

    fun startTimer() {
        isTimerActive = true
        isTimerPaused = false
        lastRound = 0
        startTime = System.currentTimeMillis()
        roundsList.clear()
        Log.i("start", "start")
        scheduledExecutorService = Executors.newScheduledThreadPool(1)
        notificationManager.notify(NOTIFICATION_ID, notification())
        timerHandle =
            scheduledExecutorService.scheduleWithFixedDelay(
                timeCounter,
                0,
                DElAY_MS,
                TimeUnit.MILLISECONDS
            )
    }

    fun stopTimer() {
        isTimerPaused = false
        isTimerActive = false
        milliseconds = 0
        scheduledExecutorService.shutdown()
        notificationManager.notify(NOTIFICATION_ID, notification())
    }

    fun pauseTimer() {
        if (isTimerActive) {
            isTimerPaused = !isTimerPaused

            if (!isTimerPaused) {
                startTime += System.currentTimeMillis() - (startTime + milliseconds)

                scheduledExecutorService = Executors.newScheduledThreadPool(1)
                timerHandle = scheduledExecutorService.scheduleWithFixedDelay(
                    timeCounter,
                    0,
                    DElAY_MS,
                    TimeUnit.MILLISECONDS
                )
                notificationManager.notify(NOTIFICATION_ID, notification())
            } else {
                scheduledExecutorService.shutdown()
                notificationManager.notify(NOTIFICATION_ID, notification())
            }
        }
    }

    fun addRound() {
        if (isTimerActive) {
            val timeDifference: Long = milliseconds - lastRound
            roundsList.add("+${getTimeString(timeDifference)}")
            lastRound = milliseconds
        }
    }

    fun addObserver(observer: TimerActivity) {
        if (!observers.contains(observer)) {
            observers.add(observer)
            observer.updatePauseButton(isTimerPaused)
            observer.updateTimer(getTimeString(milliseconds))
        }
    }

    fun removeObserver(observer: TimerActivity) {
        observers.remove(observer)
    }

    private fun updateObservers() {
        for (observer in observers) {
            observer.updatePauseButton(isTimerPaused)
            observer.updateTimer(getTimeString(milliseconds))
        }
    }
}