package com.example.lab3clock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import java.util.TimeZone

class SettingsActivity : AppCompatActivity() {
    private lateinit var timezonesSpinner: Spinner
    private lateinit var setTimezoneButton: Button

    private lateinit var clockNavButton: Button
    private lateinit var timerNavButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        timezonesSpinner = findViewById(R.id.timezonesSpinner)
        setTimezoneButton = findViewById(R.id.setTimezoneButton)
        clockNavButton = findViewById(R.id.clockNavButton)
        timerNavButton = findViewById(R.id.timerNavButton)

        val timezones = TimeZone.getAvailableIDs()
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, timezones
        )
        timezonesSpinner.adapter = adapter

        setTimezoneButton.setOnClickListener {
            TimeZone.setDefault(TimeZone.getTimeZone(timezonesSpinner.selectedItem.toString()))
            Toast.makeText(
                applicationContext,
                getString(R.string.timezone_applied),
                Toast.LENGTH_SHORT
            ).show()
        }

        clockNavButton.setOnClickListener {
            val intent = Intent(this, ClockActivity::class.java)
            startActivity(intent)
        }

        timerNavButton.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }
    }
}