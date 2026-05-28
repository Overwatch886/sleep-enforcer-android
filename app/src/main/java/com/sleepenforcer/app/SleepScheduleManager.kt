package com.sleepenforcer.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.Calendar

class SleepScheduleManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("SleepEnforcerPrefs", Context.MODE_PRIVATE)

    // Standard properties
    val warningHour: Int get() = prefs.getInt("warning_hour", 21)
    val warningMinute: Int get() = prefs.getInt("warning_minute", 55)
    val shutdownHour: Int get() = prefs.getInt("shutdown_hour", 22)
    val shutdownMinute: Int get() = prefs.getInt("shutdown_minute", 0)
    val wakeHour: Int get() = prefs.getInt("wake_hour", 6)
    val wakeMinute: Int get() = prefs.getInt("wake_minute", 0)

    val isExtensionActive: Boolean get() = prefs.getBoolean("extension_active", false)
    val extensionShutdownHour: Int get() = prefs.getInt("extension_shutdown_hour", 22)
    val extensionShutdownMinute: Int get() = prefs.getInt("extension_shutdown_minute", 30)
    val extensionWarningHour: Int get() = prefs.getInt("extension_warning_hour", 22)
    val extensionWarningMinute: Int get() = prefs.getInt("extension_warning_minute", 25)

    fun getActiveWarningHour(): Int = if (isExtensionActive) extensionWarningHour else warningHour
    fun getActiveWarningMinute(): Int = if (isExtensionActive) extensionWarningMinute else warningMinute
    fun getActiveShutdownHour(): Int = if (isExtensionActive) extensionShutdownHour else shutdownHour
    fun getActiveShutdownMinute(): Int = if (isExtensionActive) extensionShutdownMinute else shutdownMinute

    /**
     * Resets any active extension back to normal schedule.
     */
    fun resetExtension() {
        prefs.edit()
            .putBoolean("extension_active", false)
            .apply()
        Log.d("SleepScheduleManager", "Extension reset to normal schedule")
    }

    /**
     * Activates a 30-minute extension.
     */
    fun grantExtension(minutes: Int = 30) {
        val cal = Calendar.getInstance()
        
        // Add extension minutes to find new bedtime
        cal.add(Calendar.MINUTE, minutes)
        val extShutdownHour = cal.get(Calendar.HOUR_OF_DAY)
        val extShutdownMinute = cal.get(Calendar.MINUTE)

        // Warning time is 5 minutes before extended bedtime
        cal.add(Calendar.MINUTE, -5)
        val extWarningHour = cal.get(Calendar.HOUR_OF_DAY)
        val extWarningMinute = cal.get(Calendar.MINUTE)

        prefs.edit()
            .putBoolean("extension_active", true)
            .putInt("extension_shutdown_hour", extShutdownHour)
            .putInt("extension_shutdown_minute", extShutdownMinute)
            .putInt("extension_warning_hour", extWarningHour)
            .putInt("extension_warning_minute", extWarningMinute)
            .apply()

        Log.d("SleepScheduleManager", "Extension granted: Bedtime=$extShutdownHour:$extShutdownMinute, Warning=$extWarningHour:$extWarningMinute")
    }

    /**
     * Determines whether the given time falls inside the configured sleep window.
     */
    fun isInsideSleepWindow(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
        val currentHour = currentCal.get(Calendar.HOUR_OF_DAY)
        val currentMin = currentCal.get(Calendar.MINUTE)

        // Active bedtime and wake time limits
        val sh = getActiveShutdownHour()
        val sm = getActiveShutdownMinute()
        val wh = wakeHour
        val wm = wakeMinute

        val currentVal = currentHour * 60 + currentMin
        val startVal = sh * 60 + sm
        val endVal = wh * 60 + wm

        return if (startVal < endVal) {
            // Sleep window does not cross midnight (e.g. 01:00 AM to 08:00 AM)
            currentVal in startVal..endVal
        } else {
            // Sleep window crosses midnight (e.g. 22:00 PM to 06:00 AM next day)
            currentVal >= startVal || currentVal <= endVal
        }
    }

    /**
     * Gets the next exact millisecond timestamp for the warning alarm.
     */
    fun getNextWarningTimeMs(currentTimeMs: Long = System.currentTimeMillis()): Long {
        val hour = getActiveWarningHour()
        val min = getActiveWarningMinute()
        return calculateNextOccurrenceMs(hour, min, currentTimeMs)
    }

    /**
     * Gets the next exact millisecond timestamp for the shutdown alarm.
     */
    fun getNextShutdownTimeMs(currentTimeMs: Long = System.currentTimeMillis()): Long {
        val hour = getActiveShutdownHour()
        val min = getActiveShutdownMinute()
        return calculateNextOccurrenceMs(hour, min, currentTimeMs)
    }

    private fun calculateNextOccurrenceMs(hour: Int, minute: Int, currentTimeMs: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMs
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= currentTimeMs) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
