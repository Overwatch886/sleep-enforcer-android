package com.sleepenforcer.app

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var warningTimeEdit: EditText
    private lateinit var shutdownTimeEdit: EditText
    private lateinit var wakeTimeEdit: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scheduleManager = SleepScheduleManager(this)
        if (scheduleManager.isInsideSleepWindow()) {
            finish()
            return
        }

        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("SleepEnforcerPrefs", MODE_PRIVATE)

        warningTimeEdit = findViewById(R.id.warningTimeEdit)
        shutdownTimeEdit = findViewById(R.id.shutdownTimeEdit)
        wakeTimeEdit = findViewById(R.id.wakeTimeEdit)
        saveButton = findViewById(R.id.saveButton)

        loadCurrentSettings()

        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        val scheduleManager = SleepScheduleManager(this)

        val warningHour = scheduleManager.warningHour
        val warningMinute = scheduleManager.warningMinute
        val shutdownHour = scheduleManager.shutdownHour
        val shutdownMinute = scheduleManager.shutdownMinute
        val wakeHour = scheduleManager.wakeHour
        val wakeMinute = scheduleManager.wakeMinute

        warningTimeEdit.setText(String.format("%02d:%02d", warningHour, warningMinute))
        shutdownTimeEdit.setText(String.format("%02d:%02d", shutdownHour, shutdownMinute))
        wakeTimeEdit.setText(String.format("%02d:%02d", wakeHour, wakeMinute))
    }

    private fun saveSettings() {
        val newWarningTime = warningTimeEdit.text.toString().trim()
        val newShutdownTime = shutdownTimeEdit.text.toString().trim()
        val newWakeTime = wakeTimeEdit.text.toString().trim()

        if (!isValidTimeFormat(newWarningTime) || !isValidTimeFormat(newShutdownTime) || !isValidTimeFormat(newWakeTime)) {
            Toast.makeText(this, "Invalid time format. Please use HH:MM.", Toast.LENGTH_SHORT).show()
            return
        }

        val warningParts = newWarningTime.split(":").map { it.toInt() }
        val shutdownParts = newShutdownTime.split(":").map { it.toInt() }
        val wakeParts = newWakeTime.split(":").map { it.toInt() }

        val warningHour = warningParts[0]
        val warningMinute = warningParts[1]
        val shutdownHour = shutdownParts[0]
        val shutdownMinute = shutdownParts[1]
        val wakeHour = wakeParts[0]
        val wakeMinute = wakeParts[1]

        // Lockout check: deny sleep changes if we are within 3 hours of the bedtime
        val scheduleManager = SleepScheduleManager(this)
        val currentCal = java.util.Calendar.getInstance()
        val currentTimeMs = currentCal.timeInMillis

        val activeShutdownTimeMs = scheduleManager.getNextShutdownTimeMs(currentTimeMs)
        val lockoutTimeMs = activeShutdownTimeMs - (3 * 3600 * 1000) // 3 hours in ms

        if (currentTimeMs in lockoutTimeMs until activeShutdownTimeMs) {
            Toast.makeText(this, "⚠️ Changes denied 3 hours close to bedtime.", Toast.LENGTH_LONG).show()
            return
        }

        // Save standard configurations as Integers
        sharedPreferences.edit()
            .putInt("warning_hour", warningHour)
            .putInt("warning_minute", warningMinute)
            .putInt("shutdown_hour", shutdownHour)
            .putInt("shutdown_minute", shutdownMinute)
            .putInt("wake_hour", wakeHour)
            .putInt("wake_minute", wakeMinute)
            .putBoolean("extension_active", false) // Reset active extensions on save
            .apply()

        // Schedule alarms securely
        AlarmUtils.scheduleSleepCheck(this)

        Toast.makeText(this, "Settings saved and alarms scheduled.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun isValidTimeFormat(time: String): Boolean {
        return "^([0-1][0-9]|2[0-3]):[0-5][0-9]$".toRegex().matches(time)
    }
}
