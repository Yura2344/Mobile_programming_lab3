package com.example.lab3clock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.util.TimeZone

class ClockActivity : AppCompatActivity() {
    private lateinit var timerNavButton: Button
    private lateinit var settingsNavButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        timerNavButton = findViewById(R.id.timerNavButton)
        settingsNavButton = findViewById(R.id.settingsNavButton)

        val currentTimeZone: TimeZone = TimeZone.getDefault()
        findViewById<TextView>(R.id.timezoneTextView).text = getString(
            R.string.timezone_display,
            currentTimeZone.getDisplayName(false, TimeZone.SHORT), currentTimeZone.id
        )

        settingsNavButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        timerNavButton.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }
    }
}