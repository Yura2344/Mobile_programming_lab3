package com.example.lab3clock

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView

class TimerActivity : AppCompatActivity() {
    companion object {
        const val DEFAULT_TIME_STRING: String = "00:00.00"
    }

    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var timeTextBox: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var pauseButton: Button
    private lateinit var roundButton: Button
    private lateinit var roundsListView: ListView

    private lateinit var clockNavButton: Button
    private lateinit var settingsNavButton: Button

    private var timerService: TimerService? = null

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = (service as TimerService.TimerBinder).service

            adapter = ArrayAdapter(
                this@TimerActivity,
                android.R.layout.simple_list_item_1,
                timerService!!.roundsList
            )

            roundsListView.adapter = adapter

            startButton.setOnClickListener { startTimer() }
            stopButton.setOnClickListener { stopTimer() }
            pauseButton.setOnClickListener { pauseTimer() }
            roundButton.setOnClickListener { addRound() }
            timerService?.addObserver(this@TimerActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("onCreate", "onCreate")
        setContentView(R.layout.activity_timer)

        clockNavButton = findViewById(R.id.clockNavButton)
        settingsNavButton = findViewById(R.id.settingsNavButton)

        clockNavButton.setOnClickListener {
            val intent = Intent(this, ClockActivity::class.java)
            startActivity(intent)
        }

        settingsNavButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        timeTextBox = findViewById(R.id.measuredTimeTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        pauseButton = findViewById(R.id.pauseButton)
        roundButton = findViewById(R.id.roundButton)
        roundsListView = findViewById(R.id.roundsListView)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        Log.i("onStart", "onStart")
        applicationContext.startForegroundService(intent)
        applicationContext.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        Log.i("onStop", "onStop")
        timerService?.removeObserver(this)
        applicationContext.unbindService(serviceConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("onDestroy", "onDestroy")
        val intent = Intent(this, TimerService::class.java)
//        applicationContext.stopService(intent)
    }

    private fun startTimer() {
        timerService?.startTimer()
        pauseButton.text = getString(R.string.pause_button_text)
        adapter.notifyDataSetChanged()

    }

    private fun stopTimer() {
        timerService?.stopTimer()
        pauseButton.text = getString(R.string.pause_button_text)
        timerService?.handler?.post { timeTextBox.text = DEFAULT_TIME_STRING }
        adapter.notifyDataSetChanged()
    }

    private fun pauseTimer() {
        timerService?.pauseTimer()

    }

    private fun addRound() {
        timerService?.addRound()
        adapter.notifyDataSetChanged()
    }

    fun updatePauseButton(isTimerPaused: Boolean) {
        if (isTimerPaused)
            pauseButton.text = getString(R.string.continue_button_text)
        else
            pauseButton.text = getString(R.string.pause_button_text)
    }

    fun updateTimer(timerString: String) {
        timeTextBox.text = timerString
    }

}